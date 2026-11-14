-- Supports the instance-detail endpoint, which reads one instance's newest runs:
--   where task_name = ? and task_instance = ? order by id desc
-- Without it the query scans every log row of the task. Mirrors sql/log-table/*.sql.
CREATE INDEX stl_task_instance_idx ON scheduled_execution_logs (task_name, task_instance, id);
