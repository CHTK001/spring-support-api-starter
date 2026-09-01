package com.chua.starter.geo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 瓦片代理控制器，提供 CORS 转发接口。
 * Web 版为 no-op，仅作占位。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/proxy")
@Tag(name = "瓦片代理", description = "CORS 转发")
@Slf4j
public class ProxyController {

    /**
     * 启动瓦片代理服务（Web 版为 no-op）。
     *
     * @param body 请求参数（baseUrl, headers, proxy）
     * @return 代理地址
     */
    @PostMapping("/start")
    @Operation(summary = "启动瓦片代理")
    public ResponseEntity<Map<String, Object>> startProxy(@RequestBody Map<String, Object> body) {
        log.info("[Proxy] 启动代理请求（Web版跳过）: {}", body);
        return ResponseEntity.ok(Map.of("proxyUrl", "", "status", "not_supported"));
    }
}
