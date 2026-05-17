package com.chua.starter.rustserver.configuration;

import com.chua.starter.rustserver.RustServletWebServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Rust WebServer 自动配置。
 *
 * <p>在 application.yml 中设置 {@code plugin.server.rust.enabled=true} 即可启用。</p>
 *
 * <pre>{@code
 * plugin:
 *   server:
 *     rust:
 *       enabled: true
 *       port: 8080
 *       response-buffer: 524288
 * }</pre>
 *
 * <p>当 {@code plugin.server.rust.port} 未设置时，回退使用 {@code server.port}（默认 8080）。</p>
 */
@AutoConfiguration(before = ServletWebServerFactoryAutoConfiguration.class)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServlet")
@ConditionalOnProperty(prefix = RustServerProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RustServerProperties.class)
public class RustServerAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RustServerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(ServletWebServerFactory.class)
    public RustServletWebServerFactory rustServletWebServerFactory(
            Environment env, RustServerProperties properties) {

        int port = properties.getPort();
        String serverPort = env.getProperty("server.port");
        if (serverPort != null) {
            port = Integer.parseInt(serverPort);
        }

        int responseBuffer = properties.getResponseBuffer();
        String bufStr = env.getProperty("server.rust.response-buffer");
        if (bufStr != null) {
            responseBuffer = Integer.parseInt(bufStr);
        }

        RustServletWebServerFactory factory = new RustServletWebServerFactory();
        factory.setPort(port);
        factory.setResponseCapacity(responseBuffer);
        factory.setWorkerThreads(properties.getWorkerThreads());
        factory.setSessionTimeout(properties.getSessionTimeout());
        factory.setSessionCookieName(properties.getSessionCookieName());
        factory.setShutdownTimeout(properties.getShutdownTimeout());
        factory.setAccessLogEnabled(properties.isAccessLogEnabled());
        if (properties.getSsl() != null) {
            factory.setSslEnabled(properties.getSsl().isEnabled());
            factory.setSslCertPath(properties.getSsl().getCertPath());
            factory.setSslKeyPath(properties.getSsl().getKeyPath());
        }

        log.info("[RustServer] Auto-configuration enabled: port={}, responseBuffer={}, workerThreads={}, sessionTimeout={}s",
                port, responseBuffer, properties.getWorkerThreads(), properties.getSessionTimeout());

        return factory;
    }
}
