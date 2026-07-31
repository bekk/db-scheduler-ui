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

import com.github.kagkarlsson.scheduler.Scheduler;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Starts the scheduler once the HTTP server is up and seeds a couple of pending executions so the
 * dashboard (and the smoke test) have data to show. Instances are scheduled in the future so they
 * stay {@code SCHEDULED} rather than being consumed before the smoke test runs.
 */
@Singleton
public class SchedulerStartup implements ApplicationEventListener<ServerStartupEvent> {

  private final Scheduler scheduler;

  public SchedulerStartup(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public void onApplicationEvent(ServerStartupEvent event) {
    scheduler.start();

    Instant future = Instant.now().plus(1, ChronoUnit.HOURS);
    scheduler.schedule(
        SEND_WELCOME_EMAIL
            .instance("user-10472")
            .data(new NewSignup(10472, "ole.nordman@mail.com", "Ole Nordman"))
            .build(),
        future);
    scheduler.schedule(
        SEND_WELCOME_EMAIL
            .instance("delete-1")
            .data(new NewSignup(20231, "kari.nordmann@mail.com", "Kari Nordmann"))
            .build(),
        future);
  }
}
