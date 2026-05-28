package com.chua.starter.datalake.configuration;

import com.chua.starter.datalake.server.DatalakeApiServer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 数据中台 API 服务器自动装配 — 启动 DatalakeApiServer (ProtocolServer on 58081)
 *
 * @author CH
 * @since 2025/9/9 10:39
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(name = "datalake.enabled", havingValue = "true", matchIfMissing = false)
public class DatalakeAutoConfiguration {

    private DatalakeApiServer datalakeApiServer;

    @Bean
    public DatalakeApiServer datalakeApiServer() {
        this.datalakeApiServer = new DatalakeApiServer();
        new Thread(() -> {
            try {
                datalakeApiServer.start();
                log.info("[DatalakeAutoConfiguration] DatalakeApiServer 启动完成 (port: 58081, context: /v2/datalake)");
            } catch (Exception e) {
                log.error("[DatalakeAutoConfiguration] DatalakeApiServer 启动失败", e);
            }
        }, "datalake-api-server").start();
        return datalakeApiServer;
    }

    @PreDestroy
    public void destroy() {
        if (datalakeApiServer != null) {
            datalakeApiServer.stop();
            log.info("[DatalakeAutoConfiguration] DatalakeApiServer 已停止");
        }
    }
}
