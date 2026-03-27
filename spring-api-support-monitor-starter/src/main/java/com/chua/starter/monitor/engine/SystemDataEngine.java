package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.data.query.definition.query.LambdaSearch;
import com.chua.common.support.data.query.definition.query.LambdaUpdate;
import com.chua.common.support.document.parser.Document;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.storage.backup.Backup;
import com.chua.common.support.storage.backup.BackupSetting;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.data.DeleteRowRequest;
import com.chua.starter.monitor.pojo.data.InsertRowRequest;
import com.chua.starter.monitor.pojo.data.UpdateRowRequest;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;

import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 监控模块内部统一的数据引擎抽象。
 *
 * <p>对外部 Engine / Backup / Metadata SPI 做一层收口，避免业务服务直接散落依赖。</p>
 */
public interface SystemDataEngine extends AutoCloseable {

    /**
     * 当前数据源配置。
     */
    SystemDataSetting getSetting();

    /**
     * 当前数据源连接选项。
     */
    DataSourceOptions getOptions();

    /**
     * 是否支持层级元数据。
     */
    default boolean supportsHierarchy() {
        return supports(SystemDataCapability.HIERARCHY);
    }

    /**
     * 获取层级元数据。
     */
    default HierarchyMetadata hierarchy() {
        throw unsupported("HierarchyMetadata");
    }

    /**
     * 是否支持命令执行。
     */
    default boolean supportsCommand() {
        return supports(SystemDataCapability.COMMAND);
    }

    /**
     * 获取命令元数据执行器。
     */
    default CommandMetadata command() {
        throw unsupported("CommandMetadata");
    }

    /**
     * 是否支持文档导出。
     */
    default boolean supportsDocument() {
        return supports(SystemDataCapability.DOCUMENT);
    }

    /**
     * 获取文档导出组件。
     */
    default Document document() {
        throw unsupported("Document");
    }

    /**
     * 是否支持 ORM 查询 DSL。
     */
    default boolean supportsOrmSearch() {
        return supports(SystemDataCapability.ORM_SEARCH);
    }

    /**
     * ORM 查询 DSL 支持级别。
     */
    default FeatureSupportLevel ormSearchSupportLevel() {
        return capabilitySupport(SystemDataCapability.ORM_SEARCH);
    }

    /**
     * 构建 ORM 查询 DSL。
     */
    default <T> LambdaSearch<T, ?> search(Class<T> targetType) {
        throw unsupported("ORM Search");
    }

    /**
     * 是否支持 ORM 更新 DSL。
     */
    default boolean supportsOrmUpdate() {
        return supports(SystemDataCapability.ORM_UPDATE);
    }

    /**
     * ORM 更新 DSL 支持级别。
     */
    default FeatureSupportLevel ormUpdateSupportLevel() {
        return capabilitySupport(SystemDataCapability.ORM_UPDATE);
    }

    /**
     * 构建 ORM 更新 DSL。
     */
    default <T> LambdaUpdate<T, ?> update(Class<T> targetType) {
        throw unsupported("ORM Update");
    }

    /**
     * 是否支持参数化 SQL 查询。
     */
    default boolean supportsSqlQuery() {
        return supports(SystemDataCapability.SQL_QUERY);
    }

    /**
     * 执行参数化查询。
     */
    default List<Map<String, Object>> query(String sql, Object... args) throws Exception {
        throw unsupported("SQL Query");
    }

    /**
     * 是否支持参数化 SQL 更新。
     */
    default boolean supportsSqlUpdate() {
        return supports(SystemDataCapability.SQL_UPDATE);
    }

    /**
     * 执行参数化更新。
     */
    default int executeUpdate(String sql, Object... args) throws Exception {
        throw unsupported("SQL Update");
    }

    /**
     * 是否支持备份生命周期管理。
     */
    default boolean supportsBackupLifecycle() {
        return supports(SystemDataCapability.BACKUP_LIFECYCLE);
    }

    /**
     * 创建备份生命周期对象。
     */
    default Backup createBackup(BackupSetting backupSetting) {
        throw unsupported("Backup Lifecycle");
    }

    /**
     * 是否支持表级备份能力。
     */
    default boolean supportsTableBackup() {
        return supports(SystemDataCapability.TABLE_BACKUP);
    }

