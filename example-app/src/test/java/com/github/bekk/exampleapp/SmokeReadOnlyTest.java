package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.FailingTask.FAILING_ONETIME_TASK;
import static org.assertj.core.api.Assertions.assertThat;

import no.bekk.dbscheduler.ui.model.GetTasksResponse;
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
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    properties = "db-scheduler-ui.read-only=true",
    classes = ExampleApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class SmokeReadOnlyTest {

  @LocalServerPort private int serverPort;

  private String baseUrl;

  @Autowired private ApplicationContext context;

  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }

  @Test
  void testContextLoads() {
    assertThat(context.containsBean("taskAdminController")).isFalse();
    assertThat(context.containsBean("taskController")).isTrue();
  }

  @Test
  void readingTasksWorks() {
    ResponseEntity<GetTasksResponse> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler-api/tasks/all", GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getItems()).isNotEmpty();
  }

  @Test
  void deletingTasksIsNotAvailableInReadOnlyMode() {
    ResponseEntity<Void> result =
        restTemplate.postForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/delete?id=%d&name=%s"
                    .formatted(6, FAILING_ONETIME_TASK.getTaskName()),
            null,
            Void.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
