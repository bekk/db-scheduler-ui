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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexHtmlController {

  private final String patchedIndexHtml;

  public IndexHtmlController(@Qualifier("indexHtml") String indexHtml) {
    this.patchedIndexHtml = indexHtml;
  }

  @GetMapping(
      path = {
        "/db-scheduler/index.html",
        "/db-scheduler",
        "/db-scheduler/history/all/index.html",
        "/db-scheduler/history/all"
      },
      produces = MediaType.TEXT_HTML_VALUE)
  public String indexHtml() {
    return patchedIndexHtml;
  }
}
