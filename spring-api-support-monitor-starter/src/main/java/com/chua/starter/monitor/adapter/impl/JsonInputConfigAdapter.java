package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.input.Input;
import com.chua.common.support.task.sync.input.JsonInput;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * JSON Input 配置适配器
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class JsonInputConfigAdapter implements SpiConfigAdapter<Input> {

    @Override
    public String getSpiName() {
        return "json";
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
        String charset = getString(config, "charset", "UTF-8");
        boolean jsonLines = getBoolean(config, "jsonLines", false);
        String rootPath = getString(config, "rootPath", null);
        String name = getString(config, "name", "json-input");

        return JsonInput.builder()
                .name(name)
                .filePath(path)
                .charset(Charset.forName(charset))
                .jsonLines(jsonLines)
                .rootPath(rootPath)
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
