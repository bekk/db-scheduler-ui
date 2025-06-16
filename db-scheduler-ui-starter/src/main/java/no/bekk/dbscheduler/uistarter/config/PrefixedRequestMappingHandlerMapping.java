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

import java.lang.reflect.Method;
import no.bekk.dbscheduler.ui.controller.ConfigController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

public class PrefixedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

  private final String prefix;

  public PrefixedRequestMappingHandlerMapping(String prefix) {
    this.prefix = prefix;
    setPatternParser(new PathPatternParser());
  }

  @Override
  protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
    final Package aPackage = method.getDeclaringClass().getPackage();
    RequestMappingInfo finalMapping = mapping;

    if (ConfigController.class.getPackage().equals(aPackage)) {
      finalMapping = RequestMappingInfo
          .paths(prefix)
          .build()
          .combine(mapping);
    }
    super.registerHandlerMethod(handler, method, finalMapping);
  }
}
