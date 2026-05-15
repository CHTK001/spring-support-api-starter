package com.chua.starter.rustserver.configuration;

import com.chua.starter.rustserver.RustServletWebServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Rust WebServer 自动配置。
 *
 * <p>在 application.yml 中设置 {@code server.rust.enabled=true} 即可启用。</p>
 *
 * <pre>{@code
 * server:
 *   port: 8080
 *   rust:
 *     enabled: true
 *     response-buffer: 524288   # 可选，单位字节，默认 512KB
 * }</pre>
 */
@AutoConfiguration(before = ServletWebServerFactoryAutoConfiguration.class)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServlet")
@ConditionalOnProperty(prefix = "server.rust", name = "enabled", havingValue = "true")
public class RustServerAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RustServerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(ServletWebServerFactory.class)
    public RustServletWebServerFactory rustServletWebServerFactory(Environment env) {
        RustServletWebServerFactory factory = new RustServletWebServerFactory();

        // 端口
        String portStr = env.getProperty("server.port", "8080");
        factory.setPort(Integer.parseInt(portStr));

        // 响应缓冲区大小
        String bufStr = env.getProperty("server.rust.response-buffer", "524288");
        factory.setResponseCapacity(Integer.parseInt(bufStr));

        log.info("[RustServer] Auto-configuration enabled: port={}, responseBuffer={}",
                portStr, bufStr);

        return factory;
    }
}
