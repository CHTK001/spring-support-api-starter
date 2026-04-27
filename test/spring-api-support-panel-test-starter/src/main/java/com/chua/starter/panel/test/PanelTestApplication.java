package com.chua.starter.panel.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 独立 Panel 联调测试后端。
 */
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.panel.test",
        "com.chua.starter.project"
})
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
