package com.chua.spider.support;

import com.chua.spider.support.configuration.SpiderAutoConfiguration;
import com.chua.spider.support.controller.SpiderCenterController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 爬虫中心后端启动类。
 *
 * <p>用于本地启动验证，扫描爬虫中心相关组件。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@SpringBootApplication(scanBasePackageClasses = {SpiderCenterController.class, SpiderAutoConfiguration.class})
public class SpiderTestApplication {

    /**
     * 程序入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SpiderTestApplication.class, args);
        log.info("爬虫中心后端启动成功 — 端口: {}", ctx.getEnvironment().getProperty("server.port"));
    }
}
