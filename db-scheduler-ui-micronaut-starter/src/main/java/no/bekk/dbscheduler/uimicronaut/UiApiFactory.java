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
package no.bekk.dbscheduler.uimicronaut;

import static no.bekk.dbscheduler.ui.util.DbSchedulerUiUtil.normalizePaths;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import no.bekk.dbscheduler.ui.service.OverviewService;
import no.bekk.dbscheduler.ui.service.TaskLogic;
import no.bekk.dbscheduler.ui.util.Caching;

/**
 * Wires the framework-neutral db-scheduler-ui services as Micronaut beans, mirroring the in-scope
 * beans of the Spring starter's {@code UiApiAutoConfiguration}. The {@link Scheduler} and the task
 * definitions ({@code List<Task<?>>}) must be provided by the consuming application — db-scheduler
 * has no Micronaut integration, so there is no auto-configured scheduler to consume.
 */
@Factory
@Requires(property = "db-scheduler-ui.enabled", notEquals = "false")
public class UiApiFactory {

  private static final String INDEX_HTML = "static/db-scheduler/index.html";

  @Singleton
  Caching caching() {
    return new Caching();
  }

  @Singleton
  TaskLogic taskLogic(Scheduler scheduler, Caching caching, MicronautUiProperties properties) {
    return new TaskLogic(scheduler, caching, properties.isTaskData());
  }

  @Singleton
  @Requires(property = "db-scheduler-ui.overview", value = "true")
  OverviewService overviewService(Scheduler scheduler, List<Task<?>> taskDefinitions) {
    return new OverviewService(scheduler, taskDefinitions);
  }

  @Singleton
  @Named("contextPath")
  String contextPath(MicronautUiProperties properties) {
    return normalizePaths(properties.getContextPath());
  }

  @Singleton
  @Named("indexHtml")
  String indexHtml(@Named("contextPath") String contextPath) {
    String indexHtml = readIndexHtml();
    String contextPathScript = contextPath + "/db-scheduler/js/context-path.js";
    return indexHtml
        .replaceAll("/db-scheduler", contextPath + "/db-scheduler")
        .replaceAll(
            "<head>",
            """
            <head>
                <script src='%s'></script>"""
                .formatted(contextPathScript));
  }

  private String readIndexHtml() {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(INDEX_HTML)) {
      if (in == null) {
        throw new IllegalStateException("Could not find " + INDEX_HTML + " on the classpath");
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
