package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.input.CsvInput;
import com.chua.common.support.task.sync.input.Input;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * CSV Input 配置适配器
 *
 * @author CH
 * @since 2024/12/21
 */
@Slf4j
@Component
public class CsvInputConfigAdapter implements SpiConfigAdapter<Input> {

    @Override
    public String getSpiName() {
        return "csv";
    }

    @Override
    public String getSpiType() {
        return "INPUT";
    }

    @Override
    public Input create(Map<String, Object> config) {
        String filePath = getString(config, "filePath", null);
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        Path path = Paths.get(filePath);
        String delimiter = getString(config, "delimiter", ",");
        String charset = getString(config, "charset", "UTF-8");
        int linesToSkip = getInt(config, "linesToSkip", 0);
        boolean hasHeader = getBoolean(config, "hasHeader", true);
        String name = getString(config, "name", "csv-input");

        return CsvInput.builder()
                .name(name)
                .filePath(path)
                .delimiter(delimiter)
                .charset(Charset.forName(charset))
                .linesToSkip(linesToSkip)
                .hasHeader(hasHeader)
                .build();
    }

    @Override
    public String test(Map<String, Object> config) {
        String filePath = getString(config, "filePath", null);
        if (filePath == null || filePath.trim().isEmpty()) {
            return "文件路径不能为空";
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return "文件不存在: " + filePath;
        }

        if (!Files.isReadable(path)) {
            return "文件不可读: " + filePath;
        }

        return null;
    }

    @Override
    public String validate(Map<String, Object> config) {
        String filePath = getString(config, "filePath", null);
        if (filePath == null || filePath.trim().isEmpty()) {
            return "文件路径不能为空";
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
