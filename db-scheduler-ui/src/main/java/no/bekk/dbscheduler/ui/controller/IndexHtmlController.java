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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${db-scheduler-ui.ui-path:/db-scheduler}")
public class IndexHtmlController {

  private final String patchedIndexHtml;
  private final String contextPath;
  private final String routePath;
  private final String uiBasePath;
  private final String apiBasePath;

  public IndexHtmlController(
      @Qualifier("indexHtml") String indexHtml,
      @Qualifier("contextPath") String contextPath,
      String routePath,
      String uiBasePath,
      String apiBasePath) {
    this.patchedIndexHtml = indexHtml;
    this.contextPath = contextPath;
    this.routePath = routePath;
    this.uiBasePath = uiBasePath;
    this.apiBasePath = apiBasePath;
  }

  @GetMapping(
      path = {"/index.html", "", "/history/all/index.html", "/history/all"},
      produces = MediaType.TEXT_HTML_VALUE)
  public String indexHtml() {
    return patchedIndexHtml;
  }

  @GetMapping(
      path = {"/js/context-path.js"},
      produces = "text/javascript")
  public String contextPath() {
    return "window.CONTEXT_PATH='"
        + javaScriptString(contextPath)
        + "';\n"
        + "window.DB_SCHEDULER_UI = {\n"
        + "  contextPath: '"
        + javaScriptString(contextPath)
        + "',\n"
        + "  routePath: '"
        + javaScriptString(routePath)
        + "',\n"
        + "  uiBasePath: '"
        + javaScriptString(uiBasePath)
        + "',\n"
        + "  apiBasePath: '"
        + javaScriptString(apiBasePath)
        + "'\n"
        + "};";
  }

  private static String javaScriptString(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }
}
