package com.chua.starter.geo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 应用更新控制器，提供版本检查与安装接口。
 * Web 版仅返回当前版本号，不提供自动更新功能。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/update")
@Tag(name = "应用更新", description = "版本检查与更新安装")
@Slf4j
public class UpdateController {

    private static final String CURRENT_VERSION = "4.0.0";

    /**
     * 获取最新版本信息（Web 版返回当前版本作为无更新）。
     *
     * @param prerelease 是否包含预发布版本
     * @return 版本信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取更新信息")
    public ResponseEntity<Map<String, Object>> getInfo(@RequestParam(defaultValue = "false") boolean prerelease) {
        Map<String, Object> info = Map.of(
                "currentVersion", CURRENT_VERSION,
                "latestVersion", CURRENT_VERSION,
                "hasUpdate", false,
                "downloadUrl", "",
                "releaseNotes", ""
        );
        return ResponseEntity.ok(info);
    }

    /**
     * 下载安装更新（Web 版不支持）。
     *
     * @param body 请求参数（url, version）
     * @return 操作结果
     */
    @PostMapping("/install")
    @Operation(summary = "下载安装更新")
    public ResponseEntity<Map<String, String>> install(@RequestBody Map<String, Object> body) {
        log.info("[Update] 安装更新请求（Web版不支持）: {}", body);
        return ResponseEntity.ok(Map.of("message", "Update install not supported in web mode"));
    }
}
