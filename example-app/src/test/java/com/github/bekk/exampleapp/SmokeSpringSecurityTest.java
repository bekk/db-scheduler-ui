package com.github.bekk.exampleapp;

import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.ONE_TIME_TASK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.springframework.security.config.Customizer.withDefaults;

import com.github.bekk.exampleapp.SmokeSpringSecurityTest.TestConfig;
import java.util.function.Supplier;
import no.bekk.dbscheduler.ui.controller.ConfigController;
import no.bekk.dbscheduler.ui.model.ConfigResponse;
import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import no.bekk.dbscheduler.uistarter.config.DbSchedulerUiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ExampleApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
class SmokeSpringSecurityTest {

  @LocalServerPort private int serverPort;

  @Autowired private ApplicationContext context;

  @Autowired private TestRestTemplate restTemplate;

  private String baseUrl;
  private TestRestTemplate userRestTemplate;
  private TestRestTemplate adminRestTemplate;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + serverPort;
    userRestTemplate = restTemplate.withBasicAuth("user", "user");
    adminRestTemplate = restTemplate.withBasicAuth("admin", "admin");
  }

  @Configuration
  @EnableWebSecurity
  static class TestConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http.csrf(CsrfConfigurer::disable)
          .authorizeHttpRequests(
              authz ->
                  authz
                      // protect the UI
                      .requestMatchers("/db-scheduler/**")
                      .hasAnyRole("ADMIN", "USER")
                      // allow read access to the API for both users and admins
                      .requestMatchers(HttpMethod.GET, "/db-scheduler-api/**")
                      .hasAnyRole("ADMIN", "USER")
                      // only admins can delete tasks, alter scheduling, ...
                      .requestMatchers(HttpMethod.POST, "/db-scheduler-api/**")
                      .hasAnyRole("ADMIN")
                      // other application specific security
                      .anyRequest()
                      .permitAll())
          .httpBasic(withDefaults())
          .build();
    }

    @Bean
    UserDetailsManager userDetailsService() {
      UserDetails user = User.withUsername("user").password("{noop}user").roles("USER").build();
      UserDetails admin = User.withUsername("admin").password("{noop}admin").roles("ADMIN").build();
      return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
      AuthenticationManagerBuilder authenticationManagerBuilder =
          http.getSharedObject(AuthenticationManagerBuilder.class);
      authenticationManagerBuilder.userDetailsService(userDetailsService());
      return authenticationManagerBuilder.build();
    }

    @Bean
    ConfigController configController(DbSchedulerUiProperties properties) {
      return new ConfigController(properties.isHistory(), readOnly(properties));
    }

    private Supplier<Boolean> readOnly(DbSchedulerUiProperties properties) {
      return () -> properties.isReadOnly() || !isAdmin();
    }

    private boolean isAdmin() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null) {
        return false;
      }
      return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
  }

  @Test
  void testContextLoads() {
    assertThat(context.containsBean("taskAdminController")).isTrue();
    assertThat(context.containsBean("taskController")).isTrue();
  }

  @Test
  void accessingUiWithoutCredentialsIsUnauthorized() {
    ResponseEntity<String> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void accessingUiWorksForUser() {
    ResponseEntity<String> result =
        userRestTemplate.getForEntity(baseUrl + "/db-scheduler", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).contains("<title>DB Scheduler UI</title>");
  }

  @Test
  void accessingUiWorksForAdmin() {
    ResponseEntity<String> result =
        adminRestTemplate.getForEntity(baseUrl + "/db-scheduler", String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).contains("<title>DB Scheduler UI</title>");
  }

  @Test
  void readingTasksWithoutCredentialsIsUnauthorized() {
    ResponseEntity<GetTasksResponse> result =
        restTemplate.getForEntity(baseUrl + "/db-scheduler-api/tasks/all", GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void readingTasksWorksForUser() {
    ResponseEntity<GetTasksResponse> result =
        userRestTemplate.getForEntity(
            baseUrl + "/db-scheduler-api/tasks/all", GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void readingTasksWorksForAdmin() {
    ResponseEntity<GetTasksResponse> result =
        adminRestTemplate.getForEntity(
            baseUrl + "/db-scheduler-api/tasks/all", GetTasksResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void deletingTasksIsForbiddenForUser() {
    ResponseEntity<Void> result =
        userRestTemplate.postForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/delete?id=%s&name=%s"
                    .formatted("delete-2", ONE_TIME_TASK.getTaskName()),
            null,
            Void.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void deletingTasksWorksForAdmin() {
    ResponseEntity<Void> result =
        adminRestTemplate.postForEntity(
            baseUrl
                + "/db-scheduler-api/tasks/delete?id=%s&name=%s"
                    .formatted("delete-2", ONE_TIME_TASK.getTaskName()),
            null,
            Void.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void configIndicatesReadOnlyForUser() {
    ResponseEntity<ConfigResponse> result =
        userRestTemplate.getForEntity(baseUrl + "/db-scheduler-api/config", ConfigResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody())
        .isNotNull()
        .extracting(ConfigResponse::isReadOnly, BOOLEAN)
        .isTrue();
  }

  @Test
  void configIndicatesNotReadOnlyForAdmin() {
    ResponseEntity<ConfigResponse> result =
        adminRestTemplate.getForEntity(baseUrl + "/db-scheduler-api/config", ConfigResponse.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody())
        .isNotNull()
        .extracting(ConfigResponse::isReadOnly, BOOLEAN)
        .isFalse();
  }
}
