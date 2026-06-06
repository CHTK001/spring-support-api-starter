package com.chua.starter.ai.test;

import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.panel.cache.PanelSshSessionRegistry;
import com.chua.starter.panel.controller.PanelDockerController;
import com.chua.starter.panel.controller.PanelSshController;
import com.chua.starter.panel.service.DockerPanelService;
import com.chua.starter.panel.service.NginxPanelService;
import com.chua.starter.panel.service.impl.DefaultDockerPanelService;
import com.chua.starter.panel.service.impl.DefaultNginxPanelService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 独立 AI 联调测试后端。
 */
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.ai.test",
        "com.chua.starter.system",
        "com.chua.starter.spider",
        "com.chua.starter.docker",
        "com.chua.starter.server.support"
})
public class AiTestApplication {

    public static void main(String[] args) {
        // AI 测试环境默认走明文，避免因缺少加密密钥导致启动失败
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "ai-test");
        }
        SpringApplication.run(AiTestApplication.class, args);
    }

    @Bean
    public AuthClientProperties authClientProperties() {
        AuthClientProperties properties = new AuthClientProperties();
        properties.setEnable(false);
        properties.setAddress("http://localhost:58084");
        properties.setLoginAddress("");
        properties.setProtocol("static");
        properties.setWhitelist(java.util.List.of("/v2/**"));
        properties.setTemp(new AuthClientProperties.TempUser());
        return properties;
    }

    @Bean
    public PanelSshSessionRegistry panelSshSessionRegistry() {
        return new PanelSshSessionRegistry();
    }

    @Bean
    public NginxPanelService nginxPanelService(PanelSshSessionRegistry panelSshSessionRegistry) {
        return new DefaultNginxPanelService(panelSshSessionRegistry);
    }

    @Bean
    public DockerPanelService dockerPanelService() {
        return new DefaultDockerPanelService();
    }

    @Bean
    public PanelSshController panelSshController(NginxPanelService nginxPanelService) {
        return new PanelSshController(nginxPanelService);
    }

    @Bean
    public PanelDockerController panelDockerController(DockerPanelService dockerPanelService) {
        return new PanelDockerController(dockerPanelService);
    }
}