    /**
     * 是否支持结构操作。
     */
    default boolean supportsStructure() {
        return supports(SystemDataCapability.STRUCTURE);
    }

    /**
     * 获取结构操作组件。
     */
    default StructureMetadata structure() {
        throw unsupported("StructureMetadata");
    }

    /**
     * 是否支持索引管理。
     */
    default boolean supportsIndexes() {
        return supports(SystemDataCapability.INDEXES);
    }

    /**
     * 是否支持分区管理。
     */
    default boolean supportsPartitions() {
        return supports(SystemDataCapability.PARTITIONS);
    }

    /**
     * 是否支持行级数据写入。
     */
    default boolean supportsRowMutation() {
        return supports(SystemDataCapability.ROW_MUTATION);
    }

    /**
     * 获取能力支持级别。
     */
    default FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        return switch (capability) {
            case BACKUP -> FeatureSupportLevel.max(
                    capabilitySupport(SystemDataCapability.BACKUP_LIFECYCLE),
                    capabilitySupport(SystemDataCapability.TABLE_BACKUP)
            );
            case FILE -> isFileMode() ? FeatureSupportLevel.FULL : FeatureSupportLevel.NONE;
            case KV, TIME_SERIES, TOPIC, TOPIC_ADMIN, CLUSTER, NODE -> directFeatureSupport(this, capability.getFeatureType());
            default -> FeatureSupportLevel.NONE;
        };
    }

    /**
     * 是否支持指定能力。
     */
    default boolean supports(SystemDataCapability capability) {
        return capabilitySupport(capability) != FeatureSupportLevel.NONE;
    }

    /**
     * 是否以指定级别支持某个能力。
     */
    default boolean supports(SystemDataCapability capability, FeatureSupportLevel supportLevel) {
        return capabilitySupport(capability).supports(supportLevel);
    }

    /**
     * 断言指定能力可用。
     */
    default void require(SystemDataCapability capability) {
        if (!supports(capability)) {
            throw unsupported(capability == null ? "Unknown Capability" : capability.getKey());
        }
    }

    /**
     * 获取特性接口。
     */
    default <T> T feature(Class<T> featureType) {
        return null;
    }

    /**
     * 指定特性的支持级别。
     */
    default FeatureSupportLevel featureSupport(Class<?> featureType) {
        SystemDataCapability capability = SystemDataCapability.fromFeatureType(featureType);
        if (capability != null) {
            return capabilitySupport(capability);
        }
        return directFeatureSupport(this, featureType);
    }

    /**
     * 是否支持指定特性。
     */
    default boolean supportsFeature(Class<?> featureType) {
        return featureSupport(featureType) != FeatureSupportLevel.NONE;
    }

    /**
     * 是否以指定级别支持某个特性。
     */
    default boolean supportsFeature(Class<?> featureType, FeatureSupportLevel supportLevel) {
        return featureSupport(featureType).supports(supportLevel);
    }

    /**
     * 断言并获取指定特性。
     */
    default <T> T requireFeature(Class<T> featureType) {
        T feature = feature(featureType);
        if (feature != null) {
            return feature;
        }
        throw unsupported(featureType == null ? "Feature" : featureType.getSimpleName());
    }

    /**
     * 断言指定能力后获取对应特性。
     */
    default <T> T requireFeature(SystemDataCapability capability, Class<T> featureType) {
        require(capability);
        return requireFeature(featureType);
    }

    /**
     * 重命名表。
     */
    default boolean renameTable(String nodePath, String newName) {
        throw unsupported("Rename Table");
    }

    /**
     * 备份表。
     */
    default boolean backupTable(String nodePath, String backupName) {
        throw unsupported("Backup Table");
    }

    /**
     * 添加字段。
     */
    default boolean addColumn(StructureMetadata.AddColumnRequest request) {
        throw unsupported("Add Column");
    }

    /**
     * 修改字段。
     */
    default boolean modifyColumn(StructureMetadata.ModifyColumnRequest request) {
        throw unsupported("Modify Column");
    }

    /**
     * 删除字段。
     */
    default boolean dropColumn(StructureMetadata.DropColumnRequest request) {
        throw unsupported("Drop Column");
    }

    /**
     * 批量删除字段。
     */
    default boolean batchDropColumn(StructureMetadata.BatchDropColumnRequest request) {
        throw unsupported("Batch Drop Column");
    }

    /**
     * 批量添加字段。
     */
    default boolean batchAddColumn(StructureMetadata.BatchAddColumnRequest request) {
        throw unsupported("Batch Add Column");
    }

    /**
     * 批量修改字段。
     */
    default boolean batchModifyColumn(StructureMetadata.BatchModifyColumnRequest request) {
        throw unsupported("Batch Modify Column");
    }

    /**
     * 调整字段顺序。
     */
    default boolean reorderColumn(StructureMetadata.ReorderColumnRequest request) {
        throw unsupported("Reorder Column");
    }

    /**
     * 修改表注释。
     */
    default boolean modifyTableComment(StructureMetadata.ModifyTableCommentRequest request) {
        throw unsupported("Modify Table Comment");
    }

    /**
     * 批量修改表结构。
     */
    default boolean batchModifyTableStructure(StructureMetadata.BatchTableStructureRequest request) {
        throw unsupported("Batch Modify Table Structure");
    }

    /**
     * 获取索引列表。
     */
    default List<Map<String, Object>> getIndexes(String tableName) {
        throw unsupported("Index Query");
    }

    /**
     * 创建索引。
     */
    default boolean createIndex(StructureMetadata.CreateIndexRequest request) {
        throw unsupported("Create Index");
    }

    /**
     * 删除索引。
     */
    default boolean dropIndex(String tableName, String indexName) {
        throw unsupported("Drop Index");
    }

    /**
     * 获取分区列表。
     */
    default List<Map<String, Object>> getPartitions(String tableName) {
        throw unsupported("Partition Query");
    }

    /**
     * 创建分区。
     */
    default boolean createPartition(StructureMetadata.CreatePartitionRequest request) {
        throw unsupported("Create Partition");
    }

    /**
     * 删除分区。
     */
    default boolean dropPartition(String tableName, String partitionName) {
        throw unsupported("Drop Partition");
    }

    /**
     * 更新单元格。
     */
    default boolean updateRow(UpdateRowRequest request) {
        throw unsupported("Update Row");
    }

    /**
     * 删除行。
     */
    default boolean deleteRow(DeleteRowRequest request) {
        throw unsupported("Delete Row");
    }

    /**
     * 插入行。
     */
    default boolean insertRow(InsertRowRequest request) {
        throw unsupported("Insert Row");
    }

    /**
     * 是否是文件模式数据源。
     */
    default boolean isFileMode() {
        return "FILE".equalsIgnoreCase(getSetting().getSystemDataSettingMode());
    }

    /**
     * 能力级别快照。
     */
    default Map<String, FeatureSupportLevel> capabilityLevels() {
        LinkedHashMap<String, FeatureSupportLevel> levels = new LinkedHashMap<>();
        for (SystemDataCapability capability : SystemDataCapability.values()) {
            levels.put(capability.getKey(), capabilitySupport(capability));
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(levels));
    }

    /**
     * 能力快照。
     */
    default Map<String, Boolean> capabilities() {
        LinkedHashMap<String, Boolean> capabilities = new LinkedHashMap<>();
        capabilityLevels().forEach((key, value) -> capabilities.put(key, value != FeatureSupportLevel.NONE));
        return Collections.unmodifiableMap(new LinkedHashMap<>(capabilities));
    }

    /**
     * 暴露底层原生引擎/客户端。
     */
    default <T> T unwrap(Class<T> targetType) {
        throw unsupported(targetType == null ? "Native Engine" : targetType.getSimpleName());
    }

    @Override
    default void close() throws Exception {
    }

    private static UnsupportedOperationException unsupported(String feature) {
        return new UnsupportedOperationException("当前数据源不支持能力: " + feature);
    }

    private static FeatureSupportLevel directFeatureSupport(SystemDataEngine engine, Class<?> featureType) {
        return featureType != null && engine.feature(featureType) != null
                ? FeatureSupportLevel.FULL
                : FeatureSupportLevel.NONE;
    }
}
