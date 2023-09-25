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
package no.bekk.dbscheduler.ui.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GetLogsResponse {
  private final int numberOfItems;
  private final int numberOfPages;
  private final List<LogModel> items;

  @JsonCreator
  public GetLogsResponse(
      @JsonProperty("numberOfItems") int totalLogs,
      @JsonProperty("items") List<LogModel> pagedLogs,
      @JsonProperty("pageSize") int pageSize) {
    this.numberOfItems = totalLogs;
    this.numberOfPages = totalLogs == 0 ? 0 : (int) Math.ceil((double) totalLogs / pageSize);
    this.items = pagedLogs;
  }

  public int getNumberOfItems() {
    return numberOfItems;
  }

  public int getNumberOfPages() {
    return numberOfPages;
  }

  public List<LogModel> getItems() {
    return items;
  }
}
