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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
     * 获取当前数据库 schema/catalog
     * <p>
     * 使用 JDBC DatabaseMetaData API 兼容任意数据库
     * </p>
     *
     * @return schema 名称
     */
    private String resolveSchema() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            log.error("[租户自动加字段] 无法获取数据源");
            return null;
        }

        try (Connection connection = dataSource.getConnection()) {
            // 优先使用 catalog（MySQL、SQL Server 等使用 catalog）
            String catalog = connection.getCatalog();
            if (catalog != null && !catalog.isEmpty()) {
                log.debug("[租户自动加字段] 使用 catalog: {}", catalog);
                return catalog;
            }

            // 其次使用 schema（PostgreSQL、Oracle 等使用 schema）
            String schema = connection.getSchema();
            if (schema != null && !schema.isEmpty()) {
                log.debug("[租户自动加字段] 使用 schema: {}", schema);
                return schema;
            }

            log.warn("[租户自动加字段] 无法获取 catalog 或 schema");
            return null;
        } catch (SQLException e) {
            log.error("[租户自动加字段] 获取 schema 异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 查询当前 schema 下的所有表
     * <p>
     * 使用 JDBC DatabaseMetaData.getTables() 兼容任意数据库
     * </p>
     *
     * @param schema schema
     * @return 表名列表
     */
    private List<String> queryTables(String schema) {
        List<String> tables = new ArrayList<>();
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return tables;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            // 参数说明：catalog, schemaPattern, tableNamePattern, types
            // types = "TABLE" 只查询普通表，不包含视图等
            try (ResultSet rs = metaData.getTables(schema, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName != null) {
                        tables.add(tableName);
                    }
                }
            }
            log.debug("[租户自动加字段] 查询到表数量: {}", tables.size());
        } catch (SQLException e) {
            log.error("[租户自动加字段] 查询表列表失败: {}", e.getMessage());
        }
        return tables;
    }

    /**
     * 判断指定表是否存在租户字段
     * <p>
     * 使用 JDBC DatabaseMetaData.getColumns() 兼容任意数据库
     * </p>
     *
     * @param schema    schema
     * @param tableName 表名
     * @return 是否存在
     */
    private boolean existsTenantColumn(String schema, String tableName) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            // 参数说明：catalog, schemaPattern, tableNamePattern, columnNamePattern
            try (ResultSet rs = metaData.getColumns(schema, schema, tableName, TenantDbConstants.TENANT_ID_COLUMN)) {
                boolean exists = rs.next();
                log.debug("[租户自动加字段] 检查租户字段: table={}, column={}, exists={}",
                        tableName, TenantDbConstants.TENANT_ID_COLUMN, exists);
                return exists;
            }
        } catch (SQLException e) {
            log.error("[租户自动加字段] 检查租户字段失败: table={}, error={}", tableName, e.getMessage());
            return false;
        }
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
