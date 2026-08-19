package com.chua.starter.scheduler.controller;

import com.chua.common.support.annotation.Ignore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 调度平台控制台入口。
 * <p>
 * 控制台本身使用模块内置的静态页面实现，这里只负责给出一个稳定入口，
 * 避免调用方必须记住具体的静态资源文件路径。
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Controller
@Ignore
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerJobConsoleController {

    /**
     * 控制台首页入口, 重定向到静态首页。
     *
     * @return 重定向地址
     */
    @GetMapping({"/job-console", "/job-console/"})
    public String index() {
        return "redirect:/job-console/index.html";
    }

    /**
     * 控制台登录页入口, 重定向到静态登录页。
     *
     * @return 重定向地址
     */
    @GetMapping({"/job-console/login", "/job-console/login/"})
    public String login() {
        return "redirect:/job-console/login.html";
    }
}
