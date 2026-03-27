package com.chua.starter.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 支付轻量控制台配置。
 */
@Data
@ConfigurationProperties(prefix = "plugin.payment.ops.console")
public class PaymentConsoleProperties {

    /**
     * 是否启用模块内置控制台。
     */
    private boolean enabled = true;

    /**
     * 是否启用账号密码校验。
     */
    private boolean authEnabled = true;

    /**
     * 控制台用户名。
     */
    private String username = "admin";

    /**
     * 控制台密码。
     */
    private String password = "admin123456";
}
