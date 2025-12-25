package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.center.LocalDataBus;
import com.chua.common.support.task.sync.input.LocalSourceStream;
import com.chua.common.support.task.sync.output.LocalSinkStream;
import com.chua.common.support.task.sync.stream.DataStream;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 本地 DataStream 配置适配器
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class LocalDataStreamConfigAdapter implements SpiConfigAdapter<DataStream> {

    @Override
    public String getSpiName() {
        return "local";
    }

    @Override
    public String getSpiType() {
        return "STREAM";
    }

    @Override
    public DataStream create(Map<String, Object> config) {
        String direction = getString(config, "direction", "INPUT");
        String id = getString(config, "id", UUID.randomUUID().toString());
        String name = getString(config, "name", "local-stream");
        int capacity = getInt(config, "capacity", 10000);

        // 创建本地数据总线
        LocalDataBus dataBus = new LocalDataBus(name + "-bus", capacity);

        if ("INPUT".equalsIgnoreCase(direction)) {
            return new LocalSourceStream(id, dataBus);
        } else {
            return new LocalSinkStream(id, dataBus);
        }
    }

    @Override
    public String test(Map<String, Object> config) {
        // 本地数据流无需测试连接
        return null;
    }

    @Override
    public String validate(Map<String, Object> config) {
        String direction = getString(config, "direction", null);
        if (direction == null) {
            return "direction 不能为空，必须为 INPUT 或 OUTPUT";
        }
        if (!"INPUT".equalsIgnoreCase(direction) && !"OUTPUT".equalsIgnoreCase(direction)) {
            return "direction 必须为 INPUT 或 OUTPUT";
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
