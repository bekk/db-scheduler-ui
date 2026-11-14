package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.SEND_WELCOME_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

import no.bekk.dbscheduler.ui.model.GetTasksResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = {ExampleApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class SmokeTest {

  @LocalServerPort private Integer serverPort;

  private String baseUrl;

  @Autowired private ApplicationContext context;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void testContextLoads() {
    assertThat(context.containsBean("taskAdminController")).isTrue();
    assertThat(context.containsBean("taskController")).isTrue();
    // The two starters keep separate copies of UiApiAutoConfiguration, so a bean added to one
    // and forgotten in the other compiles, passes every other test, and 404s only at runtime.
    assertThat(context.containsBean("instanceController")).isTrue();
    assertThat(context.containsBean("instanceService")).isTrue();
  }

  @Test
  void testGetTasksReturnsStatusOK() {
    ResponseEntity<GetTasksResponse> result =
        this.restTemplate.getForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/all?filter=ALL&pageNumber=0&size=10&sorting=DEFAULT&asc=true&searchTerm="
                + SEND_WELCOME_EMAIL.getTaskName(),
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
                + SEND_WELCOME_EMAIL.getTaskName(),
            GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    result.getBody().getItems().forEach(t -> System.out.println(t.getTaskName()));
    assertThat(result.getBody().getItems())
        .anyMatch(taskModel -> taskModel.getTaskName().equals(SEND_WELCOME_EMAIL.getTaskName()));
  }

  @Test
  void testGetOverviewReturnsExampleOneTimeTask() {
    ResponseEntity<OverviewTask[]> result =
        this.restTemplate.getForEntity(
            baseUrl + "/db-scheduler-api/tasks/overview", OverviewTask[].class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody())
        .anyMatch(task -> task.taskName().equals(SEND_WELCOME_EMAIL.getTaskName()));
  }

  @Test
  void deletingTaskReturnsStatusOK() {
    ResponseEntity<Void> result =
        restTemplate.postForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/delete?id=%s&name=%s"
                    .formatted("delete-1", SEND_WELCOME_EMAIL.getTaskName()),
            null,
            Void.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  // Regression test for
  // issue #149: the ByteArrayResource fallback threw FileNotFoundException on lastModified(),
  // returning 500 instead of the SPA shell.
  @Test
  void unknownSpaRouteServesIndexHtml() {
    ResponseEntity<String> result =
        this.restTemplate.getForEntity(baseUrl + "/db-scheduler/some-spa-route", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).contains("/db-scheduler");
    assertThat(result.getHeaders().getContentType()).isNotNull();
    assertThat(result.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
  }

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }
}
