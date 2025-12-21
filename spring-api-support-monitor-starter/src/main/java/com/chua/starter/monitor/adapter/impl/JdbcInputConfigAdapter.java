package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.input.Input;
import com.chua.common.support.task.sync.input.JdbcInput;
import com.chua.common.support.task.sync.input.builder.JdbcInputBuilder;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import com.chua.starter.monitor.util.SyncDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

/**
 * JDBC Input 配置适配器
 *
 * @author CH
 * @since 2024/12/21
 */
@Slf4j
@Component
public class JdbcInputConfigAdapter implements SpiConfigAdapter<Input> {

    @Override
    public String getSpiName() {
        return "jdbc";
    }

    @Override
    public String getSpiType() {
        return "INPUT";
    }

    @Override
    public Input create(Map<String, Object> config) {
        // 创建 DataSource
        DataSource dataSource = SyncDataSourceFactory.createDataSource(config);

        // 获取配置参数
        String sql = getString(config, "sql", null);
        int fetchSize = getInt(config, "fetchSize", 1000);
        String name = getString(config, "name", "jdbc-input");

        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("查询SQL不能为空");
        }

        // 创建 JdbcInput
        return JdbcInput.builder()
                .name(name)
                .dataSource(dataSource)
                .sql(sql)
                .fetchSize(fetchSize)
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

        String sql = getString(config, "sql", null);
        if (sql == null || sql.trim().isEmpty()) {
            return "查询SQL不能为空";
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
}
