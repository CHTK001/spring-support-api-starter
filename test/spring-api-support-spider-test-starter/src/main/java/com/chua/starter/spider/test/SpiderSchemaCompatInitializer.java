package com.chua.starter.spider.test;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Spider 测试库兼容字段补齐。
 *
 * <p>当前 spider_* 实体继承 SysBase，运行时会按 create_name / update_name 等字段读写；
 * 测试 SQL 采用 spider_create_name / spider_update_name 前缀。
 * 这里在测试环境启动时补齐一组兼容列，避免真实联调被字段缺失阻塞。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiderSchemaCompatInitializer implements ApplicationRunner {

    private static final List<String> TABLES = List.of(
            "spider_definition",
            "spider_flow",
            "spider_flow_node",
            "spider_url_queue",
            "spider_parse_result",
            "spider_execution",
            "spider_execution_step",
            "spider_manual_task",
            "spider_ai_template"
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
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
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                      FROM information_schema.COLUMNS
                     WHERE TABLE_NAME = ?
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

    private void backfill(String tableName, String targetColumn, String sourceColumn) {
        Integer sourceExists;
        try {
            sourceExists = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                      FROM information_schema.COLUMNS
                     WHERE TABLE_NAME = ?
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

