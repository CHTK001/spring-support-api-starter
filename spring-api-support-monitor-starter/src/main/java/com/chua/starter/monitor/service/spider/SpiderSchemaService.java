package com.chua.starter.monitor.service.spider;

import com.chua.starter.monitor.pojo.spider.SpiderTableStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 爬虫表结构管理服务
 *
 * @author CH
 * @since 2026/03/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpiderSchemaService {

    private static final Map<String, List<String>> REQUIRED_COLUMNS = createRequiredColumns();
    private static final Map<String, Map<String, String>> COLUMN_DEFINITIONS = createColumnDefinitions();

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private static Map<String, List<String>> createRequiredColumns() {
        Map<String, List<String>> requiredColumns = new LinkedHashMap<>();
        requiredColumns.put("monitor_spider_task", List.of(
                "spider_task_id", "spider_task_name", "spider_task_desc", "spider_task_url",
                "spider_task_url_pattern", "spider_task_extract_rules", "spider_task_selector_type",
                "spider_task_status", "spider_task_schedule_type", "spider_task_cron", "spider_task_thread_num",
                "spider_task_sleep_time", "spider_task_retry_times", "spider_task_timeout",
                "spider_task_headers", "spider_task_cookies", "spider_task_user_agent", "spider_task_proxy",
                "spider_task_max_depth", "spider_task_enable_url_discover", "spider_task_page_pattern",
                "spider_task_max_pages", "spider_task_incremental_mode", "spider_task_rate_limit",
                "spider_task_pipeline_type", "spider_task_pipeline_config", "spider_task_last_run_time",
                "spider_task_next_run_time", "spider_task_run_status", "spider_task_total_success",
                "spider_task_total_fail", "spider_task_job_id", "create_time", "update_time"
        ));
        requiredColumns.put("monitor_spider_data", List.of(
                "spider_data_id", "spider_task_id", "spider_data_content", "spider_data_url",
                "spider_data_hash", "crawl_time", "create_time"
        ));
        requiredColumns.put("monitor_spider_log", List.of(
                "spider_log_id", "spider_task_id", "spider_log_level", "spider_log_message",
                "spider_log_url", "spider_log_time", "create_time"
        ));
        requiredColumns.put("monitor_spider_node", List.of(
                "node_id", "spider_task_id", "node_key", "node_name", "node_type", "spi_name",
                "node_config", "node_description", "position_x", "position_y", "node_order",
                "node_enabled", "requires_config", "create_time", "update_time"
        ));
        requiredColumns.put("monitor_spider_connection", List.of(
                "connection_id", "spider_task_id", "source_node_id", "target_node_id", "source_port",
                "target_port", "connection_type", "connection_label", "create_time"
        ));
        return Collections.unmodifiableMap(requiredColumns);
    }

    private static Map<String, Map<String, String>> createColumnDefinitions() {
        Map<String, Map<String, String>> definitions = new LinkedHashMap<>();

        definitions.put("monitor_spider_task", Map.ofEntries(
                Map.entry("spider_task_max_depth", "`spider_task_max_depth` int DEFAULT '0' COMMENT '最大爬取深度'"),
                Map.entry("spider_task_enable_url_discover", "`spider_task_enable_url_discover` tinyint(1) DEFAULT '0' COMMENT '是否启用URL自动发现'"),
                Map.entry("spider_task_page_pattern", "`spider_task_page_pattern` varchar(1000) DEFAULT NULL COMMENT '分页URL模式'"),
                Map.entry("spider_task_max_pages", "`spider_task_max_pages` int DEFAULT '0' COMMENT '最大分页数'"),
                Map.entry("spider_task_incremental_mode", "`spider_task_incremental_mode` varchar(20) DEFAULT 'NONE' COMMENT '增量爬取模式'"),
                Map.entry("spider_task_rate_limit", "`spider_task_rate_limit` int DEFAULT '0' COMMENT '速率限制'"),
                Map.entry("create_time", "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"),
                Map.entry("update_time", "`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
        ));
        definitions.put("monitor_spider_data", Map.ofEntries(
                Map.entry("crawl_time", "`crawl_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '爬取时间'"),
                Map.entry("create_time", "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
        ));
        definitions.put("monitor_spider_log", Map.of("create_time",
                "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"));
        definitions.put("monitor_spider_node", Map.ofEntries(
                Map.entry("create_time", "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"),
                Map.entry("update_time", "`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
        ));
        definitions.put("monitor_spider_connection", Map.of("create_time",
                "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"));

        return Collections.unmodifiableMap(definitions);
    }

    public SpiderTableStatus checkTableStatus() {
        SpiderTableStatus status = new SpiderTableStatus();
        Map<String, Boolean> tables = new LinkedHashMap<>();
        Map<String, List<String>> missingColumns = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : REQUIRED_COLUMNS.entrySet()) {
            String tableName = entry.getKey();
            boolean exists = tableExists(tableName);
            tables.put(tableName, exists);
            if (!exists) {
                missingColumns.put(tableName, entry.getValue());
                continue;
            }

            List<String> columns = jdbcTemplate.queryForList("""
                    SELECT COLUMN_NAME
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                    """, String.class, tableName);
            List<String> missing = entry.getValue().stream()
                    .filter(it -> !columns.contains(it))
                    .toList();
            if (!missing.isEmpty()) {
                missingColumns.put(tableName, missing);
            }
        }

        List<String> missingTables = tables.entrySet().stream()
                .filter(it -> !it.getValue())
                .map(Map.Entry::getKey)
                .toList();

        status.setTables(tables);
        status.setMissingTables(missingTables);
        status.setMissingColumns(missingColumns);
        status.setReady(missingTables.isEmpty() && missingColumns.isEmpty());
        return status;
    }

    public boolean initializeTables(boolean force) {
        try {
            executeSchemaScript();
            backfillMissingColumns();
            SpiderTableStatus status = checkTableStatus();
            if (!status.getReady()) {
                throw new IllegalStateException("爬虫表仍存在缺失项: " + describeMissing(status));
            }
            log.info("[Spider] 爬虫数据库表初始化完成, force={}", force);
            return true;
        } catch (Exception e) {
            log.error("[Spider] 初始化爬虫表失败", e);
            throw new IllegalStateException("初始化爬虫表失败: " + e.getMessage(), e);
        }
    }

    void executeSchemaScript() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
        populator.addScript(new ClassPathResource("db/spider/V1.0__init_spider_tables.sql"));
        populator.execute(dataSource);
    }

    void backfillMissingColumns() {
        SpiderTableStatus status = checkTableStatus();
        for (Map.Entry<String, List<String>> entry : status.getMissingColumns().entrySet()) {
            String tableName = entry.getKey();
            if (!tableExists(tableName)) {
                continue;
            }
            for (String columnName : entry.getValue()) {
                String definition = resolveColumnDefinition(tableName, columnName);
                if (definition == null) {
                    log.warn("[Spider] 未找到缺失列定义，跳过补齐: {}.{}", tableName, columnName);
                    continue;
                }
                String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN " + definition;
                jdbcTemplate.execute(sql);
                log.info("[Spider] 已补齐缺失列: {}.{}", tableName, columnName);
            }
        }
    }

    private String resolveColumnDefinition(String tableName, String columnName) {
        Map<String, String> tableDefinitions = COLUMN_DEFINITIONS.get(tableName);
        if (tableDefinitions == null) {
            return null;
        }
        return tableDefinitions.get(columnName);
    }

    private String describeMissing(SpiderTableStatus status) {
        StringJoiner joiner = new StringJoiner("; ");
        if (!status.getMissingTables().isEmpty()) {
            joiner.add("missingTables=" + status.getMissingTables());
        }
        if (!status.getMissingColumns().isEmpty()) {
            joiner.add("missingColumns=" + status.getMissingColumns());
        }
        return joiner.toString();
    }

    public boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }
}
