create table scheduled_execution_logs (
    id                   BIGINT                   not null primary key,
    task_name            VARCHAR(250)             not null,
    task_instance        VARCHAR(250)             not null,
    task_data            VARBINARY,
    picked_by            VARCHAR(50),
    time_started         timestamp with time zone not null,
    time_finished        timestamp with time zone not null,
    succeeded            BOOLEAN                  not null,
    duration_ms          BIGINT                   not null,
    exception_class      VARCHAR(1000),
    exception_message    CLOB,
    exception_stacktrace CLOB
);

CREATE INDEX stl_started_idx         ON scheduled_execution_logs (time_started);
CREATE INDEX stl_task_name_idx       ON scheduled_execution_logs (task_name);
CREATE INDEX stl_exception_class_idx ON scheduled_execution_logs (exception_class);
