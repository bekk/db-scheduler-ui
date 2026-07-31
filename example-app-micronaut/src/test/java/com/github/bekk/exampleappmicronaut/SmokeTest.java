/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.bekk.exampleappmicronaut;

import static com.github.bekk.exampleappmicronaut.SchedulerConfiguration.SEND_WELCOME_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import org.junit.jupiter.api.Test;

@MicronautTest
class SmokeTest {

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void getTasksReturnsWelcomeTask() {
    HttpResponse<GetTasksResponse> result =
        client
            .toBlocking()
            .exchange(
                HttpRequest.GET(
                    "/db-scheduler-api/tasks/all?filter=ALL&pageNumber=0&size=10&sorting=DEFAULT&asc=true&searchTermTaskName="
                        + SEND_WELCOME_EMAIL.getTaskName()),
                GetTasksResponse.class);

    assertThat(result.getStatus().getCode()).isEqualTo(200);
    assertThat(result.body()).isNotNull();
    assertThat(result.body().getItems()).isNotEmpty();
    assertThat(result.body().getItems())
        .anyMatch(task -> task.getTaskName().equals(SEND_WELCOME_EMAIL.getTaskName()));
  }

  @Test
  void getOverviewReturnsWelcomeTask() {
    HttpResponse<List<OverviewTask>> result =
        client
            .toBlocking()
            .exchange(
                HttpRequest.GET("/db-scheduler-api/tasks/overview"),
                Argument.listOf(OverviewTask.class));

    assertThat(result.getStatus().getCode()).isEqualTo(200);
    assertThat(result.body())
        .anyMatch(task -> task.taskName().equals(SEND_WELCOME_EMAIL.getTaskName()));
  }

  @Test
  void deletingTaskReturnsStatusOK() {
    HttpResponse<?> result =
        client
            .toBlocking()
            .exchange(
                HttpRequest.POST(
                    "/db-scheduler-api/tasks/delete?id=delete-1&name="
                        + SEND_WELCOME_EMAIL.getTaskName(),
                    null));

    assertThat(result.getStatus().getCode()).isEqualTo(200);
  }

  @Test
  void unknownSpaRouteServesIndexHtml() {
    HttpResponse<String> result =
        client.toBlocking().exchange(HttpRequest.GET("/db-scheduler/some-spa-route"), String.class);

    assertThat(result.getStatus().getCode()).isEqualTo(200);
    assertThat(result.body()).contains("/db-scheduler");
    assertThat(result.getContentType()).hasValue(MediaType.TEXT_HTML_TYPE);
  }
}
