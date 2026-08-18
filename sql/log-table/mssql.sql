CREATE TABLE scheduled_execution_logs (
    id                   BIGINT          NOT NULL PRIMARY KEY,
    task_name            VARCHAR(250)    NOT NULL,
    task_instance        VARCHAR(250)    NOT NULL,
    task_data            VARBINARY(MAX),
    picked_by            VARCHAR(50),
    time_started         DATETIMEOFFSET  NOT NULL,
    time_finished        DATETIMEOFFSET  NOT NULL,
    succeeded            BIT             NOT NULL,
    duration_ms          BIGINT          NOT NULL,
    exception_class      VARCHAR(1000),
    exception_message    NVARCHAR(MAX),
    exception_stacktrace NVARCHAR(MAX)
);

CREATE INDEX stl_started_idx         ON scheduled_execution_logs (time_started);
CREATE INDEX stl_task_name_idx       ON scheduled_execution_logs (task_name);
CREATE INDEX stl_task_instance_idx   ON scheduled_execution_logs (task_name, task_instance, id);
CREATE INDEX stl_exception_class_idx ON scheduled_execution_logs (exception_class);
