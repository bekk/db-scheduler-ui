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
package no.bekk.dbscheduler.uimicronaut.error;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import no.bekk.dbscheduler.ui.exception.DbSchedulerUiNotFoundException;

/** Maps the framework-neutral {@link DbSchedulerUiNotFoundException} to an HTTP 404. */
@Produces
@Singleton
@Requires(classes = {DbSchedulerUiNotFoundException.class, ExceptionHandler.class})
public class NotFoundExceptionHandler
    implements ExceptionHandler<DbSchedulerUiNotFoundException, HttpResponse<?>> {

  @Override
  public HttpResponse<?> handle(HttpRequest request, DbSchedulerUiNotFoundException exception) {
    return HttpResponse.notFound(exception.getMessage());
  }
}
