package com.chua.starter.service.configuration;

import com.chua.datalake.support.server.DatalakeServer;
import com.chua.datalake.support.server.DatalakeServerBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 数据中台自动装配 — 使用 DatalakeServerBuilder 构建完整服务器
 *
 * @author CH
 * @since 2025/6/2
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(DatalakeServerBuilder.class)
@ConditionalOnProperty(name = "datalake.enabled", havingValue = "true", matchIfMissing = false)
public class DatalakeAutoConfiguration {

    private DatalakeServer datalakeServer;

    @Bean
    public DatalakeServer datalakeServer() {
        this.datalakeServer = new DatalakeServerBuilder()
                .enableAutoDiscover(true)
                .build();
        new Thread(() -> {
            try {
                datalakeServer.start();
                log.info("[DatalakeAutoConfiguration] 数据中台启动完成 (API:58081, WS:58082, Collect:58080)");
            } catch (Exception e) {
                log.error("[DatalakeAutoConfiguration] 数据中台启动失败", e);
            }
        }, "datalake-server").start();
        return datalakeServer;
    }

    @PreDestroy
    public void destroy() {
        if (datalakeServer != null) {
            datalakeServer.stop();
            log.info("[DatalakeAutoConfiguration] 数据中台已停止");
        }
    }
}
