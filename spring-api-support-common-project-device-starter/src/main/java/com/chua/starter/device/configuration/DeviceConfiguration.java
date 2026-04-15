package com.chua.starter.device.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 独立设备模块自动配置。
 *
 * @author CH
 * @since 2026/3/26
 */
@AutoConfiguration
@ComponentScan("com.chua.starter.device")
public class DeviceConfiguration {
}
