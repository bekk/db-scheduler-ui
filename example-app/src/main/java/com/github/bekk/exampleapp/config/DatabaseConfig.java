package com.github.bekk.exampleapp.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

@Configuration
public class DatabaseConfig {
  private final DataSource dataSource;

  public DatabaseConfig(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public SimpleJdbcInsert simpleJdbcInsert() {
    return new SimpleJdbcInsert(dataSource)
        .withTableName("scheduled_tasks")
        .usingGeneratedKeyColumns("id");
  }

  @Bean
  public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(dataSource);
  }
}
