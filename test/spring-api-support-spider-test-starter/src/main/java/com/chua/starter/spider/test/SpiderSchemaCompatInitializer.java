package com.chua.starter.spider.test;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Spider 测试库兼容字段补齐。
 */
@Component
@RequiredArgsConstructor
public class SpiderSchemaCompatInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SpiderSchemaCompatInitializer.class);

    private static final List<String> TABLES = List.of(
            "spider_definition",
            "spider_flow",
            "spider_flow_node",
            "spider_flow_edge",
            "spider_url_queue",
            "spider_parse_result",
            "spider_execution",
            "spider_execution_step",
            "spider_manual_task",
            "spider_ai_template"
    );

    /**
     * 每次启动时清除陈旧流程数据，强制后续读取回退到
     * {@link com.chua.starter.spider.support.example.SpiderExampleScenarioRegistry}。
     * <p>避免 V1.1__init_spider_data.sql 中的流程数据与 registry 不同步，
     * 也避免前次运行时 UI 保存的陈旧数据影响新启动的执行。</p>
     */
    private static final List<String> FLOW_TABLES = List.of(
            "spider_flow_edge",
            "spider_flow_node",
            "spider_flow"
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        log.info("[SchemaCompat] 开始清除陈旧流程数据，确保 registry 为唯一权威来源...");
        for (String table : FLOW_TABLES) {
            try {
                jdbcTemplate.execute("DELETE FROM " + table);
                log.debug("[SchemaCompat] 已清除: {}", table);
            } catch (Exception e) {
                log.warn("[SchemaCompat] 清除 {} 失败: {}", table, e.getMessage());
            }
        }
        for (String table : TABLES) {
            try {
                ensureColumn(table, "create_name", "VARCHAR(255) NULL");
                ensureColumn(table, "create_by", "INT NULL");
                ensureColumn(table, "create_time", "DATETIME NULL");
                ensureColumn(table, "update_name", "VARCHAR(255) NULL");
                ensureColumn(table, "update_by", "INT NULL");
                ensureColumn(table, "update_time", "DATETIME NULL");
                backfill(table, "create_name", "spider_create_name");
                backfill(table, "create_by", "spider_create_by");
                backfill(table, "create_time", "spider_create_time");
                backfill(table, "update_name", "spider_update_name");
                backfill(table, "update_by", "spider_update_by");
                backfill(table, "update_time", "spider_update_time");
            } catch (Exception e) {
                log.warn("兼容字段补齐失败（table={}）: {}", table, e.getMessage());
            }
        }
        // 编排类型分离：AI 编排 / MANUAL 编排
        ensureColumn("spider_flow", "spider_flow_orchestration_type",
                "VARCHAR(16) NOT NULL DEFAULT 'AI' COMMENT '编排类型: AI/MANUAL'");

        // 执行记录边表（执行引擎需要）
        ensureDropAndRecreate("spider_execution_edge", """
                CREATE TABLE IF NOT EXISTS spider_execution_edge (
                  spider_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  spider_edge_id VARCHAR(64) NOT NULL COMMENT '边ID',
                  spider_execution_no VARCHAR(64) NOT NULL COMMENT '执行编号',
                  spider_source_node_code VARCHAR(128) DEFAULT NULL,
                  spider_target_node_code VARCHAR(128) DEFAULT NULL,
                  spider_edge_label VARCHAR(128) DEFAULT NULL,
                  spider_source_output VARCHAR(64) DEFAULT NULL,
                  spider_target_input VARCHAR(64) DEFAULT NULL,
                  spider_runtime_status VARCHAR(32) DEFAULT NULL,
                  spider_visited TINYINT(1) DEFAULT 0,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        ensureColumn("spider_execution_edge", "create_name", "VARCHAR(255) NULL");
        ensureColumn("spider_execution_edge", "create_by", "INT NULL");
        ensureColumn("spider_execution_edge", "update_name", "VARCHAR(255) NULL");
        ensureColumn("spider_execution_edge", "update_by", "INT NULL");

        log.info("[SchemaCompat] spider_* 表兼容字段检查完成");
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                      FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = (SELECT DATABASE())
                       AND TABLE_NAME = ?
                       AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    tableName,
                    columnName
            );
            if (count != null && count > 0) {
                return;
            }
        } catch (Exception e) {
            log.warn("查询列信息失败（table={}, column={}）: {}", tableName, columnName, e.getMessage());
            return;
        }
        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + ddl);
    }

    private void ensureTable(String tableName, String createDDL) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = (SELECT DATABASE())
                       AND TABLE_NAME = ?
                    """,
                    Integer.class,
                    tableName
            );
            if (count != null && count > 0) {
                return;
            }
        } catch (Exception e) {
            log.warn("查询表信息失败（table={}）: {}", tableName, e.getMessage());
            return;
        }
        jdbcTemplate.execute(createDDL);
        log.info("[SchemaCompat] 已创建表: {}", tableName);
    }

    private void ensureDropAndRecreate(String tableName, String createDDL) {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        } catch (Exception e) {
            log.warn("删除表 {} 失败: {}", tableName, e.getMessage());
        }
        jdbcTemplate.execute(createDDL);
        log.info("[SchemaCompat] 已重建表: {}", tableName);
    }

    private void backfill(String tableName, String targetColumn, String sourceColumn) {
        Integer sourceExists;
        try {
            sourceExists = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                      FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = (SELECT DATABASE())
                       AND TABLE_NAME = ?
                       AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    tableName,
                    sourceColumn
            );
        } catch (Exception e) {
            log.warn("查询源列信息失败（table={}, column={}）: {}", tableName, sourceColumn, e.getMessage());
            return;
        }
        if (sourceExists == null || sourceExists == 0) {
            return;
        }
        jdbcTemplate.update(
                "UPDATE `" + tableName + "` SET `" + targetColumn + "` = `" + sourceColumn + "` WHERE `" + targetColumn + "` IS NULL"
        );
    }
}

