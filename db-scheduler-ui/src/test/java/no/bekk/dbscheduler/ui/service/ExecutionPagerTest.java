package no.bekk.dbscheduler.ui.service;

import static no.bekk.dbscheduler.ui.model.ExecutionStatusFilter.ALL;
import static no.bekk.dbscheduler.ui.model.ExecutionStatusFilter.FAILING;
import static no.bekk.dbscheduler.ui.model.ExecutionStatusFilter.OVERDUE;
import static no.bekk.dbscheduler.ui.model.ExecutionStatusFilter.RUNNING;
import static no.bekk.dbscheduler.ui.model.ExecutionStatusFilter.SCHEDULED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.model.ExecutionPage;
import no.bekk.dbscheduler.ui.model.ExecutionRow;
import no.bekk.dbscheduler.ui.model.ExecutionStatusFilter;
import no.bekk.dbscheduler.ui.model.InstanceStatus;
import no.bekk.dbscheduler.ui.testsupport.ScheduledTasksTable;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionPagerTest {

  private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");
  private static final String TASK = "invoice";

  private final OneTimeTask<String> invoice =
      Tasks.oneTime(TASK, String.class).execute((instance, context) -> {});
  private final OneTimeTask<String> other =
      Tasks.oneTime("other", String.class).execute((instance, context) -> {});

  private DataSource dataSource;
  private SchedulerClient client;

  @BeforeEach
  void setUp() {
    dataSource = TestDatabase.newDataSourceWithTables();
    ScheduledTasksTable.createSchema(dataSource);
    client = SchedulerClient.Builder.create(dataSource, List.of(invoice, other)).build();
  }

  @Test
  void all_returnsEveryExecutionInTimeOrder() {
    schedule("b", NOW.plusSeconds(20));
    schedule("a", NOW.plusSeconds(10));
    schedule("c", NOW.plusSeconds(30));

    assertThat(ids(pager().page(TASK, ALL, null, null, null))).containsExactly("a", "b", "c");
  }

  @Test
  void failing_onlyUnpickedWithFailures() {
    seedMixedStates();

    assertThat(ids(pager().page(TASK, FAILING, null, null, null))).containsExactly("failing");
  }

  @Test
  void scheduled_onlyUnpickedWithoutFailures() {
    seedMixedStates();

    assertThat(ids(pager().page(TASK, SCHEDULED, null, null, null))).containsExactly("scheduled");
  }

  @Test
  void running_onlyPicked() {
    seedMixedStates();

    assertThat(ids(pager().page(TASK, RUNNING, null, null, null)))
        .containsExactly("running", "runningAndFailing");
  }

  @Test
  void pickedAndFailing_onlyInAllAndRunning() {
    seedMixedStates();

    ExecutionRow row =
        pager().page(TASK, ALL, null, null, null).items().stream()
            .filter(r -> r.taskInstance().equals("runningAndFailing"))
            .findFirst()
            .orElseThrow();

    assertThat(row.status()).isEqualTo(InstanceStatus.RUNNING);
    assertThat(row.consecutiveFailures()).isEqualTo(2);
    assertThat(ids(pager().page(TASK, FAILING, null, null, null)))
        .doesNotContain("runningAndFailing");
    assertThat(ids(pager().page(TASK, SCHEDULED, null, null, null)))
        .doesNotContain("runningAndFailing");
  }

  @Test
  void overdue_unpickedInThePast_ascending() {
    schedule("in-1h", NOW.plus(Duration.ofHours(1)));
    schedule("ago-1h", NOW.minus(Duration.ofHours(1)));
    schedule("ago-3h", NOW.minus(Duration.ofHours(3)));
    schedule("picked-ago-2h", NOW.minus(Duration.ofHours(2)));
    ScheduledTasksTable.markPicked(dataSource, TASK, "picked-ago-2h");

    assertThat(ids(pager().page(TASK, OVERDUE, null, null, null)))
        .containsExactly("ago-3h", "ago-1h");
  }

  @Test
  void overdue_paging_staysAscending() {
    schedule("ago-3h", NOW.minus(Duration.ofHours(3)));
    schedule("ago-2h", NOW.minus(Duration.ofHours(2)));
    schedule("ago-1h", NOW.minus(Duration.ofHours(1)));

    assertThat(collectAll(OVERDUE, 1)).containsExactly("ago-3h", "ago-2h", "ago-1h");
  }

  @Test
  void paging_equalExecutionTimes_noSkipOrDuplicate() {
    for (int i = 0; i < 7; i++) {
      schedule("same-" + i, NOW.plusSeconds(60));
    }

    ExecutionPage first = pager().page(TASK, ALL, null, null, 3);
    ExecutionPage second = pager().page(TASK, ALL, first.next(), null, 3);
    ExecutionPage third = pager().page(TASK, ALL, second.next(), null, 3);

    assertThat(first.items()).hasSize(3);
    assertThat(second.items()).hasSize(3);
    assertThat(third.items()).hasSize(1);
    assertThat(third.next()).isNull();
    List<String> all = new ArrayList<>();
    all.addAll(ids(first));
    all.addAll(ids(second));
    all.addAll(ids(third));
    assertThat(all).doesNotHaveDuplicates().hasSize(7);
  }

  @Test
  void lastPage_hasNoNext() {
    schedule("a", NOW.plusSeconds(10));
    schedule("b", NOW.plusSeconds(20));

    assertThat(pager().page(TASK, ALL, null, null, 2).next()).isNull();
    assertThat(pager().page(TASK, ALL, null, null, 1).next()).isNotNull();
  }

  @Test
  void from_startsAtThatTime() {
    schedule("early", NOW.plusSeconds(10));
    schedule("late", NOW.plusSeconds(100));

    assertThat(ids(pager().page(TASK, ALL, null, NOW.plusSeconds(50), null)))
        .containsExactly("late");
  }

  @Test
  void cursorWinsOverFrom() {
    schedule("a", NOW.plusSeconds(10));
    schedule("b", NOW.plusSeconds(20));
    schedule("c", NOW.plusSeconds(30));
    String afterA = pager().page(TASK, ALL, null, null, 1).next();

    assertThat(ids(pager().page(TASK, ALL, afterA, NOW.plusSeconds(25), null)))
        .containsExactly("b", "c");
  }

  @Test
  void limit_defaultsTo50_clampsTo200() {
    for (int i = 0; i < 210; i++) {
      schedule(String.format("i-%03d", i), NOW.plusSeconds(i));
    }

    assertThat(pager().page(TASK, ALL, null, null, null).items()).hasSize(50);
    assertThat(pager().page(TASK, ALL, null, null, 1000).items()).hasSize(200);
  }

  @Test
  void cursor_roundTripsAwkwardIds() {
    List<String> awkward = List.of("a/b", "c:d", "e f", "g%h");
    for (int i = 0; i < awkward.size(); i++) {
      schedule(awkward.get(i), NOW.plusSeconds(i));
    }

    assertThat(collectAll(ALL, 1)).containsExactlyElementsOf(awkward);
  }

  @Test
  void otherTasksExecutions_neverIncluded() {
    schedule("mine", NOW.plusSeconds(10));
    client.scheduleIfNotExists(other.instance("theirs"), NOW.plusSeconds(5));

    assertThat(ids(pager().page(TASK, ALL, null, null, null))).containsExactly("mine");
  }

  @Test
  void taskDataPreview_followsShowTaskData() {
    client.scheduleIfNotExists(invoice.instance("with-data", "hello"), NOW.plusSeconds(10));

    assertThat(pager().page(TASK, ALL, null, null, null).items().get(0).taskDataPreview())
        .isEqualTo("\"hello\"");
    assertThat(
            new ExecutionPager(client, false, clock())
                .page(TASK, ALL, null, null, null)
                .items()
                .get(0)
                .taskDataPreview())
        .isNull();
  }

  @Test
  void malformedCursor_isRejected() {
    assertThatThrownBy(() -> pager().page(TASK, ALL, "not-a-cursor", null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void seedMixedStates() {
    schedule("failing", NOW.plusSeconds(10));
    schedule("running", NOW.plusSeconds(20));
    schedule("runningAndFailing", NOW.plusSeconds(30));
    schedule("scheduled", NOW.plusSeconds(40));
    ScheduledTasksTable.markFailing(dataSource, TASK, "failing", 3);
    ScheduledTasksTable.markPicked(dataSource, TASK, "running");
    ScheduledTasksTable.markPicked(dataSource, TASK, "runningAndFailing");
    ScheduledTasksTable.markFailing(dataSource, TASK, "runningAndFailing", 2);
  }

  private List<String> collectAll(ExecutionStatusFilter status, int limit) {
    List<String> ids = new ArrayList<>();
    String cursor = null;
    // Bounded so a cursor that never advances fails the test instead of hanging it.
    for (int pages = 0; pages < 100; pages++) {
      ExecutionPage page = pager().page(TASK, status, cursor, null, limit);
      ids.addAll(ids(page));
      cursor = page.next();
      if (cursor == null) {
        return ids;
      }
    }
    throw new AssertionError("Paging did not end after 100 pages; collected " + ids.subList(0, 5));
  }

  private void schedule(String id, Instant time) {
    client.scheduleIfNotExists(invoice.instance(id), time);
  }

  private ExecutionPager pager() {
    return new ExecutionPager(client, true, clock());
  }

  private static Clock clock() {
    return Clock.fixed(NOW, ZoneOffset.UTC);
  }

  private static List<String> ids(ExecutionPage page) {
    return page.items().stream().map(ExecutionRow::taskInstance).toList();
  }
}
