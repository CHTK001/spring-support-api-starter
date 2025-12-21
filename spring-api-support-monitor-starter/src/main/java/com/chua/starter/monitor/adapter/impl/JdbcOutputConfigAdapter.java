package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.output.JdbcOutput;
import com.chua.common.support.task.sync.output.Output;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import com.chua.starter.monitor.util.SyncDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

/**
 * JDBC Output 配置适配器
 *
 * @author CH
 * @since 2024/12/21
 */
@Slf4j
@Component
public class JdbcOutputConfigAdapter implements SpiConfigAdapter<Output> {

    @Override
    public String getSpiName() {
        return "jdbc";
    }

    @Override
    public String getSpiType() {
        return "OUTPUT";
    }

    @Override
    public Output create(Map<String, Object> config) {
        // 创建 DataSource
        DataSource dataSource = SyncDataSourceFactory.createDataSource(config);

        // 获取配置参数
        String table = getString(config, "table", null);
        String insertSql = getString(config, "insertSql", null);
        int batchSize = getInt(config, "batchSize", 1000);
        boolean autoCommit = getBoolean(config, "autoCommit", false);
        String name = getString(config, "name", "jdbc-output");

        if (table == null || table.trim().isEmpty()) {
            throw new IllegalArgumentException("目标表名不能为空");
        }

        // 创建 JdbcOutput
        return JdbcOutput.builder()
                .name(name)
                .dataSource(dataSource)
                .tableName(table)
                .insertSql(insertSql)
                .batchSize(batchSize)
                .autoCommit(autoCommit)
                .build();
    }

    @Override
    public String test(Map<String, Object> config) {
        return SyncDataSourceFactory.testConnection(config);
    }

    @Override
    public String validate(Map<String, Object> config) {
        String url = getString(config, "url", null);
        if (url == null || url.trim().isEmpty()) {
            return "数据库URL不能为空";
        }

        String table = getString(config, "table", null);
        if (table == null || table.trim().isEmpty()) {
            return "目标表名不能为空";
        }

        return null;
    }

    private String getString(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    private int getInt(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBoolean(Map<String, Object> config, String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
