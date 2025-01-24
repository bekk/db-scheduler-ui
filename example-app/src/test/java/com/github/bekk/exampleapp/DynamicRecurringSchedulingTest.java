package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.DynamicRecurringTaskExample.DYNAMIC_RECURRING_TASK;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.bekk.exampleapp.model.TaskScheduleAndNoData;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import java.time.Instant;
import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = {ExampleApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class DynamicRecurringSchedulingTest {

  @LocalServerPort private Integer serverPort;
  private String baseUrl;
  @Autowired private TestRestTemplate restTemplate;

  @Autowired private SchedulerClient schedulerClient;

  @Test
  void testGetTasksReturnsDynamicRecurringTask() {
    // Given
    CronSchedule cron = Schedules.cron("0 0/1 * * * *"); // every minute
    TaskScheduleAndNoData data = new TaskScheduleAndNoData(cron);

    schedulerClient.scheduleIfNotExists(
        DYNAMIC_RECURRING_TASK.instance("single_instance").data(data).build(),
        cron.getInitialExecutionTime(Instant.now()));

    // When
    ResponseEntity<GetTasksResponse> result =
        this.restTemplate.getForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/all?filter=ALL&pageNumber=0&size=10&sorting=DEFAULT&asc=true&searchTerm="
                + DYNAMIC_RECURRING_TASK.getTaskName(),
            GetTasksResponse.class);

    // Then
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    result.getBody().getItems().forEach(t -> System.out.println(t.getTaskName()));
    assertThat(result.getBody().getItems())
        .anyMatch(
            taskModel -> taskModel.getTaskName().equals(DYNAMIC_RECURRING_TASK.getTaskName()));
  }

  @BeforeEach
  public void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }
}
