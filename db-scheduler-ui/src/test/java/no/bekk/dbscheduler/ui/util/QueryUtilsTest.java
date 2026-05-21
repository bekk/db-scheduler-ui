/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.bekk.dbscheduler.ui.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import no.bekk.dbscheduler.ui.model.TaskModel;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskFilter;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskSort;
import org.junit.jupiter.api.Test;

class QueryUtilsTest {

  // --- Pagination ---

  @Test
  void paginate_emptyList_returnsEmpty() {
    assertThat(QueryUtils.paginate(List.of(), 0, 10)).isEmpty();
  }

  @Test
  void paginate_firstPage_returnsFirstNItems() {
    List<String> items = IntStream.rangeClosed(1, 15).mapToObj(String::valueOf).toList();
    List<String> page = QueryUtils.paginate(items, 0, 10);
    assertThat(page).hasSize(10).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
  }

  @Test
  void paginate_pageOutOfBounds_returnsEmpty() {
    List<String> items = IntStream.rangeClosed(1, 5).mapToObj(String::valueOf).toList();
    assertThat(QueryUtils.paginate(items, 1, 10)).isEmpty();
  }

  // --- Filter ---

  @Test
  void filterTasks_failedFilter_returnsOnlyTasksWithConsecutiveFailures() {
    TaskModel failed = task("failing-job", false, 3);
    TaskModel clean = task("clean-job", false, 0);

    List<TaskModel> result = QueryUtils.filterTasks(List.of(failed, clean), TaskFilter.FAILED);

    assertThat(result).containsExactly(failed);
  }

  @Test
  void filterTasks_runningFilter_returnsOnlyPickedTasks() {
    TaskModel running = task("running-job", true, 0);
    TaskModel waiting = task("waiting-job", false, 0);

    List<TaskModel> result = QueryUtils.filterTasks(List.of(running, waiting), TaskFilter.RUNNING);

    assertThat(result).containsExactly(running);
  }

  @Test
  void filterTasks_scheduledFilter_excludesFailedAndRunning() {
    TaskModel scheduled = task("scheduled-job", false, 0);
    TaskModel running = task("running-job", true, 0);
    TaskModel failed = task("failed-job", false, 2);

    List<TaskModel> result =
        QueryUtils.filterTasks(List.of(scheduled, running, failed), TaskFilter.SCHEDULED);

    assertThat(result).containsExactly(scheduled);
  }

  // --- Sort ---

  @Test
  void sortTasks_byNameAscending_returnsSortedAlphabetically() {
    List<TaskModel> tasks =
        Arrays.asList(
            task("zebra-job", false, 0), task("alpha-job", false, 0), task("middle-job", false, 0));

    List<TaskModel> sorted = QueryUtils.sortTasks(tasks, TaskSort.NAME, true);

    assertThat(sorted)
        .extracting(TaskModel::getTaskName)
        .containsExactly("alpha-job", "middle-job", "zebra-job");
  }

  @Test
  void sortTasks_byNameDescending_returnsReverseSorted() {
    List<TaskModel> tasks =
        Arrays.asList(
            task("zebra-job", false, 0), task("alpha-job", false, 0), task("middle-job", false, 0));

    List<TaskModel> sorted = QueryUtils.sortTasks(tasks, TaskSort.NAME, false);

    assertThat(sorted)
        .extracting(TaskModel::getTaskName)
        .containsExactly("zebra-job", "middle-job", "alpha-job");
  }

  // --- Search ---

  @Test
  void search_partialMatch_returnsAllContainingTasks() {
    TaskModel sendEmail = task("send-email", false, 0);
    TaskModel sendSms = task("send-sms", false, 0);
    TaskModel payment = task("payment-job", false, 0);

    List<TaskModel> result =
        QueryUtils.search(List.of(sendEmail, sendSms, payment), "send", true, false);

    assertThat(result).containsExactlyInAnyOrder(sendEmail, sendSms);
  }

  @Test
  void search_exactMatch_returnsOnlyExactTask() {
    TaskModel sendEmail = task("send-email", false, 0);
    TaskModel sendSms = task("send-sms", false, 0);

    List<TaskModel> result =
        QueryUtils.search(List.of(sendEmail, sendSms), "send-email", true, true);

    assertThat(result).containsExactly(sendEmail);
  }

  // --- Helper ---

  private static TaskModel task(String name, boolean picked, int failures) {
    return new TaskModel(
        name,
        List.of(name + "-instance"),
        List.of(),
        List.of(Instant.now()),
        List.of(picked),
        List.of(picked ? "worker-1" : ""),
        List.of(),
        null,
        List.of(failures),
        null,
        1);
  }
}
