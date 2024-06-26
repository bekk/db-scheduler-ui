create table if not exists scheduled_tasks (
    task_name varchar(255),
    task_instance varchar(255),
    task_data BYTEA,
    execution_time TIMESTAMP WITH TIME ZONE,
                                 picked BIT,
                                 picked_by varchar(50),
    last_success TIMESTAMP WITH TIME ZONE,
    last_failure TIMESTAMP WITH TIME ZONE,
    consecutive_failures INT,
    last_heartbeat TIMESTAMP WITH TIME ZONE,
                                 version BIGINT,
                                 PRIMARY KEY (task_name, task_instance)
    );
