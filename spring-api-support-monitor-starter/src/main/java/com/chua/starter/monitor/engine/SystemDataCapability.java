package com.chua.starter.monitor.engine;

import com.chua.starter.monitor.engine.feature.SystemDataClusterFeature;
import com.chua.starter.monitor.engine.feature.SystemDataKvFeature;
import com.chua.starter.monitor.engine.feature.SystemDataNodeFeature;
import com.chua.starter.monitor.engine.feature.SystemDataTimeSeriesFeature;
import com.chua.starter.monitor.engine.feature.SystemDataTopicAdminFeature;
import com.chua.starter.monitor.engine.feature.SystemDataTopicFeature;

/**
 * 监控模块统一能力定义。
 */
public enum SystemDataCapability {
    BACKUP("backup", "supportsBackup"),
    BACKUP_LIFECYCLE("backupLifecycle", "supportsBackupLifecycle"),
    TABLE_BACKUP("tableBackup", "supportsTableBackup"),
    DOCUMENT("document", "supportsDocument"),
    FILE("file", "supportsFile"),
    HIERARCHY("hierarchy", "supportsHierarchy"),
    COMMAND("command", "supportsCommand"),
    ORM_SEARCH("ormSearch", "supportsOrmSearch"),
    ORM_UPDATE("ormUpdate", "supportsOrmUpdate"),
    SQL_QUERY("sqlQuery", "supportsSqlQuery"),
    SQL_UPDATE("sqlUpdate", "supportsSqlUpdate"),
    STRUCTURE("structure", "supportsStructure"),
    INDEXES("indexes", "supportsIndexes"),
    PARTITIONS("partitions", "supportsPartitions"),
    ROW_MUTATION("rowMutation", "supportsRowMutation"),
    KV("kv", "supportsKv", SystemDataKvFeature.class),
    TIME_SERIES("timeSeries", "supportsTimeSeries", SystemDataTimeSeriesFeature.class),
    TOPIC("topic", "supportsTopic", SystemDataTopicFeature.class),
    TOPIC_ADMIN("topicAdmin", "supportsTopicAdmin", SystemDataTopicAdminFeature.class),
    CLUSTER("cluster", "supportsCluster", SystemDataClusterFeature.class),
    NODE("node", "supportsNode", SystemDataNodeFeature.class);

    private final String key;
    private final String legacyAliasKey;
    private final Class<?> featureType;

    SystemDataCapability(String key, String legacyAliasKey) {
        this(key, legacyAliasKey, null);
    }

    SystemDataCapability(String key, String legacyAliasKey, Class<?> featureType) {
        this.key = key;
        this.legacyAliasKey = legacyAliasKey;
        this.featureType = featureType;
    }

    public String getKey() {
        return key;
    }

    public String getLegacyAliasKey() {
        return legacyAliasKey;
    }

    public Class<?> getFeatureType() {
        return featureType;
    }

    public static SystemDataCapability fromFeatureType(Class<?> featureType) {
        if (featureType == null) {
            return null;
        }
        for (SystemDataCapability capability : values()) {
            if (capability.featureType == featureType) {
                return capability;
            }
        }
        return null;
    }
}
