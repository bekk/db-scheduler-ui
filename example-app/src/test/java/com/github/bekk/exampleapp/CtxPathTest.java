package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.ONE_TIME_TASK;
import static org.assertj.core.api.Assertions.assertThat;

import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("ctxpath")
@SpringBootTest(
    classes = {ExampleApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class CtxPathTest {

  @LocalServerPort private Integer serverPort;

  @Value("${server.servlet.context-path:}") private String ctxPath;

  private String baseUrl;

  @Autowired private ApplicationContext context;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void testContextLoads() {
    assertThat(context.containsBean("taskAdminController")).isTrue();
    assertThat(context.containsBean("taskController")).isTrue();
  }

  @Test
  void testGetTasksReturnsStatusOK() {
    ResponseEntity<GetTasksResponse> result =
        this.restTemplate.getForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/all?filter=ALL&pageNumber=0&size=10&sorting=DEFAULT&asc=true&searchTerm="
                + ONE_TIME_TASK.getTaskName(),
            GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getItems()).isNotEmpty();
  }

  @Test
  void testGetTasksReturnsExampleOneTimeTask() {
    ResponseEntity<GetTasksResponse> result =
        this.restTemplate.getForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/all?filter=ALL&pageNumber=0&size=10&sorting=DEFAULT&asc=true&searchTerm="
                + ONE_TIME_TASK.getTaskName(),
            GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    result.getBody().getItems().forEach(t -> System.out.println(t.getTaskName()));
    assertThat(result.getBody().getItems())
        .anyMatch(taskModel -> taskModel.getTaskName().equals(ONE_TIME_TASK.getTaskName()));
  }

  @Test
  void deletingTaskReturnsStatusOK() {
    ResponseEntity<Void> result =
        restTemplate.postForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/delete?id=%s&name=%s"
                    .formatted("delete-1", ONE_TIME_TASK.getTaskName()),
            null,
            Void.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testGetUiPage() {
   ResponseEntity<String> result =
        this.restTemplate.getForEntity(
            baseUrl
                + "/db-scheduler/index.html",
            String.class);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody().contains("/api/test")).isTrue();
  }

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort + ctxPath;
  }
}
