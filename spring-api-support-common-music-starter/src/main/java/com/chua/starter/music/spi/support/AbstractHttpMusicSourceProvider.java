package com.chua.starter.music.spi.support;

import com.chua.common.support.network.http.HttpClient;
import com.chua.common.support.network.http.HttpClientBuilder;
import com.chua.common.support.network.http.HttpResponse;
import com.chua.common.support.network.protocol.utils.JsonUtils;
import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistSummary;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicTrackSummary;
import com.chua.starter.music.spi.MusicSourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractHttpMusicSourceProvider implements MusicSourceProvider {

    protected static final ObjectMapper MAPPER = JsonUtils.getSharedMapper();
    protected static final String DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    protected static final String MOBILE_UA =
            "Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36";
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 20_000;
    private static final String CONNECT_TIMEOUT_PROPERTY = "music.http.connect-timeout-millis";
    private static final String CONNECT_TIMEOUT_ENV = "MUSIC_HTTP_CONNECT_TIMEOUT_MILLIS";
    private static final String READ_TIMEOUT_PROPERTY = "music.http.read-timeout-millis";
    private static final String READ_TIMEOUT_ENV = "MUSIC_HTTP_READ_TIMEOUT_MILLIS";

    protected JsonNode getJson(String url) {
        return getJson(url, null);
    }

    protected JsonNode getJson(String url, Consumer<HttpClientBuilder> customizer) {
        HttpClientBuilder builder = HttpClient.get(url)
                .userAgent(DESKTOP_UA)
                .connectTimeout(connectTimeoutMillis())
                .readTimeout(readTimeoutMillis())
                .retry(0);
        if (customizer != null) {
            customizer.accept(builder);
        }
        return readTree(builder.execute(), url);
    }

    protected String getText(String url) {
        return getText(url, null);
    }

    protected String getText(String url, Consumer<HttpClientBuilder> customizer) {
        HttpClientBuilder builder = HttpClient.get(url)
                .userAgent(DESKTOP_UA)
                .connectTimeout(connectTimeoutMillis())
                .readTimeout(readTimeoutMillis())
                .retry(0);
        if (customizer != null) {
            customizer.accept(builder);
        }
        HttpResponse response = builder.execute();
        assertOk(response, url);
        return response.asStringBody();
    }

    protected JsonNode postJson(String url, Object body, Consumer<HttpClientBuilder> customizer) {
        HttpClientBuilder builder = HttpClient.post(url)
                .json()
                .userAgent(DESKTOP_UA)
                .body(JsonUtils.toJson(body))
                .connectTimeout(connectTimeoutMillis())
                .readTimeout(readTimeoutMillis())
                .retry(0);
        if (customizer != null) {
            customizer.accept(builder);
        }
        return readTree(builder.execute(), url);
    }

    protected JsonNode postForm(String url, Map<String, ?> form, Consumer<HttpClientBuilder> customizer) {
        HttpClientBuilder builder = HttpClient.post(url)
                .userAgent(DESKTOP_UA)
                .form(form)
                .connectTimeout(connectTimeoutMillis())
                .readTimeout(readTimeoutMillis())
                .retry(0);
        if (customizer != null) {
            customizer.accept(builder);
        }
        return readTree(builder.execute(), url);
    }

    protected HttpResponse getResponse(String url, Consumer<HttpClientBuilder> customizer) {
        HttpClientBuilder builder = HttpClient.get(url)
                .userAgent(DESKTOP_UA)
                .connectTimeout(connectTimeoutMillis())
                .readTimeout(readTimeoutMillis())
                .retry(0);
        if (customizer != null) {
            customizer.accept(builder);
        }
        HttpResponse response = builder.execute();
        assertOk(response, url);
        return response;
    }

    protected MusicOverview overviewOf(List<String> hotKeywords, List<MusicPlaylistSummary> featuredPlaylists) {
        return MusicOverview.builder()
                .hotKeywords(hotKeywords == null ? List.of() : hotKeywords)
                .featuredPlaylists(featuredPlaylists == null ? List.of() : featuredPlaylists)
                .build();
    }

    protected MusicSearchResult searchResult(String keyword, int page, int pageSize, long total, List<MusicTrackSummary> tracks) {
        return MusicSearchResult.builder()
                .source(getSource().getCode())
                .keyword(keyword)
                .page(page)
                .pageSize(pageSize)
                .total(total)
                .tracks(tracks == null ? List.of() : tracks)
                .build();
    }

    protected String text(JsonNode node, String... fields) {
        JsonNode current = path(node, fields);
        return current == null || current.isMissingNode() || current.isNull() ? "" : current.asText("");
    }

    protected int integer(JsonNode node, String... fields) {
        JsonNode current = path(node, fields);
        return current == null || current.isMissingNode() || current.isNull() ? 0 : current.asInt(0);
    }

    protected long longValue(JsonNode node, String... fields) {
        JsonNode current = path(node, fields);
        return current == null || current.isMissingNode() || current.isNull() ? 0L : current.asLong(0L);
    }

    protected JsonNode path(JsonNode node, String... fields) {
        JsonNode current = node;
        for (String field : fields) {
            if (current == null) {
                return null;
            }
            current = current.path(field);
        }
        return current;
    }

    protected List<JsonNode> elements(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<JsonNode> result = new ArrayList<>();
        node.forEach(result::add);
        return result;
    }

    protected String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    protected String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    protected JsonNode readTree(HttpResponse response, String url) {
        assertOk(response, url);
        try {
            return MAPPER.readTree(response.asStringBody());
        } catch (Exception e) {
            throw new IllegalStateException("解析音乐接口响应失败: " + url, e);
        }
    }

    protected void assertOk(HttpResponse response, String url) {
        if (response == null || !response.isOk()) {
            throw new IllegalStateException("音乐接口请求失败: " + url);
        }
    }

    protected int connectTimeoutMillis() {
        return resolveTimeoutMillis(CONNECT_TIMEOUT_PROPERTY, CONNECT_TIMEOUT_ENV, DEFAULT_CONNECT_TIMEOUT_MILLIS);
    }

    protected int readTimeoutMillis() {
        return resolveTimeoutMillis(READ_TIMEOUT_PROPERTY, READ_TIMEOUT_ENV, DEFAULT_READ_TIMEOUT_MILLIS);
    }

    private int resolveTimeoutMillis(String propertyName, String envName, int defaultValue) {
        String configured = System.getProperty(propertyName);
        if (!StringUtils.hasText(configured)) {
            configured = System.getenv(envName);
        }
        if (!StringUtils.hasText(configured)) {
            return defaultValue;
        }
        try {
            int timeoutMillis = Integer.parseInt(configured.trim());
            return timeoutMillis > 0 ? timeoutMillis : defaultValue;
        } catch (NumberFormatException ex) {
            log.warn("Ignore invalid music timeout configuration, property={}, env={}, value={}",
                    propertyName, envName, configured);
            return defaultValue;
        }
    }
}
