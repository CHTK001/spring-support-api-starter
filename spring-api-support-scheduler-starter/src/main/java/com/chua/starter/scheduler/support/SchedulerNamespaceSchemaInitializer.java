package com.chua.starter.scheduler.support;

import com.chua.datasource.support.data.datasource.meta.TableMetadata;
import com.chua.datasource.support.data.query.JdbcEngine;
import com.chua.datasource.support.data.query.ddl.ActionType;
import com.chua.datasource.support.data.query.ddl.JdbcTableExecutor;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.starter.job.support.JobProperties;
import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.entity.SysJobLog;
import com.chua.starter.job.support.entity.SysJobLogBackup;
import com.chua.starter.job.support.entity.SysJobLogDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 调度平台命名空间表初始化器。
 * <p>
 * 该初始化器会复用 {@code plugin.job.table-init-mode} 的策略，把调度平台里显式
 * 配置的业务命名空间表按前缀解析后创建/更新出来，避免联调前还要手工补表。
 * </p>
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerNamespaceSchemaInitializer implements InitializingBean {

    /** 作业属性 */
    private final JobProperties jobProperties;
    /** Namespace表解析器 */
    private final SchedulerNamespaceTableResolver namespaceTableResolver;
    /** 数据来源提供者 */
    private final ObjectProvider<DataSource> dataSourceProvider;

    @Override
    /** AfterProperties设置 */
    public void afterPropertiesSet() {
        ActionType actionType = resolveActionType(jobProperties);
        if (actionType == null) {
            return;
        }

        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: 未找到可用的数据源");
        }

        String databaseProductName = detectDatabaseProductName(dataSource);
        JdbcTableExecutor executor = new JdbcEngine()
                .setDataSource(dataSource)
                .createTable();

        Set<String> initializedTables = new HashSet<>();
        for (String namespace : namespaceTableResolver.namespaces()) {
            if ("default".equalsIgnoreCase(namespace)) {
                continue;
            }
            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
            initialize(executor, dataSource, initializedTables, SysJob.class, tables.jobTable(), actionType, namespace, databaseProductName);
            initialize(executor, dataSource, initializedTables, SysJobLog.class, tables.logTable(), actionType, namespace, databaseProductName);
            initialize(executor, dataSource, initializedTables, SysJobLogDetail.class, tables.logDetailTable(), actionType, namespace, databaseProductName);
            initialize(executor, dataSource, initializedTables, SysJobLogBackup.class, tables.logBackupTable(), actionType, namespace, databaseProductName);
        }
    }

    /**
     * 初始化
     * @param executor executor
     * @param dataSource dataSource
     * @param initializedTables initializedTables
     * @param entityType entityType
     * @param tableName tableName
     * @param actionType actionType
     * @param namespace namespace
     * @param databaseProductName databaseProductName
     */
    private void initialize(JdbcTableExecutor executor,
                            DataSource dataSource,
                            Set<String> initializedTables,
                            Class<?> entityType,
                            String tableName,
                            ActionType actionType,
                            String namespace,
                            String databaseProductName) {
        if (!initializedTables.add(tableName)) {
            return;
        }
        TableMetadata metadata = new TableMetadata();
        BeanUtils.copyProperties(TableMetadata.of(entityType), metadata);
        metadata.setName(tableName);

        if (isH2Database(databaseProductName)) {
            initializeForH2(dataSource, entityType, tableName, actionType, namespace);
            return;
        }
        if (actionType == ActionType.CREATE) {
            createOrSkip(executor, dataSource, metadata, entityType, namespace, tableName, actionType);
            return;
        }
        if (actionType == ActionType.DROP_CREATE) {
            execute(executor, metadata, ActionType.DROP_CREATE, namespace);
            upgradeLegacySchema(dataSource, entityType, tableName, namespace);
            log.info("[Scheduler] 命名空间表初始化完成: namespace={}, table={}, mode={}", namespace, tableName, actionType);
            return;
        }

        updateOrCreate(executor, dataSource, metadata, entityType, namespace, tableName, actionType);
    }

    /**
     * 执行
     * @param executor executor
     * @param metadata metadata
     * @param actionType actionType
     * @param namespace namespace
     */
    private void execute(JdbcTableExecutor executor,
                         TableMetadata metadata,
                         ActionType actionType,
                         String namespace) {
        ErrorResult<String> result = executor.doIt(metadata, actionType);
        if (result != null && result.hasError()) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + metadata.getName() + ", error=" + result.getFirstError());
        }
    }

    /**
     * 创建OrSkip
     * @param executor executor
     * @param dataSource dataSource
     * @param metadata metadata
     * @param entityType entityType
     * @param namespace namespace
     * @param tableName tableName
     * @param actionType actionType
     */
    private void createOrSkip(JdbcTableExecutor executor,
                              DataSource dataSource,
                              TableMetadata metadata,
                              Class<?> entityType,
                              String namespace,
                              String tableName,
                              ActionType actionType) {
        ErrorResult<String> result = executor.doIt(metadata, ActionType.CREATE);
        if (result == null || !result.hasError()) {
            upgradeLegacySchema(dataSource, entityType, tableName, namespace);
            log.info("[Scheduler] 命名空间表初始化完成: namespace={}, table={}, mode={}", namespace, tableName, actionType);
            return;
        }
        if (isAlreadyExists(result)) {
            upgradeLegacySchema(dataSource, entityType, tableName, namespace);
            validateExistingTable(dataSource, metadata, namespace);
            log.info("[Scheduler] 命名空间表已存在，跳过创建: namespace={}, table={}, mode={}", namespace, tableName, actionType);
            return;
        }
        throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                + namespace + ", table=" + tableName + ", error=" + result.getFirstError());
    }

    /**
     * 调度平台浏览器模式默认使用 H2 内存库。
     * <p>
     * H2 在兼容 MySQL 模式时，JdbcEngine 对 identity 列的 DROP/UPDATE 仍然不稳定，
     * 所以命名空间表初始化需要复用一套确定性的 DDL，保证 browser 模式可直接启动。
     * </p>
     */
    private void initializeForH2(DataSource dataSource,
                                 Class<?> entityType,
                                 String tableName,
                                 ActionType actionType,
                                 String namespace) {
        List<String> ddl = h2SchemaSql(entityType, tableName);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            if (actionType == ActionType.DROP_CREATE) {
                statement.execute("DROP TABLE IF EXISTS " + tableName);
            }
            for (String sql : ddl) {
                statement.execute(sql);
            }
            log.info("[Scheduler] H2 命名空间表初始化完成: namespace={}, table={}, mode={}", namespace, tableName, actionType);
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler H2 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName, e);
        }
    }

    /** HSchemaSql */
    private List<String> h2SchemaSql(Class<?> entityType, String tableName) {
        if (SysJob.class == entityType) {
            return List.of(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                            + "job_id INT AUTO_INCREMENT PRIMARY KEY,"
                            + "job_no VARCHAR(64) NOT NULL,"
                            + "job_name VARCHAR(255),"
                            + "job_schedule_type VARCHAR(50) DEFAULT 'CRON',"
                            + "job_schedule_time VARCHAR(255),"
                            + "job_author VARCHAR(100),"
                            + "job_alarm_email VARCHAR(255),"
                            + "job_trigger_status TINYINT DEFAULT 0,"
                            + "job_desc VARCHAR(500),"
                            + "job_glue_updatetime TIMESTAMP NULL,"
                            + "job_glue_source CLOB,"
                            + "job_glue_type VARCHAR(50) DEFAULT 'BEAN',"
                            + "job_dispatch_mode VARCHAR(20) DEFAULT 'LOCAL',"
                            + "job_remote_executor_address VARCHAR(500),"
                            + "job_storage_mode VARCHAR(20) DEFAULT 'DATABASE',"
                            + "job_fail_retry INT DEFAULT 3,"
                            + "job_retry_interval INT DEFAULT 0,"
                            + "job_execute_timeout INT DEFAULT 0,"
                            + "job_execute_bean VARCHAR(255),"
                            + "job_execute_param VARCHAR(512),"
                            + "job_exception_callback_bean VARCHAR(255),"
                            + "job_retry_callback_bean VARCHAR(255),"
                            + "job_execute_misfire_strategy VARCHAR(50) DEFAULT 'DO_NOTHING',"
                            + "job_trigger_last_time BIGINT DEFAULT 0,"
                            + "job_trigger_next_time BIGINT DEFAULT 0,"
                            + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                            + "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                            + "create_by VARCHAR(50),"
                            + "update_by VARCHAR(50)"
                            + ")",
                    "CREATE UNIQUE INDEX IF NOT EXISTS uk_job_no ON " + tableName + " (job_no)",
                    "CREATE INDEX IF NOT EXISTS idx_trigger_status ON " + tableName + " (job_trigger_status)",
                    "CREATE INDEX IF NOT EXISTS idx_trigger_next_time ON " + tableName + " (job_trigger_next_time)",
                    "CREATE INDEX IF NOT EXISTS idx_job_dispatch_mode ON " + tableName + " (job_dispatch_mode)"
            );
        }
        if (SysJobLog.class == entityType) {
            return List.of(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                            + "job_log_id INT AUTO_INCREMENT PRIMARY KEY,"
                            + "job_log_no VARCHAR(64) NOT NULL,"
                            + "job_id INT,"
                            + "job_no VARCHAR(64),"
                            + "job_log_app VARCHAR(255),"
                            + "job_log_trigger_bean VARCHAR(255),"
                            + "job_log_trigger_type VARCHAR(50),"
                            + "job_log_profile VARCHAR(50),"
                            + "job_log_trigger_time TIMESTAMP NULL,"
                            + "job_log_trigger_date DATE,"
                            + "job_log_trigger_code VARCHAR(20),"
                            + "job_log_execute_code VARCHAR(20) DEFAULT 'PADDING',"
                            + "job_log_cost DECIMAL(10,2),"
                            + "job_log_trigger_msg CLOB,"
                            + "job_log_trigger_param VARCHAR(512),"
                            + "job_log_trigger_address VARCHAR(255),"
                            + "job_log_file_path VARCHAR(500),"
                            + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                            + "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                            + "create_by VARCHAR(50),"
                            + "update_by VARCHAR(50)"
                            + ")",
                    "CREATE UNIQUE INDEX IF NOT EXISTS uk_job_log_no ON " + tableName + " (job_log_no)",
                    "CREATE INDEX IF NOT EXISTS idx_trigger_date ON " + tableName + " (job_log_trigger_date)",
                    "CREATE INDEX IF NOT EXISTS idx_trigger_bean ON " + tableName + " (job_log_trigger_bean)",
                    "CREATE INDEX IF NOT EXISTS idx_job_no ON " + tableName + " (job_no)"
            );
        }
        if (SysJobLogDetail.class == entityType) {
            return List.of(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                            + "job_log_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                            + "job_log_id INT,"
                            + "job_log_no VARCHAR(64),"
                            + "job_id INT,"
                            + "job_no VARCHAR(64),"
                            + "job_log_detail_level VARCHAR(20) DEFAULT 'INFO',"
                            + "job_log_detail_content CLOB,"
                            + "job_log_detail_time TIMESTAMP NULL,"
                            + "job_log_detail_phase VARCHAR(50),"
                            + "job_log_detail_progress INT,"
                            + "job_log_detail_file_path VARCHAR(500),"
                            + "job_log_detail_handler VARCHAR(255),"
                            + "job_log_detail_profile VARCHAR(100),"
                            + "job_log_detail_address VARCHAR(255),"
                            + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                            + ")",
                    "CREATE INDEX IF NOT EXISTS idx_job_log_id ON " + tableName + " (job_log_id)",
                    "CREATE INDEX IF NOT EXISTS idx_job_id ON " + tableName + " (job_id)",
                    "CREATE INDEX IF NOT EXISTS idx_job_no ON " + tableName + " (job_no)",
                    "CREATE INDEX IF NOT EXISTS idx_detail_time ON " + tableName + " (job_log_detail_time)",
                    "CREATE INDEX IF NOT EXISTS idx_level ON " + tableName + " (job_log_detail_level)"
            );
        }
        if (SysJobLogBackup.class == entityType) {
            return List.of(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                            + "job_log_backup_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                            + "job_log_backup_file_name VARCHAR(255),"
                            + "job_log_backup_file_path VARCHAR(500),"
                            + "job_log_backup_file_size BIGINT,"
                            + "job_log_backup_count BIGINT,"
                            + "job_log_backup_start_date DATE,"
                            + "job_log_backup_end_date DATE,"
                            + "job_log_backup_status VARCHAR(20) DEFAULT 'RUNNING',"
                            + "job_log_backup_type VARCHAR(20) DEFAULT 'MANUAL',"
                            + "job_log_backup_start_time TIMESTAMP NULL,"
                            + "job_log_backup_end_time TIMESTAMP NULL,"
                            + "job_log_backup_cost BIGINT,"
                            + "job_log_backup_message VARCHAR(1000),"
                            + "job_log_backup_compress_type VARCHAR(20) DEFAULT 'ZIP',"
                            + "job_log_backup_cleaned TINYINT DEFAULT 0,"
                            + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                            + ")",
                    "CREATE INDEX IF NOT EXISTS idx_backup_status ON " + tableName + " (job_log_backup_status)",
                    "CREATE INDEX IF NOT EXISTS idx_backup_type ON " + tableName + " (job_log_backup_type)",
                    "CREATE INDEX IF NOT EXISTS idx_backup_start_time ON " + tableName + " (job_log_backup_start_time)"
            );
        }
        throw new IllegalArgumentException("不支持的 Scheduler Job 实体类型: " + entityType.getName());
    }

    /**
     * 更新Or创建
     * @param executor executor
     * @param dataSource dataSource
     * @param metadata metadata
     * @param entityType entityType
     * @param namespace namespace
     * @param tableName tableName
     * @param actionType actionType
     */
    private void updateOrCreate(JdbcTableExecutor executor,
                                DataSource dataSource,
                                TableMetadata metadata,
                                Class<?> entityType,
                                String namespace,
                                String tableName,
                                ActionType actionType) {
        ErrorResult<String> result = executor.doIt(metadata, ActionType.UPDATE);
        if (result == null || !result.hasError()) {
            upgradeLegacySchema(dataSource, entityType, tableName, namespace);
            log.info("[Scheduler] 命名空间表初始化完成: namespace={}, table={}, mode={}", namespace, tableName, actionType);
            return;
        }
        if (isAlreadyExists(result)) {
            upgradeLegacySchema(dataSource, entityType, tableName, namespace);
            validateExistingTable(dataSource, metadata, namespace);
            log.info("[Scheduler] 命名空间表已存在，JdbcEngine UPDATE 已退化为结构校验通过: namespace={}, table={}, mode={}", namespace, tableName, actionType);
            return;
        }
        if (isTableMissing(result)) {
            execute(executor, metadata, ActionType.CREATE, namespace);
            upgradeLegacySchema(dataSource, entityType, tableName, namespace);
            log.info("[Scheduler] 命名空间表初始化完成: namespace={}, table={}, mode={}", namespace, tableName, actionType);
            return;
        }
        throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                + namespace + ", table=" + tableName + ", error=" + result.getFirstError());
    }

    /** 是否Already是否存在 */
    private boolean isAlreadyExists(ErrorResult<String> result) {
        String message = result == null ? null : result.getFirstError();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("already exists")
                || normalized.contains("已存在")
                || normalized.contains("duplicate");
    }

    /** 是否TableMissing */
    private boolean isTableMissing(ErrorResult<String> result) {
        String message = result == null ? null : result.getFirstError();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("doesn't exist")
                || normalized.contains("does not exist")
                || normalized.contains("not exist")
                || normalized.contains("unknown table")
                || normalized.contains("不存在")
                || normalized.contains("not found");
    }

    /** 校验ExistingTable */
    private void validateExistingTable(DataSource dataSource, TableMetadata metadata, String namespace) {
        Set<String> existingColumns = readExistingColumns(dataSource, metadata.getName(), namespace);
        List<String> missingColumns = metadata.getColumnMetadata().stream()
                .map(column -> normalizeColumnName(column.getColumnName()))
                .filter(columnName -> !existingColumns.contains(columnName))
                .toList();
        if (!missingColumns.isEmpty()) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + metadata.getName() + ", 缺少字段=" + missingColumns);
        }
        upgradeLegacyColumnTypes(dataSource, metadata.getName(), namespace);
    }

    /**
     * UpgradeLegacySchema
     * @param dataSource dataSource
     * @param entityType entityType
     * @param tableName tableName
     * @param namespace namespace
     */
    private void upgradeLegacySchema(DataSource dataSource,
                                     Class<?> entityType,
                                     String tableName,
                                     String namespace) {
        Map<String, String> columnTypes = readExistingColumnTypes(dataSource, tableName, namespace);
        List<String> upgradeSql = new ArrayList<>();
        if (SysJob.class == entityType) {
            collectBigintUpgradeSql(upgradeSql, tableName, columnTypes, "job_trigger_last_time");
            collectBigintUpgradeSql(upgradeSql, tableName, columnTypes, "job_trigger_next_time");
        }
        if (SysJobLog.class == entityType) {
            collectMissingColumnSql(upgradeSql, tableName, columnTypes,
                    "job_log_profile",
                    "ALTER TABLE `" + tableName + "` ADD COLUMN `job_log_profile` VARCHAR(50) NULL DEFAULT NULL COMMENT '环境' AFTER `job_log_trigger_type`");
            collectMissingColumnSql(upgradeSql, tableName, columnTypes,
                    "job_log_trigger_address",
                    "ALTER TABLE `" + tableName + "` ADD COLUMN `job_log_trigger_address` VARCHAR(255) NULL DEFAULT NULL COMMENT '触发地址' AFTER `job_log_trigger_param`");
        }
        if (SysJobLogDetail.class == entityType) {
            collectMissingColumnSql(upgradeSql, tableName, columnTypes,
                    "job_log_detail_profile",
                    "ALTER TABLE `" + tableName + "` ADD COLUMN `job_log_detail_profile` VARCHAR(100) NULL DEFAULT NULL COMMENT '执行环境/Profile' AFTER `job_log_detail_handler`");
            collectMissingColumnSql(upgradeSql, tableName, columnTypes,
                    "job_log_detail_address",
                    "ALTER TABLE `" + tableName + "` ADD COLUMN `job_log_detail_address` VARCHAR(255) NULL DEFAULT NULL COMMENT '执行地址' AFTER `job_log_detail_profile`");
        }
        if (upgradeSql.isEmpty()) {
            return;
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : upgradeSql) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName + ", 升级历史表结构失败", e);
        }

        log.info("[Scheduler] 命名空间表历史结构已升级: namespace={}, table={}, sqlCount={}",
                namespace, tableName, upgradeSql.size());
    }

    /** 读取ExistingColumns */
    private Set<String> readExistingColumns(DataSource dataSource, String tableName, String namespace) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Set<String> columns = new LinkedHashSet<>(readColumns(metaData, connection.getCatalog(), tableName));
            if (!columns.isEmpty()) {
                return columns;
            }
            columns.addAll(readColumns(metaData, null, tableName));
            if (!columns.isEmpty()) {
                return columns;
            }
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName + ", 读取现有表结构为空");
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName + ", 无法读取现有表结构", e);
        }
    }

    /**
     * 兼容旧命名空间任务表中仍然是 int 的时间戳列。
     * <p>
     * 远程调度中心创建或启动任务时会直接写毫秒时间戳，旧列类型如果没升级为 bigint，
     * 就会在这里出现溢出。平台启动时顺带修正这两个历史列，避免联调卡在旧库结构上。
     * </p>
     */
    private void upgradeLegacyColumnTypes(DataSource dataSource, String tableName, String namespace) {
        Map<String, String> columnTypes = readExistingColumnTypes(dataSource, tableName, namespace);
        List<String> upgradeSql = new ArrayList<>();
        collectBigintUpgradeSql(upgradeSql, tableName, columnTypes, "job_trigger_last_time");
        collectBigintUpgradeSql(upgradeSql, tableName, columnTypes, "job_trigger_next_time");
        if (upgradeSql.isEmpty()) {
            return;
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : upgradeSql) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName + ", 升级字段类型失败", e);
        }

        log.info("[Scheduler] 命名空间表字段类型已升级: namespace={}, table={}, columns={}",
                namespace, tableName, List.of("job_trigger_last_time", "job_trigger_next_time"));
    }

    /**
     * CollectBigintUpgradeSql
     * @param upgradeSql upgradeSql
     * @param tableName tableName
     * @param columnTypes columnTypes
     * @param columnName columnName
     */
    private void collectBigintUpgradeSql(List<String> upgradeSql,
                                         String tableName,
                                         Map<String, String> columnTypes,
                                         String columnName) {
        String type = columnTypes.get(normalizeColumnName(columnName));
        if (type == null || type.isBlank() || type.contains("bigint")) {
            return;
        }
        upgradeSql.add("ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + columnName + "` BIGINT NULL DEFAULT 0");
    }

    /**
     * CollectMissingColumnSql
     * @param upgradeSql upgradeSql
     * @param tableName tableName
     * @param columnTypes columnTypes
     * @param columnName columnName
     * @param sql sql
     */
    private void collectMissingColumnSql(List<String> upgradeSql,
                                         String tableName,
                                         Map<String, String> columnTypes,
                                         String columnName,
                                         String sql) {
        if (columnTypes.containsKey(normalizeColumnName(columnName))) {
            return;
        }
        upgradeSql.add(sql);
    }

    /** 读取ExistingColumnTypes */
    private Map<String, String> readExistingColumnTypes(DataSource dataSource, String tableName, String namespace) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Map<String, String> columnTypes = new LinkedHashMap<>(readColumnTypes(metaData, connection.getCatalog(), tableName));
            if (!columnTypes.isEmpty()) {
                return columnTypes;
            }
            columnTypes.putAll(readColumnTypes(metaData, null, tableName));
            if (!columnTypes.isEmpty()) {
                return columnTypes;
            }
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName + ", 读取现有字段类型为空");
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: namespace="
                    + namespace + ", table=" + tableName + ", 无法读取现有字段类型", e);
        }
    }

    /** 读取Columns */
    private Set<String> readColumns(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        Set<String> columns = new LinkedHashSet<>();
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, null)) {
            while (resultSet.next()) {
                columns.add(normalizeColumnName(resultSet.getString("COLUMN_NAME")));
            }
        }
        return columns;
    }

    /** 读取ColumnTypes */
    private Map<String, String> readColumnTypes(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, null)) {
            while (resultSet.next()) {
                columnTypes.put(normalizeColumnName(resultSet.getString("COLUMN_NAME")),
                        normalizeColumnType(resultSet.getString("TYPE_NAME")));
            }
        }
        return columnTypes;
    }

    /** NormalizeColumnName */
    private String normalizeColumnName(String columnName) {
        return columnName == null ? null : columnName.trim().toLowerCase(Locale.ROOT);
    }

    /** NormalizeColumnType */
    private String normalizeColumnType(String columnType) {
        return columnType == null ? null : columnType.trim().toLowerCase(Locale.ROOT);
    }

    /** DetectDatabaseProductName */
    private String detectDatabaseProductName(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new IllegalStateException("Scheduler 命名空间表初始化失败: 无法识别数据库类型", e);
        }
    }

    /** 是否Database */
    private boolean isH2Database(String databaseProductName) {
        if (databaseProductName == null) {
            return false;
        }
        return databaseProductName.trim().toLowerCase(Locale.ROOT).contains("h2");
    }

    /** 解析ActionType */
    private ActionType resolveActionType(JobProperties properties) {
        if (properties == null) {
            return null;
        }
        try {
            Method getter = properties.getClass().getMethod("getTableInitMode");
            Object initMode = getter.invoke(properties);
            if (initMode == null) {
                return null;
            }
            Method enabledMethod = initMode.getClass().getMethod("isEnabled");
            Object enabled = enabledMethod.invoke(initMode);
            if (!(enabled instanceof Boolean flag) || !flag) {
                return null;
            }
            Method actionMethod = initMode.getClass().getMethod("toActionType");
            Object action = actionMethod.invoke(initMode);
            return action instanceof ActionType actionType ? actionType : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}

