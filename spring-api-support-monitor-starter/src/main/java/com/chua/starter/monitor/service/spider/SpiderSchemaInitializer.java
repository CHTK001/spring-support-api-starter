package com.chua.starter.monitor.service.spider;

import com.chua.starter.monitor.properties.SpiderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 爬虫表结构初始化器
 *
 * @author CH
 * @since 2026/03/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiderSchemaInitializer implements CommandLineRunner {

    private final SpiderProperties spiderProperties;
    private final SpiderSchemaService spiderSchemaService;

    @Override
    public void run(String... args) {
        if (!spiderProperties.isEnabled() || !spiderProperties.isAutoCreateTable()) {
            return;
        }

        log.info("[Spider] 启动时自动初始化爬虫表结构");
        spiderSchemaService.initializeTables(false);
    }
}
