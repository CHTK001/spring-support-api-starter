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
        SpringApplication.run(AiTestApplication.class, args);
    }
}
