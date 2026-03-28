package com.chua.starter.monitor.controller.spider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Spider 轻页面入口重定向。
 */
@Controller
public class SpiderConsoleController {

    @GetMapping({"/spider-console", "/spider-console/"})
    public String index() {
        return "redirect:/spider-console/index.html";
    }

    @GetMapping({"/spider-console/login", "/spider-console/login/"})
    public String login() {
        return "redirect:/spider-console/login.html";
    }
}
