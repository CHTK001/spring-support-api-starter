package com.chua.starter.geo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 本地 3D Tiles 静态文件服务控制器。
 * Web 版为 no-op，仅作占位。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/tiles")
@Tag(name = "本地瓦片服务", description = "本地 3D Tiles 静态文件服务")
@Slf4j
public class TilesServeController {

    /**
     * 启动本地瓦片服务（Web 版为 no-op）。
     *
     * @param body 请求参数（dirPath）
     * @return 服务地址
     */
    @PostMapping("/serve")
    @Operation(summary = "启动本地瓦片服务")
    public ResponseEntity<Map<String, Object>> serve(@RequestBody Map<String, Object> body) {
        log.info("[Tiles] 本地服务请求（Web版跳过）: {}", body);
        return ResponseEntity.ok(Map.of("baseUrl", "", "status", "not_supported"));
    }
}
