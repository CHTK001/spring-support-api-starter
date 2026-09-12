package com.chua.starter.tenant.client;

 // 以下类已从 工具-通用 移除，改为直接使用 JDBC databasemeta数据 API：
 // 导入 com.chua.通用.支持.datasource.dialect.Dialect;
 // 导入 com.chua.通用.支持.datasource.dialect.dialect工厂;
 // 导入 com.chua.通用.支持.datasource.meta.columnmetadata;
 // 导入 com.chua.通用.支持.datasource.meta.tablemetadata;
 // 导入 com.chua.通用.支持.datasource.类型.java类型;
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
* 用于租户端（客户端 模式）启动时自动为业务表补齐租户隔离字段：sys_tenant_标识。
* 使用 工具-通用 的 tabledefinition 工具生成 DDL 语句。
* </p>
*
* @版本 1.0.0
* @since 2025/12/12
* @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "auto-add-column", havingValue = "true")
public class TenantAutoAddColumnInitializer implements ApplicationRunner {

    /** JDBC模板 */
    private final JdbcTemplate jdbcTemplate;
    /** Tenant属性 */
    private final TenantProperties tenantProperties;

    /**
    * 数据库方言（懒加载）
    * <p>Dialect / TableMetadata / ColumnMetadata 已移除，改用 JDBC DatabaseMetaData API 直接操作。</p>
     */
 // 私募 Dialect dialect;

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
    * 获取当前数据库 模式/catalog
    * <p>
    * 使用 JDBC databasemeta数据 API 兼容任意数据库
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
 // 优先使用 catalog（MySQL、SQL 服务端 等使用 catalog）
            String catalog = connection.getCatalog();
            if (catalog != null && !catalog.isEmpty()) {
                log.debug("[租户自动加字段] 使用 catalog: {}", catalog);
                return catalog;
            }

 // 其次使用 模式（PostgreSQL、Oracle 等使用 模式）
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
    * 查询当前 模式 下的所有表
    * <p>
    * 使用 JDBC databasemeta数据.获取tables() 兼容任意数据库
    * </p>
    *
    * @param schema 模式
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
 // 参数说明：catalog, 模式模式, table名称模式, 类型
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
    * 使用 JDBC databasemeta数据.获取columns() 兼容任意数据库
    * </p>
    *
    * @param schema    模式
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
 // 参数说明：catalog, 模式模式, table名称模式, column名称模式
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
    * <p>Dialect、TableMetadata、ColumnMetadata 已移除，使用简单 ALTER TABLE 语句实现。</p>
    *
    * @param tableName 表名
     */
    private void addTenantColumn(String tableName) {
        try {
            // 使用简单的 ALTER TABLE 语句
            String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + TenantDbConstants.TENANT_ID_COLUMN + " VARCHAR(64) COMMENT '租户ID'";
            log.debug("[租户自动加字段] 生成的SQL: {}", alterSql);

            jdbcTemplate.execute(alterSql);
            log.info("[租户自动加字段] 已补齐租户字段: table={}, column={}", tableName, TenantDbConstants.TENANT_ID_COLUMN);
        } catch (Exception e) {
            log.error("[租户自动加字段] 补齐租户字段失败: table={}", tableName, e);
        }
    }
}
