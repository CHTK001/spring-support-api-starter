package com.chua.starter.protocol.test.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class ProtocolTestController {

    @Value("${server.port:19181}")
    private int serverPort;

    @Value("${protocol.test.proxy.port:19190}")
    private int proxyPort;

    @GetMapping("/upstream/echo")
    public Map<String, Object> upstreamEcho(@RequestParam(defaultValue = "world") String name) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ok", true);
        data.put("name", name);
        data.put("message", "hello-" + name);
        data.put("timestamp", Instant.now().toString());
        return data;
    }

    @GetMapping("/proxy/check")
    public Map<String, Object> proxyCheck() throws Exception {
        String target = "http://127.0.0.1:" + serverPort + "/test/upstream/echo?name=dynamic-proxy";
        HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", proxyPort)))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(target))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ok", response.statusCode() >= 200 && response.statusCode() < 300);
        data.put("status", response.statusCode());
        data.put("proxyPort", proxyPort);
        data.put("target", target);
        data.put("body", response.body());
        return data;
    }
}
