package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.filter.DataFilter;
import com.chua.common.support.task.sync.filter.ValidationFilter;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据验证过滤器配置适配器
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class ValidationFilterConfigAdapter implements SpiConfigAdapter<DataFilter> {

    @Override
    public String getSpiName() {
        return "validation";
    }

    @Override
    public String getSpiType() {
        return "FILTER";
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataFilter create(Map<String, Object> config) {
        String name = getString(config, "name", "validationFilter");
        int order = getInt(config, "order", 0);
        boolean discardOnFail = getBoolean(config, "discardOnFail", false);
        boolean throwOnFail = getBoolean(config, "throwOnFail", false);

        ValidationFilter.Builder builder = ValidationFilter.builder()
                .name(name)
                .order(order)
                .discardOnFail(discardOnFail)
                .throwOnFail(throwOnFail);

        // 处理必填字段
        Object requiredObj = config.get("required");
        if (requiredObj instanceof List) {
            List<String> required = (List<String>) requiredObj;
            builder.required(required.toArray(new String[0]));
        } else if (requiredObj instanceof String) {
            builder.required(((String) requiredObj).split(","));
        }

        // 处理验证规则
        Object rulesObj = config.get("rules");
        if (rulesObj instanceof List) {
            List<Map<String, Object>> rules = (List<Map<String, Object>>) rulesObj;
            for (Map<String, Object> rule : rules) {
                String type = getString(rule, "type", "");
                String field = getString(rule, "field", "");

                switch (type) {
                    case "required":
                        builder.required(field);
                        break;
                    case "type":
                        String className = getString(rule, "class", "String");
                        Class<?> clazz = getClassByName(className);
                        if (clazz != null) {
                            builder.type(field, clazz);
                        }
                        break;
                    case "range":
                        Number min = (Number) rule.getOrDefault("min", 0);
                        Number max = (Number) rule.getOrDefault("max", Integer.MAX_VALUE);
                        builder.range(field, min, max);
                        break;
                    case "pattern":
                        String pattern = getString(rule, "pattern", ".*");
                        builder.pattern(field, pattern);
                        break;
                    case "length":
                        int minLen = getInt(rule, "minLength", 0);
                        int maxLen = getInt(rule, "maxLength", Integer.MAX_VALUE);
                        builder.length(field, minLen, maxLen);
                        break;
                    case "in":
                        Object valuesObj = rule.get("values");
                        if (valuesObj instanceof List) {
                            builder.in(field, ((List<?>) valuesObj).toArray());
                        }
                        break;
                }
            }
        }

        return builder.build();
    }

    private Class<?> getClassByName(String className) {
        return switch (className.toLowerCase()) {
            case "string" -> String.class;
            case "integer", "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "boolean" -> Boolean.class;
            default -> null;
        };
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
