package com.chua.starter.geo.controller;

import com.chua.starter.geo.model.WaybackVersion;
import com.chua.starter.geo.service.WaybackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Esri Wayback 历史影像控制器，提供历史版本查询接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/wayback")
@Tag(name = "Esri Wayback历史影像", description = "Esri World Imagery 历史版本浏览")
@RequiredArgsConstructor
@Slf4j
public class WaybackController {

    /** Wayback 服务 */
    private final WaybackService waybackService;

    /**
     * 获取历史影像版本列表。
     *
     * @return 版本列表及总数
     */
    @GetMapping("/versions")
    @Operation(summary = "获取历史影像版本列表")
    public Map<String, Object> getVersions() {
        List<WaybackVersion> versions = waybackService.getVersions();
        return Map.of("versions", versions, "count", versions.size());
    }
}
