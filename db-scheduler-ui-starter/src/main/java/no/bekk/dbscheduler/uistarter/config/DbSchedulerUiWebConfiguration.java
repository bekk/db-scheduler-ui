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
package no.bekk.dbscheduler.uistarter.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class DbSchedulerUiWebConfiguration implements WebMvcRegistrations {

  private final String apiPrefix;

  public DbSchedulerUiWebConfiguration(String apiPrefix) {
    this.apiPrefix = apiPrefix;
  }

  @Override
  public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
    return new PrefixedRequestMappingHandlerMapping(apiPrefix);
  }
}
