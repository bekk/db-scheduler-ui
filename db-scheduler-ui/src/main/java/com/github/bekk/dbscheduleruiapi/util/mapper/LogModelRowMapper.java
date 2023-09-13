package com.github.bekk.dbscheduleruiapi.util.mapper;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class LogModelRowMapper implements RowMapper<LogModel> {

    @Override
    public LogModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LogModel(
                rs.getLong("id"),
                rs.getString("task_name"),
                rs.getString("task_instance"),
                rs.getString("task_data"),
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
