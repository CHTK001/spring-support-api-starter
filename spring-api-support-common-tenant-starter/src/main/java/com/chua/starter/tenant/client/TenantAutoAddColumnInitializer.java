package com.chua.starter.tenant.client;

import com.chua.starter.tenant.constant.TenantDbConstants;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 租户端自动补齐租户字段初始化器
 * <p>
 * 用于租户端（client 模式）启动时自动为业务表补齐租户隔离字段：sys_tenant_id。
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "auto-add-column", havingValue = "true")
public class TenantAutoAddColumnInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final TenantProperties tenantProperties;

    /**
     * 执行初始化逻辑
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!tenantProperties.isClientMode()) {
            return;
        }

        String schema = resolveSchema();
        if (schema == null || schema.isEmpty()) {
            log.warn("[租户自动加字段] 获取数据库 schema 失败，跳过自动加字段");
            return;
        }

        Set<String> ignoreTables = tenantProperties.getEffectiveIgnoreTable();
        List<String> tables = queryTables(schema);
        log.info("[租户自动加字段] 开始检查租户字段，schema={}, tableCount={}", schema, tables.size());

        for (String tableName : tables) {
            if (ignoreTables.contains(tableName)) {
                log.debug("[租户自动加字段] 跳过忽略表: {}", tableName);
                continue;
            }

            boolean exists = existsTenantColumn(schema, tableName);
            if (exists) {
                continue;
            }

            // 对缺失租户字段的表进行补齐
            addTenantColumn(tableName);
        }

        log.info("[租户自动加字段] 检查完成");
    }

    /**
     * 获取当前数据库 schema
     *
     * @return schema 名称
     */
    private String resolveSchema() {
        try {
            return jdbcTemplate.queryForObject("select database()", String.class);
        } catch (Exception ignored) {
            try {
                return jdbcTemplate.getDataSource() != null
                        ? jdbcTemplate.getDataSource().getConnection().getCatalog()
                        : null;
            } catch (Exception e) {
                log.debug("[租户自动加字段] 获取 schema 异常: {}", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 查询当前 schema 下的所有表
     *
     * @param schema schema
     * @return 表名列表
     */
    private List<String> queryTables(String schema) {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
        return jdbcTemplate.queryForList(sql, String.class, schema);
    }

    /**
     * 判断指定表是否存在租户字段
     *
     * @param schema    schema
     * @param tableName 表名
     * @return 是否存在
     */
    private boolean existsTenantColumn(String schema, String tableName) {
        String sql = """
                SELECT COUNT(1)
                FROM information_schema.columns
                WHERE table_schema = ?
                  AND table_name = ?
                  AND column_name = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schema, tableName, TenantDbConstants.TENANT_ID_COLUMN);
        return count != null && count > 0;
    }

    /**
     * 为指定表添加租户字段
     *
     * @param tableName 表名
     */
    private void addTenantColumn(String tableName) {
        // 重要：这里只做“字段补齐”，不对业务表结构做更多侵入式修改
        String alterSql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + TenantDbConstants.TENANT_ID_COLUMN
                + "` varchar(64) NULL COMMENT '租户ID'";

        try {
            jdbcTemplate.execute(alterSql);
            log.info("[租户自动加字段] 已补齐租户字段: table={}, column={}", tableName, TenantDbConstants.TENANT_ID_COLUMN);
        } catch (Exception e) {
            log.error("[租户自动加字段] 补齐租户字段失败: table={}, sql={}", tableName, alterSql, e);
        }
    }
}
