package com.chua.starter.monitor.configuration;

import com.chua.starter.ai.support.configuration.RagProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 ai-starter 的 RagProperties bean，
 * 供 AiConfiguration 组件扫描加载的 RagController 使用。
 */
@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class AiStarterPropertiesConfiguration {
}
