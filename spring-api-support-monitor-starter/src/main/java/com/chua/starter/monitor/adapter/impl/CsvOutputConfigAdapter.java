package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.output.CsvOutput;
import com.chua.common.support.task.sync.output.Output;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * CSV Output 配置适配器
 *
 * @author CH
 * @since 2024/12/21
 */
@Slf4j
@Component
public class CsvOutputConfigAdapter implements SpiConfigAdapter<Output> {

    @Override
    public String getSpiName() {
        return "csv";
    }

    @Override
    public String getSpiType() {
        return "OUTPUT";
    }

    @Override
    public Output create(Map<String, Object> config) {
        String filePath = getString(config, "filePath", null);
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        Path path = Paths.get(filePath);
        String delimiter = getString(config, "delimiter", ",");
        String charset = getString(config, "charset", "UTF-8");
        boolean writeHeader = getBoolean(config, "writeHeader", true);
        boolean append = getBoolean(config, "append", false);
        String name = getString(config, "name", "csv-output");

        return CsvOutput.builder()
                .name(name)
                .filePath(path)
                .delimiter(delimiter)
                .charset(Charset.forName(charset))
                .writeHeader(writeHeader)
                .append(append)
                .build();
    }

    @Override
    public String test(Map<String, Object> config) {
        String filePath = getString(config, "filePath", null);
        if (filePath == null || filePath.trim().isEmpty()) {
            return "文件路径不能为空";
        }

        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (Exception e) {
                return "无法创建目录: " + parent;
            }
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
