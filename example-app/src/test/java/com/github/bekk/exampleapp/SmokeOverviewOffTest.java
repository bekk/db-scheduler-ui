package com.github.bekk.exampleapp;

import static org.assertj.core.api.Assertions.assertThat;

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
    properties = "db-scheduler-ui.overview=false",
    classes = ExampleApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class SmokeOverviewOffTest {

  @LocalServerPort private int serverPort;

  @Autowired private ApplicationContext context;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void taskPageBeansAreAbsent() {
    assertThat(context.containsBean("executionPager")).isFalse();
    assertThat(context.containsBean("executionAdminController")).isFalse();
  }

  @Test
  void executionsEndpointIsAbsent() {
    ResponseEntity<String> result =
        restTemplate.getForEntity(
            "http://localhost:" + serverPort + "/db-scheduler-api/tasks/executions?taskName=x",
            String.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
