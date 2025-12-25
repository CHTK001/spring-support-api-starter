package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.filter.DataFilter;
import com.chua.common.support.task.sync.filter.FieldMappingFilter;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 字段映射过滤器配置适配器
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class FieldMappingFilterConfigAdapter implements SpiConfigAdapter<DataFilter> {

    @Override
    public String getSpiName() {
        return "fieldMapping";
    }

    @Override
    public String getSpiType() {
        return "FILTER";
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataFilter create(Map<String, Object> config) {
        String name = getString(config, "name", "fieldMappingFilter");
        int order = getInt(config, "order", 0);
        boolean keepUnmapped = getBoolean(config, "keepUnmapped", true);

        FieldMappingFilter.Builder builder = FieldMappingFilter.builder()
                .name(name)
                .order(order)
                .keepUnmapped(keepUnmapped);

        // 处理重命名映射
        Object renameObj = config.get("rename");
        if (renameObj instanceof Map) {
            Map<String, String> rename = (Map<String, String>) renameObj;
            builder.rename(rename);
        }

        // 处理选择字段
        Object selectObj = config.get("select");
        if (selectObj instanceof String[]) {
            builder.select((String[]) selectObj);
        } else if (selectObj instanceof String) {
            builder.select(((String) selectObj).split(","));
        }

        // 处理删除字段
        Object removeObj = config.get("remove");
        if (removeObj instanceof String[]) {
            builder.remove((String[]) removeObj);
        } else if (removeObj instanceof String) {
            builder.remove(((String) removeObj).split(","));
        }

        // 处理默认值
        Object defaultValuesObj = config.get("defaultValues");
        if (defaultValuesObj instanceof Map) {
            Map<String, Object> defaultValues = (Map<String, Object>) defaultValuesObj;
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                builder.defaultValue(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    @Override
    public String test(Map<String, Object> config) {
        return null;
    }

    @Override
    public String validate(Map<String, Object> config) {
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
