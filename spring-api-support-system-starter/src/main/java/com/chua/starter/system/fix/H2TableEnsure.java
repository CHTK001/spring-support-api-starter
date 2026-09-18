package com.chua.starter.system.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

/**
 * H2 环境下，在所有 DataSource 的实际连接上执行 ddl-h2.sql，
 * 确保 AI 模块的表（sys_ai_global_setting / sys_ai_requirement 等）在 mapper 查询前存在。
 * 使用 ApplicationReadyEvent 前的 earliest hook 不现实，因此改为在首个 mapper bean 初始化时
 * 通过 BeanPostProcessor 触发（见 EarlyH2DdlRunner），并在此处做兜底。
 */
@Profile("h2")
@Component
public class H2TableEnsure {

    private static final Logger log = LoggerFactory.getLogger(H2TableEnsure.class);

    @Autowired
    /** 数据Sources */
    private Map<String, DataSource> dataSources;

    /**
     * ensureTables。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureTables() {
        // 兜底：应用就绪后再次执行 DDL（幂等）
        for (Map.Entry<String, DataSource> e : dataSources.entrySet()) {
            try {
                ResourceDatabasePopulator p = new ResourceDatabasePopulator();
                p.addScript(new ClassPathResource("ddl-h2.sql"));
                p.setContinueOnError(true);
                p.setSeparator(";");
                p.execute(e.getValue());
                log.info("[H2TableEnsure] re-ensure tables on {}", e.getKey());
            } catch (Exception ex) {
                log.warn("[H2TableEnsure] skipped {} : {}", e.getKey(), ex.getMessage());
            }
        }
    }
}