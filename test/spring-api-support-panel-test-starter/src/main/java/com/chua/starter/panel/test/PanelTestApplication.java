package com.chua.starter.panel.test;

import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.workflow.configuration.WorkflowAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 独立 Panel 联调测试后端。
 */
@SpringBootApplication
@EnableConfigurationProperties(AuthClientProperties.class)
@Import(WorkflowAutoConfiguration.class)
@ComponentScan(basePackages = {
        "com.chua.starter.panel.test",
        "com.chua.starter.project",
        "com.chua.starter.docker",
        "com.chua.starter.datalake",
})
@MapperScan(basePackages = {"com.chua.starter.system.mapper", "com.chua.starter.workflow.mapper"})
public class PanelTestApplication {

    public static void main(String[] args) {
        // 面板测试环境默认走明文，避免因缺少加密密钥导致启动失败
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "panel-test");
        }
        SpringApplication.run(PanelTestApplication.class, args);
    }

}
