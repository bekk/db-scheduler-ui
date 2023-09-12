package com.github.bekk.dbscheduleruiapi.service;

import com.github.bekk.dbscheduleruiapi.model.GetTasksResponse;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.model.TaskModel;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams;
import com.github.bekk.dbscheduleruiapi.util.QueryUtils;
import com.github.bekk.dbscheduleruiapi.util.mapper.LogModelRowMapper;
import com.github.bekk.dbscheduleruiapi.util.mapper.TaskMapper;
import com.github.bekk.dbscheduleruiapi.model.LogModel;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskLogic {

    private final Scheduler scheduler;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TaskLogic(Scheduler scheduler, DataSource dataSource){
        this.scheduler = scheduler;
        this.scheduler.start();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void runTaskNow(String taskId, String taskName) {
        Optional<ScheduledExecution<Object>> scheduledExecutionOpt = scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

        if (scheduledExecutionOpt.isPresent()) {
            TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
            scheduler.reschedule(taskInstance, Instant.now());
        } else {
            // Handle the case where the ScheduledExecution is not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
        }
    }

    public void deleteTask(String taskId, String taskName) {
        Optional<ScheduledExecution<Object>> scheduledExecutionOpt = scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

        if (scheduledExecutionOpt.isPresent()) {
            TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
            scheduler.cancel(taskInstance);
        } else {
            // Handle the case where the ScheduledExecution is not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
        }
    }

    public GetTasksResponse getAllTasks(TaskRequestParams params) {
        List<TaskModel> tasks = TaskMapper.mapAllExecutionsToTaskModel(scheduler.getScheduledExecutions(),
                scheduler.getCurrentlyExecuting());

        tasks = QueryUtils.sortTasks(
                QueryUtils.filterTasks(tasks, params.getFilter()), params.getSorting(), params.isAsc());
        List<TaskModel> pagedTasks = QueryUtils.paginate(tasks, params.getPageNumber(), params.getSize());
        return new GetTasksResponse(tasks.size(), pagedTasks, params.getSize());
    }
    

    public GetTasksResponse getTask(TaskDetailsRequestParams params) {
        List<TaskModel> tasks = params.getTaskId()!=null
        ? TaskMapper.mapAllExecutionsToTaskModelUngrouped(scheduler.getScheduledExecutions(), scheduler.getCurrentlyExecuting()).stream().filter(task -> {
            return task.getTaskName().equals(params.getTaskName()) && task.getTaskInstance().get(0).equals(params.getTaskId());
        }).collect(Collectors.toList())
        : TaskMapper.mapAllExecutionsToTaskModelUngrouped(scheduler.getScheduledExecutions(), scheduler.getCurrentlyExecuting()).stream().filter(task -> {
            return task.getTaskName().equals(params.getTaskName());
        }).collect(Collectors.toList());
        if (tasks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No tasks found for taskName: "
                    + params.getTaskName() + ", taskId: " + params.getTaskId());
        }
        tasks = QueryUtils.sortTasks(
                QueryUtils.filterTasks(tasks, params.getFilter()), params.getSorting(), params.isAsc());
        List<TaskModel> pagedTasks = QueryUtils.paginate(tasks, params.getPageNumber(), params.getSize());
        return new GetTasksResponse(tasks.size(), pagedTasks, params.getSize());
        }
        
    public List<LogModel> getLogs() {
        return jdbcTemplate.query("SELECT * FROM scheduled_execution_logs", new LogModelRowMapper());

    }
}
