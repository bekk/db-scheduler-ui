package com.github.bekk.dbscheduleruiapi.util.mapper;

import com.github.bekk.dbscheduleruiapi.model.LogModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.springframework.jdbc.core.RowMapper;

public class LogModelRowMapper implements RowMapper<LogModel> {

    @Override
    public LogModel mapRow(ResultSet rs, int rowNum) throws SQLException{
        long id = rs.getLong("id");
        String taskName = rs.getString("task_name");
        String taskInstance = rs.getString("task_instance");
        byte[] taskData = rs.getBytes("task_data");
        Instant timeStarted = rs.getTimestamp("time_started").toInstant();
        Instant timeFinished = rs.getTimestamp("time_finished").toInstant();
        boolean succeeded = rs.getBoolean("succeeded");
        Long durationMs = rs.getLong("duration_ms");
        String exceptionClass = rs.getString("exception_class");
        String exceptionMessage = rs.getString("exception_message");
        String exceptionStackTrace = rs.getString("exception_stacktrace");
        return new LogModel(
                rs.getLong("id"),
                rs.getString("task_name"),
                rs.getString("task_instance"),
                rs.getBytes("task_data"),
                rs.getTimestamp("time_started").toInstant(),
                rs.getTimestamp("time_finished").toInstant(),
                rs.getBoolean("succeeded"),
                rs.getLong("duration_ms"),
                rs.getString("exception_class"),
                rs.getString("exception_message"),
                rs.getString("exception_stacktrace")
        );
    }
}
