package com.github.bekk.dbscheduleruiapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@CrossOrigin
@RequestMapping("/jobjuggler")
public class UIController {
    @RequestMapping("/**")
    public String forwardToIndex() {
        return "forward:/db-scheduler-ui/index.html";
    }
}
