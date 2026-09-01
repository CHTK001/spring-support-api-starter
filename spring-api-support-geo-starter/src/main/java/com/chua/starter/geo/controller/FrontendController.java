package com.chua.starter.geo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端页面路由，所有未匹配的请求重定向到 index.html（SPA 支持）。
 *
 * @author CH
 * @since 1.0.0
 */
@Controller
public class FrontendController {

    /**
     * 根路径重定向到 index.html。
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
