package com.chua.starter.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 基准测试基类 — 提供共享的 HttpClient 和服务器 URL 配置。
 *
 * <p>Rust 服务器默认端口 18081, Tomcat 默认端口 18082。</p>
 */
public abstract class AbstractBenchmarkBase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static final String RUST_BASE = "http://localhost:18081";
    public static final String TOMCAT_BASE = "http://localhost:18082";

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    protected HttpResponse<String> get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> post(String url, String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> request(String url, String method, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10));
        switch (method.toUpperCase()) {
            case "GET" -> builder.GET();
            case "DELETE" -> builder.DELETE();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            default -> builder.method(method.toUpperCase(),
                    HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        }
        if (body != null && !body.isEmpty()) {
            builder.header("Content-Type", "application/json");
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    protected String rustUrl(String path) {
        return RUST_BASE + path;
    }

    protected String tomcatUrl(String path) {
        return TOMCAT_BASE + path;
    }
}
