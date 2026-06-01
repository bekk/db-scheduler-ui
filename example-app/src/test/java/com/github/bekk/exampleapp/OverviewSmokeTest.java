package com.github.bekk.exampleapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.WorstStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = ExampleApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class OverviewSmokeTest {

  @LocalServerPort private int serverPort;

  private String baseUrl;

  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }

  private OverviewTask byName(OverviewTask[] tasks, String name) {
    return Arrays.stream(tasks).filter(t -> name.equals(t.getTaskName())).findFirst().orElse(null);
  }

  @Test
  void overviewEndpointAggregatesAndClassifiesTasks() {
    ResponseEntity<OverviewTask[]> result =
        restTemplate.getForEntity(
            baseUrl + "/db-scheduler-api/tasks/overview", OverviewTask[].class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    OverviewTask[] tasks = result.getBody();
    assertThat(tasks).isNotNull().isNotEmpty();

    // Plain RecurringTask: auto-schedules on startup, so always present and recurring.
    OverviewTask recurring = byName(tasks, "recurring-task");
    assertThat(recurring).isNotNull();
    assertThat(recurring.getRecurring()).isTrue();
    assertThat(recurring.getWorstStatus()).isNotEqualTo(WorstStatus.DORMANT);

    // RecurringTaskWithPersistentSchedule is intentionally NOT classified recurring — it belongs
    // in the one-time/dynamic/custom group (multi-instance, dynamic schedule, dormant-eligible).
    OverviewTask dynamic = byName(tasks, "dynamic-recurring-task");
    assertThat(dynamic).isNotNull();
    assertThat(dynamic.getRecurring()).isFalse();
  }
}
