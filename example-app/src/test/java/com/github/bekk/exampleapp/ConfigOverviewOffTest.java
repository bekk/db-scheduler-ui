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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    properties = "db-scheduler-ui.overview=false",
    classes = ExampleApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
class ConfigOverviewOffTest {

  @LocalServerPort private int serverPort;

  private String baseUrl;

  @Autowired private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
  }

  @Test
  void configReportsShowOverviewFalseWhenFlagOff() {
    ResponseEntity<ConfigResponse> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler-api/config", ConfigResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().isShowOverview()).isFalse();
  }
}
