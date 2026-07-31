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
package no.bekk.dbscheduler.uimicronaut.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.server.types.files.StreamedFile;
import jakarta.inject.Named;
import java.net.URL;

/**
 * Serves the bundled single-page app: the patched {@code index.html}, the generated {@code
 * context-path.js}, the static asset files from the classpath, and an SPA fallback that returns
 * {@code index.html} for unknown client-side routes — mirroring the Spring {@code SpaFallbackMvc}
 * resource resolver.
 */
@Controller("/db-scheduler")
public class MicronautIndexHtmlController {

  private static final String STATIC_ROOT = "static/db-scheduler/";

  private final String patchedIndexHtml;
  private final String contextPath;

  public MicronautIndexHtmlController(
      @Named("indexHtml") String indexHtml, @Named("contextPath") String contextPath) {
    this.patchedIndexHtml = indexHtml;
    this.contextPath = contextPath;
  }

  @Get(produces = MediaType.TEXT_HTML)
  public String index() {
    return patchedIndexHtml;
  }

  @Get(value = "/js/context-path.js", produces = "text/javascript")
  public String contextPath() {
    return "window.CONTEXT_PATH='" + contextPath + "';";
  }

  @Get("/{+path}")
  public HttpResponse<?> asset(String path) {
    URL resource = getClass().getClassLoader().getResource(STATIC_ROOT + path);
    if (resource != null) {
      return HttpResponse.ok(new StreamedFile(resource));
    }
    return HttpResponse.ok(patchedIndexHtml).contentType(MediaType.TEXT_HTML);
  }
}
