package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.document.parser.Document;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.command.CommandMetadataFactory;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadataFactory;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.storage.backup.Backup;
import com.chua.common.support.storage.backup.BackupSetting;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于现有 SPI 的默认数据引擎实现。
 */
public class LegacySpiSystemDataEngine extends AbstractSystemDataEngine {

    public LegacySpiSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        String settingType = getSetting().getSystemDataSettingType();
        return switch (capability) {
            case HIERARCHY -> ServiceProvider.of(HierarchyMetadata.class).isSupport(settingType)
                    ? FeatureSupportLevel.FULL
                    : FeatureSupportLevel.NONE;
            case COMMAND -> ServiceProvider.of(CommandMetadata.class).isSupport(settingType)
                    ? FeatureSupportLevel.FULL
                    : FeatureSupportLevel.NONE;
            case DOCUMENT -> ServiceProvider.of(Document.class).isSupport(settingType)
                    ? FeatureSupportLevel.FULL
                    : FeatureSupportLevel.NONE;
            case ORM_SEARCH, ORM_UPDATE -> supports(SystemDataCapability.COMMAND) && resolveOrmDialect() != null
                    ? FeatureSupportLevel.ATTACHED
                    : FeatureSupportLevel.NONE;
            case BACKUP_LIFECYCLE -> ServiceProvider.of(Backup.class).isSupport(settingType)
                    ? FeatureSupportLevel.FULL
                    : FeatureSupportLevel.NONE;
            case STRUCTURE -> ServiceProvider.of(StructureMetadata.class).isSupport(settingType)
                    ? FeatureSupportLevel.FULL
                    : FeatureSupportLevel.NONE;
            case INDEXES -> capabilitySupport(SystemDataCapability.STRUCTURE);
            default -> super.capabilitySupport(capability);
        };
    }

    @Override
    public HierarchyMetadata hierarchy() {
        SystemDataSetting setting = getSetting();
        return HierarchyMetadataFactory.createHierarchy(
                String.valueOf(setting.getSystemDataSettingId()),
                setting.getSystemDataSettingType(),
                getOptions()
        );
    }

    public CommandMetadata command() {
        SystemDataSetting setting = getSetting();
        return CommandMetadataFactory.create(
                setting.getSystemDataSettingId(),
                setting.getSystemDataSettingType(),
                getOptions()
        );
    }

    public Document document() {
        return ServiceProvider.of(Document.class)
                .getNewExtension(getSetting().getSystemDataSettingType(), getOptions());
    }

    @Override
    public <T> Search<T> search(Class<T> targetType) {
        require(SystemDataCapability.ORM_SEARCH);
        return new CommandSearchSystemDataEngineAdapter<>(targetType, command(), resolveOrmDialect());
    }

    public <T> Update<T> update(Class<T> targetType) {
        require(SystemDataCapability.ORM_UPDATE);
        return new CommandUpdateSystemDataEngineAdapter<>(targetType, command(), resolveOrmDialect());
    }

    public Backup createBackup(BackupSetting backupSetting) {
        return Backup.createBackup(getSetting().getSystemDataSettingType(), getOptions(), backupSetting);
    }

    @Override
    public StructureMetadata structure() {
        return ServiceProvider.of(StructureMetadata.class)
                .getNewExtension(getSetting().getSystemDataSettingType(), getOptions());
    }

    @Override
    public List<Map<String, Object>> getIndexes(String tableName) {
        if (!supports(SystemDataCapability.INDEXES)) {
            return Collections.emptyList();
        }
        return structure().getIndexes(tableName);
    }

    @Override
    public boolean createIndex(StructureMetadata.CreateIndexRequest request) {
        return supports(SystemDataCapability.INDEXES) && structure().createIndex(request);
    }

    @Override
    public boolean dropIndex(String tableName, String indexName) {
        return supports(SystemDataCapability.INDEXES) && structure().dropIndex(tableName, indexName);
    }

    @Override
    public List<Map<String, Object>> getPartitions(String tableName) {
        if (!supports(SystemDataCapability.PARTITIONS)) {
            return Collections.emptyList();
        }
        return structure().getPartitions(tableName);
    }

    @Override
    public boolean createPartition(StructureMetadata.CreatePartitionRequest request) {
        return supports(SystemDataCapability.PARTITIONS) && structure().createPartition(request);
    }

    @Override
    public boolean dropPartition(String tableName, String partitionName) {
        return supports(SystemDataCapability.PARTITIONS) && structure().dropPartition(tableName, partitionName);
    }
}
