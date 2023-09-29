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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@CrossOrigin
@RequestMapping("/db-scheduler")
public class UIController {

  public UIController(boolean showTaskData, boolean showHistory, ResourceLoader resourceLoader) {
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("showTaskData", showTaskData);
    jsonMap.put("showHistory", showHistory);

    ObjectMapper objectMapper = new ObjectMapper();
    Resource resource = resourceLoader.getResource("classpath:static/db-scheduler-ui/config.json");
    try {
      File file = resource.getFile();
      objectMapper.writeValue(file, jsonMap);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @RequestMapping("/**")
  public String forwardToIndex() {
    return "forward:/db-scheduler-ui/index.html";
  }
}
