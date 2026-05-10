package com.chua.starter.spider.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 爬虫编排联调应用入口
 *
 * @author CH
 */
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.spider.test",
        "com.chua.starter.spider",
        "com.chua.starter.ai"
})
public class SpiderTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpiderTestApplication.class, args);
    }
}
