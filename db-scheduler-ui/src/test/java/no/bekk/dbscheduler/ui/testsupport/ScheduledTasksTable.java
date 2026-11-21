package no.bekk.dbscheduler.ui.testsupport;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/** db-scheduler's {@code scheduled_tasks} table, for tests that need a real SchedulerClient. */
public final class ScheduledTasksTable {

  private ScheduledTasksTable() {}

  public static void createSchema(DataSource dataSource) {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    jdbc.execute(
        """
        create table scheduled_tasks (
          task_name text not null,
          task_instance text not null,
          task_data bytea,
          execution_time timestamp with time zone not null,
          picked boolean not null,
          picked_by text,
          last_success timestamp with time zone,
          last_failure timestamp with time zone,
          consecutive_failures int,
          last_heartbeat timestamp with time zone,
          version bigint not null,
          priority smallint,
          primary key (task_name, task_instance)
        )""");
    jdbc.execute("create index execution_time_idx on scheduled_tasks (execution_time)");
    jdbc.execute(
        "create index st_task_time_idx on scheduled_tasks (task_name, execution_time, task_instance)");
  }

  /** Records {@code failures} consecutive failures, the last one now-ish. */
  public static void markFailing(DataSource dataSource, String task, String id, int failures) {
    new JdbcTemplate(dataSource)
        .update(
            "update scheduled_tasks set consecutive_failures = ?, last_failure = current_timestamp"
                + " where task_name = ? and task_instance = ?",
            failures,
            task,
            id);
  }

  public static void markPicked(DataSource dataSource, String task, String id) {
    new JdbcTemplate(dataSource)
        .update(
            "update scheduled_tasks set picked = true, picked_by = 'test-node'"
                + " where task_name = ? and task_instance = ?",
            task,
            id);
  }
}
