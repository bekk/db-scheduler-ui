package com.github.bekk.exampleapp;

import static org.assertj.core.api.Assertions.assertThat;

import no.bekk.dbscheduler.ui.model.ConfigResponse;
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
import org.springframework.test.context.TestPropertySource;

/** With {@code db-scheduler-ui.overview=false}, the Overview path must be entirely absent. */
@SpringBootTest(
    classes = {ExampleApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@TestPropertySource(properties = "db-scheduler-ui.overview=false")
class OverviewDisabledTest {

  @LocalServerPort private Integer serverPort;
  private String baseUrl;

  @Autowired private ApplicationContext context;
  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }

  @Test
  void overviewBeansAreAbsentWhenFlagOff() {
    assertThat(context.containsBean("overviewController")).isFalse();
    assertThat(context.containsBean("overviewLogic")).isFalse();
  }

  @Test
  void overviewEndpointIsNotFound() {
    ResponseEntity<String> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler-api/tasks/overview", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void configExposesShowOverviewFalse() {
    ResponseEntity<ConfigResponse> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler-api/config", ConfigResponse.class);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().isShowOverview()).isFalse();
  }
}
