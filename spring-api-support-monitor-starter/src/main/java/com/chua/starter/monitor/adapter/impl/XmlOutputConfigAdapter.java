package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.output.Output;
import com.chua.common.support.task.sync.output.XmlOutput;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * XML Output 配置适配器
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class XmlOutputConfigAdapter implements SpiConfigAdapter<Output> {

    @Override
    public String getSpiName() {
        return "xml";
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
        String charset = getString(config, "charset", "UTF-8");
        String rootElement = getString(config, "rootElement", "data");
        String itemElement = getString(config, "itemElement", "item");
        boolean prettyPrint = getBoolean(config, "prettyPrint", true);
        String name = getString(config, "name", "xml-output");

        return XmlOutput.builder()
                .name(name)
                .filePath(path)
                .charset(Charset.forName(charset))
                .rootElement(rootElement)
                .itemElement(itemElement)
                .prettyPrint(prettyPrint)
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
