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
 * 数据中台自动装配 — 使用 数据湖服务端构建器 构建完整服务器
 *
 * @since 2025/6/2
 * @author CH
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(DatalakeServerBuilder.class)
@ConditionalOnProperty(name = "datalake.enabled", havingValue = "true", matchIfMissing = false)
public class DatalakeAutoConfiguration {

    /**
     * 数据湖服务器
    */
    private DatalakeServer datalakeServer;

    @Bean
    /**
     * 数据湖服务端
     *
     * @return 数据湖服务端的结果
     */
    public DatalakeServer datalakeServer() {
        this.datalakeServer = DatalakeServerBuilder.builder()
                .build();
        // 同步启动：交给裸线程只会把启动失败降级成一条 error 日志，Spring 上下文仍宣告健康
        datalakeServer.start();
        log.info("[DatalakeAutoConfiguration] 数据中台已启动（本装配未注入 API 服务端，不开设任何 HTTP 端口）");
        return datalakeServer;
    }

    @PreDestroy
    /**
     * 销毁
    */
    public void destroy() {
        if (datalakeServer != null) {
            datalakeServer.stop();
            log.info("[DatalakeAutoConfiguration] 数据中台已停止");
        }
    }
}
