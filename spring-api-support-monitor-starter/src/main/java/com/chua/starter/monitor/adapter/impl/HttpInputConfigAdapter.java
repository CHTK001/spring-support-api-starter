package com.chua.starter.monitor.adapter.impl;

import com.chua.common.support.task.sync.input.HttpInput;
import com.chua.common.support.task.sync.input.Input;
import com.chua.starter.monitor.adapter.SpiConfigAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * HTTP Input 配置适配器
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
public class HttpInputConfigAdapter implements SpiConfigAdapter<Input> {

    @Override
    public String getSpiName() {
        return "http";
    }

    @Override
    public String getSpiType() {
        return "INPUT";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Input create(Map<String, Object> config) {
        String url = getString(config, "url", null);
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL不能为空");
        }

        String method = getString(config, "method", "GET");
        String charset = getString(config, "charset", "UTF-8");
        int connectTimeout = getInt(config, "connectTimeout", 30000);
        int readTimeout = getInt(config, "readTimeout", 60000);
        String dataPath = getString(config, "dataPath", null);
        int pageSize = getInt(config, "pageSize", 0);
        int maxPages = getInt(config, "maxPages", 0);
        String name = getString(config, "name", "http-input");

        var builder = HttpInput.builder()
                .name(name)
                .url(url)
                .method(method)
                .charset(Charset.forName(charset))
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .dataPath(dataPath);

        if (pageSize > 0) {
            builder.pageSize(pageSize);
        }
        if (maxPages > 0) {
            builder.maxPages(maxPages);
        }

        // 处理请求头
        Object headersObj = config.get("headers");
        if (headersObj instanceof Map) {
            Map<String, String> headers = (Map<String, String>) headersObj;
            builder.headers(headers);
        }

        // 处理请求体
        String requestBody = getString(config, "requestBody", null);
        if (requestBody != null) {
            builder.requestBody(requestBody);
        }

        return builder.build();
    }

    @Override
    public String test(Map<String, Object> config) {
        String url = getString(config, "url", null);
        if (url == null || url.trim().isEmpty()) {
            return "URL不能为空";
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return "URL格式不正确，必须以 http:// 或 https:// 开头";
            }
        } catch (Exception e) {
            return "URL格式不正确: " + e.getMessage();
        }

        return null;
    }

    @Override
    public String validate(Map<String, Object> config) {
        String url = getString(config, "url", null);
        if (url == null || url.trim().isEmpty()) {
            return "URL不能为空";
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
