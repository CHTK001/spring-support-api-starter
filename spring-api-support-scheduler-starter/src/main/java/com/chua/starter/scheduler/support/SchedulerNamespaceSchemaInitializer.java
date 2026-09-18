package com.chua.starter.scheduler.support;

import com.chua.common.support.lang.datasource.dialect.Dialect;
import com.chua.common.support.lang.datasource.dialect.meta.ColumnMetadata;
import com.chua.common.support.lang.datasource.dialect.meta.TableMetadata;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.job.support.store.JobLogEntity;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 调度平台命名空间表初始化器。
 * <p>
 * 平台基于 job-starter 新架构，任务统一存储在 {@code sys_job / sys_job_log} 单表，
 * 不再维护多命名空间物理表。本初始化器基于新数据源 DDL 方言，对配置中
 * 显式启用的命名空间表做创建/更新，避免联调前手工补表。
 * </p>
 *
 * @since 4.0.0
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerNamespaceSchemaInitializer implements InitializingBean {

    /**
     * 默认任务表名
     */
    private static final String DEFAULT_JOB_TABLE = "sys_job";

    /**
     * 默认日志表名
     */
    private static final String DEFAULT_LOG_TABLE = "sys_job_log";

    /**
     * 调度平台属性
     */
    private final SchedulerJobPlatformProperties platformProperties;

    /**
     * 数据源提供者
     */
    private final ObjectProvider<DataSource> dataSourceProvider;

    @Override
    public void afterPropertiesSet() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            log.info("[Scheduler] 命名空间表初始化跳过: 未找到可用的数据源");
            return;
        }

        Map<String, SchedulerJobPlatformProperties.Namespace> namespaces = platformProperties.getNamespaces();
        if (namespaces == null || namespaces.isEmpty()) {
            return;
        }

        String databaseProductName = detectDatabaseProductName(dataSource);
        Dialect dialect = Dialect.getExtension(resolveDialectProtocol(databaseProductName));
        JdbcTableExecutor executor = new JdbcTableExecutor(dataSource, dialect);

        Set<String> initializedTables = new HashSet<>();
        for (Map.Entry<String, SchedulerJobPlatformProperties.Namespace> entry : namespaces.entrySet()) {
            if ("default".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            String namespace = entry.getKey();
            String jobTable = resolveJobTable(namespace);
            String logTable = resolveLogTable(namespace);
            initialize(executor, initializedTables, JobEntity.class, jobTable, namespace, databaseProductName);
            initialize(executor, initializedTables, JobLogEntity.class, logTable, namespace, databaseProductName);
        }
    }

    /**
     * 初始化单张命名空间表。
     *
     * @param executor          表结构执行器
     * @param initializedTables 已初始化表名集合
     * @param entityType        实体类型
     * @param tableName         表名
     * @param namespace         命名空间
     * @param databaseProductName 数据库产品名
     */
    private void initialize(JdbcTableExecutor executor,
                            Set<String> initializedTables,
                            Class<?> entityType,
                            String tableName,
                            String namespace,
                            String databaseProductName) {
        if (!initializedTables.add(tableName)) {
            return;
        }

        TableMetadata metadata = new TableMetadata()
                .setName(tableName)
                .setColumns(readExistingOrDefaultColumns(executor, tableName, namespace, entityType));

        if (isH2Database(databaseProductName)) {
            initializeForH2(executor, metadata, namespace);
            return;
        }

        List<String> errors = executor.doIt(metadata, JdbcTableExecutor.ActionType.CREATE);
        if (errors.isEmpty() || isAlreadyExists(errors)) {
            validateExistingTable(executor, metadata, namespace);
            log.info("[Scheduler] 命名空间表初始化完成: namespace={}, table={}", namespace, tableName);
            return;
        }
        throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                + namespace + ", table=" + tableName + ", error=" + String.join("; ", errors));
    }

    /**
     * 读取现有列，缺失时使用实体默认列。
     *
     * @param executor  执行器
     * @param tableName 表名
     * @param namespace 命名空间
     * @param entityType 实体类型
     * @return 列元数据列表
     */
    private List<ColumnMetadata> readExistingOrDefaultColumns(JdbcTableExecutor executor,
                                                              String tableName,
                                                              String namespace,
                                                              Class<?> entityType) {
        Set<String> existing = readExistingColumns(executor, tableName, namespace);
        if (existing.isEmpty()) {
            return SchedulerNamespaceTableResolver.defaultColumns(entityType);
        }
        List<ColumnMetadata> columns = new ArrayList<>();
        for (String column : existing) {
            columns.add(new ColumnMetadata().setColumnName(column));
        }
        return columns;
    }

    /**
     * H2 初始化：直接执行创建 DDL。
     *
     * @param executor  执行器
     * @param metadata  表元数据
     * @param namespace 命名空间
     */
    private void initializeForH2(JdbcTableExecutor executor,
                                  TableMetadata metadata,
                                  String namespace) {
        List<String> errors = executor.doIt(metadata, JdbcTableExecutor.ActionType.CREATE);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Scheduler H2 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + metadata.getName() + ", error=" + String.join("; ", errors));
        }
        log.info("[Scheduler] H2 命名空间表初始化完成: namespace={}, table={}", namespace, metadata.getName());
    }

    /**
     * 校验现有表结构。
     *
     * @param executor  执行器
     * @param metadata  表元数据
     * @param namespace 命名空间
     */
    private void validateExistingTable(JdbcTableExecutor executor,
                                       TableMetadata metadata,
                                       String namespace) {
        Set<String> existingColumns = readExistingColumns(executor, metadata.getName(), namespace);
        if (existingColumns.isEmpty()) {
            log.warn("[Scheduler] 命名空间表已存在但未读取到列信息: namespace={}, table={}",
                    namespace, metadata.getName());
            return;
        }
        log.info("[Scheduler] 命名空间表结构校验完成: namespace={}, table={}, columnCount={}",
                namespace, metadata.getName(), existingColumns.size());
    }

    /**
     * 解析命名空间任务表名。
     *
     * @param namespace 命名空间
     * @return 任务表名
     */
    private String resolveJobTable(String namespace) {
        return DEFAULT_JOB_TABLE + "_" + normalize(namespace);
    }

    /**
     * 解析命名空间日志表名。
     *
     * @param namespace 命名空间
     * @return 日志表名
     */
    private String resolveLogTable(String namespace) {
        return DEFAULT_LOG_TABLE + "_" + normalize(namespace);
    }

    /**
     * 归一化命名空间表名后缀。
     *
     * @param namespace 命名空间
     * @return 归一化后缀
     */
    private String normalize(String namespace) {
        return namespace.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    /**
     * 检测数据库产品名。
     *
     * @param dataSource 数据源
     * @return 数据库产品名
     */
    private String detectDatabaseProductName(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: 无法识别数据库类型", e);
        }
    }

    /**
     * 根据产品名解析方言协议。
     *
     * @param databaseProductName 数据库产品名
     * @return 方言协议名
     */
    private String resolveDialectProtocol(String databaseProductName) {
        if (databaseProductName == null) {
            return "mysql";
        }
        String name = databaseProductName.trim().toLowerCase(Locale.ROOT);
        if (name.contains("mysql") || name.contains("mariadb")) {
            return "mysql";
        }
        if (name.contains("postgresql")) {
            return "postgresql";
        }
        if (name.contains("oracle")) {
            return "oracle";
        }
        if (name.contains("h2")) {
            return "h2";
        }
        if (name.contains("sqlserver") || name.contains("mssql")) {
            return "sqlserver";
        }
        if (name.contains("sqlite")) {
            return "sqlite";
        }
        if (name.contains("derby")) {
            return "derby";
        }
        return "mysql";
    }

    /**
     * 判断是否为 H2 数据库。
     *
     * @param databaseProductName 数据库产品名
     * @return H2 返回 true
     */
    private boolean isH2Database(String databaseProductName) {
        if (databaseProductName == null) {
            return false;
        }
        return databaseProductName.trim().toLowerCase(Locale.ROOT).contains("h2");
    }

    /**
     * 判断错误消息是否表示表已存在。
     *
     * @param errors 错误消息列表
     * @return 已存在返回 true
     */
    private boolean isAlreadyExists(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return false;
        }
        String normalized = String.join(" ", errors).toLowerCase(Locale.ROOT);
        return normalized.contains("already exists")
                || normalized.contains("已存在")
                || normalized.contains("duplicate");
    }

    /**
     * 读取现有列名。
     *
     * @param executor  执行器
     * @param tableName 表名
     * @param namespace 命名空间
     * @return 列名集合
     */
    private Set<String> readExistingColumns(JdbcTableExecutor executor,
                                            String tableName,
                                            String namespace) {
        try (Connection connection = executor.dataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Set<String> columns = new LinkedHashSet<>();
            try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, null)) {
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    if (columnName != null) {
                        columns.add(columnName.trim().toLowerCase(Locale.ROOT));
                    }
                }
            }
            return columns;
        } catch (SQLException e) {
            log.warn("[Scheduler] 读取现有表结构失败: namespace={}, table={}, error={}",
                    namespace, tableName, e.getMessage());
            return Set.of();
        }
    }
}
