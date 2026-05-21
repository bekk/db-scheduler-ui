CREATE TABLE scheduled_execution_logs (
    id                   BIGINT          NOT NULL PRIMARY KEY,
    task_name            VARCHAR(250)    NOT NULL,
    task_instance        VARCHAR(250)    NOT NULL,
    task_data            BLOB,
    picked_by            VARCHAR(50),
    time_started         DATETIME(6)    NOT NULL,
    time_finished        DATETIME(6)    NOT NULL,
    succeeded            BOOLEAN         NOT NULL,
    duration_ms          BIGINT          NOT NULL,
    exception_class      VARCHAR(1000),
    exception_message    TEXT,
    exception_stacktrace MEDIUMTEXT
);

CREATE INDEX stl_started_idx         ON scheduled_execution_logs (time_started);
CREATE INDEX stl_task_name_idx       ON scheduled_execution_logs (task_name);
CREATE INDEX stl_exception_class_idx ON scheduled_execution_logs (exception_class);
