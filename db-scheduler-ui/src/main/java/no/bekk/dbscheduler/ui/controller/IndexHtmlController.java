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
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexHtmlController {

  private final String patchedIndexHtml;

  public IndexHtmlController(String contextPath) throws IOException {
    this.patchedIndexHtml = contextPathAwareIndexHtml(contextPath);
  }

  @GetMapping(
      path = {"/db-scheduler/index.html", "/db-scheduler"},
      produces = MediaType.TEXT_HTML_VALUE)
  String indexHtml() {
    return patchedIndexHtml;
  }

  private static String contextPathAwareIndexHtml(String contextPath) throws IOException {
    String indexHtml =
        new ClassPathResource(SpaFallbackMvc.DEFAULT_STARTING_PAGE)
            .getContentAsString(StandardCharsets.UTF_8);

    return indexHtml
        .replaceAll("/db-scheduler", contextPath + "/db-scheduler")
        .replaceAll(
            "<head>",
            """
                <head>
                    <script>
                      window.CONTEXT_PATH = '%s';
                    </script>"""
                .formatted(contextPath));
  }
}
