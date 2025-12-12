package com.chua.starter.tenant.client;

import com.chua.common.support.datasource.dialect.Dialect;
import com.chua.common.support.datasource.dialect.DialectFactory;
import com.chua.common.support.datasource.meta.ColumnMetadata;
import com.chua.common.support.datasource.meta.TableMetadata;
import com.chua.common.support.datasource.type.JavaType;
import com.chua.starter.tenant.constant.TenantDbConstants;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Set;

/**
 * 租户端自动补齐租户字段初始化器
 * <p>
 * 用于租户端（client 模式）启动时自动为业务表补齐租户隔离字段：sys_tenant_id。
 * 使用 utils-common 的 TableDefinition 工具生成 DDL 语句。
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
     * 数据库方言（懒加载）
     */
    private Dialect dialect;

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
     * <p>
     * 使用 utils-common 的 TableMetadata 和 ColumnMetadata 生成 DDL 语句
     * </p>
     *
     * @param tableName 表名
     */
    private void addTenantColumn(String tableName) {
        try {
            // 获取数据库方言
            Dialect currentDialect = getDialect();
            if (currentDialect == null) {
                log.error("[租户自动加字段] 无法获取数据库方言，跳过表: {}", tableName);
                return;
            }

            // 构建表元数据
            TableMetadata tableMetadata = new TableMetadata();
            tableMetadata.setName(tableName);

            // 构建租户字段的列元数据
            ColumnMetadata columnMetadata = createTenantColumnMetadata();

            // 使用 TableMetadata 生成 ADD COLUMN SQL
            String alterSql = tableMetadata.sqlAddColumn(currentDialect, columnMetadata);
            log.debug("[租户自动加字段] 生成的SQL: {}", alterSql);

            jdbcTemplate.execute(alterSql);
            log.info("[租户自动加字段] 已补齐租户字段: table={}, column={}", tableName, TenantDbConstants.TENANT_ID_COLUMN);
        } catch (Exception e) {
            log.error("[租户自动加字段] 补齐租户字段失败: table={}", tableName, e);
        }
    }

    /**
     * 创建租户字段的列元数据
     *
     * @return 列元数据
     */
    private ColumnMetadata createTenantColumnMetadata() {
        ColumnMetadata columnMetadata = new ColumnMetadata();
        columnMetadata.setColumnName(TenantDbConstants.TENANT_ID_COLUMN);
        columnMetadata.setComment("租户ID");
        columnMetadata.setNullable(true);
        columnMetadata.setLength(64);
        // 设置 Java 类型为 String，对应 VARCHAR
        columnMetadata.setJavaType(new JavaType(String.class, "VARCHAR"));
        return columnMetadata;
    }

    /**
     * 获取数据库方言（懒加载）
     *
     * @return 数据库方言
     */
    private Dialect getDialect() {
        if (dialect != null) {
            return dialect;
        }

        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            log.error("[租户自动加字段] 无法获取数据源");
            return null;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            String driverName = metaData.getDriverName();
            log.debug("[租户自动加字段] 数据库URL: {}, 驱动: {}", url, driverName);

            // 使用 DialectFactory 创建方言
            dialect = DialectFactory.createDialect(url, driverName);

            if (dialect != null) {
                log.debug("[租户自动加字段] 使用数据库方言: {}", dialect.getClass().getSimpleName());
            }
            return dialect;
        } catch (Exception e) {
            log.error("[租户自动加字段] 获取数据库方言失败", e);
            return null;
        }
    }
}
