package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.metadata.base.SchemaMetadata;
import com.chua.starter.monitor.entity.SystemDataSetting;

/**
 * ZooKeeper 专用引擎。
 */
public class ZookeeperSystemDataEngine extends LegacySpiSystemDataEngine {

    private SchemaMetadata schemaMetadata;

    public ZookeeperSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public <T> T unwrap(Class<T> targetType) {
        if (targetType != null && targetType.isInstance(ensureSchemaMetadata())) {
            return targetType.cast(ensureSchemaMetadata());
        }
        return super.unwrap(targetType);
    }

    @Override
    public void close() throws Exception {
        if (schemaMetadata != null) {
            closeQuietly(schemaMetadata);
        }
        super.close();
    }

    private SchemaMetadata ensureSchemaMetadata() {
        if (schemaMetadata == null) {
            schemaMetadata = resolveExtension(SchemaMetadata.class, defaultExtensionNames("zookeeper", "zk"));
        }
        return schemaMetadata;
    }
}
