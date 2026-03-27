package com.chua.starter.payment.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 支付控制台静态页入口。
 */
@Controller
@ConditionalOnProperty(prefix = "plugin.payment.ops.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentConsoleController {

    @GetMapping({"/payment-console", "/payment-console/"})
    public String index() {
        return "redirect:/payment-console/index.html";
    }

    @GetMapping({"/payment-console/login", "/payment-console/login/"})
    public String login() {
        return "redirect:/payment-console/login.html";
    }
}
