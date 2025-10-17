package com.chua.starter.monitor.properties;

import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ACME 配置
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "monitor.acme")
@ApiModel(description = "ACME 配置")
@Schema(description = "ACME 配置")
public class AcmeProperties {

    /** 是否启用 */
    private boolean enabled = true;

    /** 到期前多少天触发续签 */
    private int renewDaysBefore = 30;
}
