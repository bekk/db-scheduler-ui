package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.RecurringTaskExample.RECURRING_TASK;
import static org.assertj.core.api.Assertions.assertThat;

import no.bekk.dbscheduler.ui.model.ConfigResponse;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Overview endpoint smoke test — the example app enables {@code db-scheduler-ui.overview=true}. */
@SpringBootTest(
    classes = {ExampleApp.class},
    properties = "db-scheduler-ui.overview=true",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class OverviewSmokeTest {

  @LocalServerPort private Integer serverPort;
  private String baseUrl;

  @Autowired private ApplicationContext context;
  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }

  @Test
  void overviewBeansAreWiredWhenFlagOn() {
    assertThat(context.containsBean("overviewController")).isTrue();
    assertThat(context.containsBean("overviewLogic")).isTrue();
  }

  @Test
  void overviewEndpointReturnsAggregatedRows() {
    ResponseEntity<OverviewTask[]> result =
        restTemplate.getForEntity(
            baseUrl + "/db-scheduler-api/tasks/overview", OverviewTask[].class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody())
        .as("each task name appears at most once")
        .extracting(OverviewTask::taskName)
        .doesNotHaveDuplicates();
  }

  @Test
  void recurringDefinitionIsFlaggedRecurring() {
    ResponseEntity<OverviewTask[]> result =
        restTemplate.getForEntity(
            baseUrl + "/db-scheduler-api/tasks/overview", OverviewTask[].class);

    assertThat(result.getBody())
        .filteredOn(row -> row.taskName().equals(RECURRING_TASK.getTaskName()))
        .singleElement()
        .satisfies(row -> assertThat(row.recurring()).isTrue());
  }

  @Test
  void configExposesShowOverviewTrue() {
    ResponseEntity<ConfigResponse> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler-api/config", ConfigResponse.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().isShowOverview()).isTrue();
  }
}
