package com.github.bekk.dbscheduleruiapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@CrossOrigin
@RequestMapping("/db-scheduler")
public class UIController {
    @RequestMapping("/**")
    public String forwardToIndex() {

        System.out.println("FORWARDING");
        return "forward:/db-scheduler-ui/index.html";
    }
}
