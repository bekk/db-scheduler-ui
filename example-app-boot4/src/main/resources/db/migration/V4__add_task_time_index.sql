-- Supports the task page, which pages one task's executions in time order:
--   where task_name = ? [and ...] order by execution_time, task_instance
-- Without it the query sorts every execution of the task, or walks other tasks' rows in
-- execution_time_idx. Recommended in the README for existing deployments.
CREATE INDEX st_task_time_idx ON scheduled_tasks (task_name, execution_time, task_instance);
