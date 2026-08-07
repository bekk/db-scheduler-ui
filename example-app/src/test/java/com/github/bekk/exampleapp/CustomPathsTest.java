package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.SEND_WELCOME_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

import no.bekk.dbscheduler.ui.model.ConfigResponse;
import no.bekk.dbscheduler.ui.model.GetTasksResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("custompaths")
@SpringBootTest(
    classes = {ExampleApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class CustomPathsTest {

  @LocalServerPort private Integer serverPort;

  private String baseUrl;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void testTaskApiWithCustomPath() {
    ResponseEntity<GetTasksResponse> result =
        this.restTemplate.getForEntity(
            baseUrl
                + "/my-custom-api/tasks/all?filter=ALL&pageNumber=0&size=10&sorting=DEFAULT&asc=true&searchTerm="
                + SEND_WELCOME_EMAIL.getTaskName(),
            GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getItems()).isNotEmpty();
    assertThat(result.getBody().getItems())
        .anyMatch(taskModel -> taskModel.getTaskName().equals(SEND_WELCOME_EMAIL.getTaskName()));
  }

  @Test
  void testDeleteTaskWithCustomPath() {
    ResponseEntity<Void> result =
        restTemplate.postForEntity(
            baseUrl
                + "/my-custom-api/tasks/delete?id=%s&name=%s"
                    .formatted("delete-1", SEND_WELCOME_EMAIL.getTaskName()),
            null,
            Void.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testUiPageWithCustomPath() {
    ResponseEntity<String> result =
        this.restTemplate.getForEntity(baseUrl + "/my-custom-ui", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getHeaders().getContentType()).isNotNull();
    assertThat(result.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
    assertThat(result.getBody()).contains("/my-custom-ui");
  }

  @Test
  void testUnknownSpaRouteServesIndexHtml() {
    ResponseEntity<String> result =
        this.restTemplate.getForEntity(baseUrl + "/my-custom-ui/some-spa-route", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).contains("/my-custom-ui");
    assertThat(result.getHeaders().getContentType()).isNotNull();
    assertThat(result.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
  }

  @Test
  void testContextPathJsContainsCustomPaths() {
    ResponseEntity<String> result =
        this.restTemplate.getForEntity(baseUrl + "/my-custom-ui/js/context-path.js", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).contains("window.CONTEXT_PATH=");
    assertThat(result.getBody()).contains("window.DB_SCHEDULER_UI = {");
    assertThat(result.getBody()).contains("routePath: '/my-custom-ui'");
    assertThat(result.getBody()).contains("uiBasePath: '/my-custom-ui'");
    assertThat(result.getBody()).contains("apiBasePath: '/my-custom-api'");
  }

  @Test
  void testConfigEndpointHasCustomPaths() {
    ResponseEntity<ConfigResponse> result =
        this.restTemplate.getForEntity(baseUrl + "/my-custom-api/config", ConfigResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getUiPath()).isEqualTo("/my-custom-ui");
    assertThat(result.getBody().getApiPath()).isEqualTo("/my-custom-api");
  }

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }
}
