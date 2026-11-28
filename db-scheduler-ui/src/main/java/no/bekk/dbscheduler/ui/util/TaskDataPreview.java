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
package no.bekk.dbscheduler.ui.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** A one-line glimpse of an execution's task data, for lists that cannot show the payload. */
public final class TaskDataPreview {

  static final int MAX_LENGTH = 100;

  private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

  private TaskDataPreview() {}

  /** Compact JSON of {@code data}, cut to {@value #MAX_LENGTH} characters; null when hidden. */
  public static String of(Object data, boolean show) {
    if (!show || data == null) {
      return null;
    }
    String text;
    try {
      text = MAPPER.writeValueAsString(data);
    } catch (JsonProcessingException | RuntimeException e) {
      text = String.valueOf(data);
    }
    return text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) + "…" : text;
  }
}
