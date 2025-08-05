package com.chua.starter.monitor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 服务器指标WebSocket配置
 * 由于定时任务已移至task包，此配置类不再需要启动/停止服务
 *
 * @author CH
 * @since 2024/12/20
 */
@Configuration
@Slf4j
public class ServerMetricsWebSocketConfiguration implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("服务器指标WebSocket推送服务配置已加载，定时任务由task包管理");
    }
}
