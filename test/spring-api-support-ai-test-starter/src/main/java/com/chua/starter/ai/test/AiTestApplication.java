package com.chua.starter.ai.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 独立 AI 联调测试后端。
 */
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.ai.test",
        "com.chua.starter.spider"
})
public class AiTestApplication {

    public static void main(String[] args) {
        // 测试环境默认走明文加解密，避免缺少密钥导致启动失败
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "ai-test");
        }
        SpringApplication.run(AiTestApplication.class, args);
    }
}
