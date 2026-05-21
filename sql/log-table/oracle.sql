CREATE TABLE scheduled_execution_logs (
    id                   NUMBER                       NOT NULL PRIMARY KEY,
    task_name            VARCHAR2(250)                NOT NULL,
    task_instance        VARCHAR2(250)                NOT NULL,
    task_data            BLOB,
    picked_by            VARCHAR2(50),
    time_started         TIMESTAMP(6) WITH TIME ZONE  NOT NULL,
    time_finished        TIMESTAMP(6) WITH TIME ZONE  NOT NULL,
    succeeded            NUMBER(1, 0)                 NOT NULL,
    duration_ms          NUMBER                       NOT NULL,
    exception_class      VARCHAR2(1000),
    exception_message    CLOB,
    exception_stacktrace CLOB
);

CREATE INDEX stl_started_idx         ON scheduled_execution_logs (time_started);
CREATE INDEX stl_task_name_idx       ON scheduled_execution_logs (task_name);
CREATE INDEX stl_exception_class_idx ON scheduled_execution_logs (exception_class);
