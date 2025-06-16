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
package no.bekk.dbscheduler.ui.controller;

import java.io.IOException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

public class SpaFallbackMvc implements WebMvcConfigurer {

  public static final String DEFAULT_STARTING_PAGE = "static/db-scheduler/index.html";

  private final String prefix;

  public SpaFallbackMvc(@Value("${db-scheduler-ui.context-path}") String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler(prefix + "/db-scheduler", prefix + "/db-scheduler/**")
        .addResourceLocations("classpath:/static/db-scheduler/")
        .resourceChain(true)
        .addResolver(new SpaFallbackResolver());
  }

  static class SpaFallbackResolver extends PathResourceResolver {

    @Override
    protected Resource getResource(@NonNull String resourcePath, Resource location)
        throws IOException {
      var requestedResource = location.createRelative(resourcePath);

      if (requestedResource.exists() && requestedResource.isReadable()) {
        return requestedResource;
      }

      return new ClassPathResource(DEFAULT_STARTING_PAGE);
    }
  }
}
