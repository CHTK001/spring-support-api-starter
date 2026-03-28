package com.chua.starter.oshi.performance;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.api.annotations.ApiPlatform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 系统性能监控接口。
 *
 * @author CH
 * @since 2026/3/28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/system-performance")
@Tag(name = "系统性能监控", description = "系统设置页性能监控数据接口")
public class SystemPerformanceController {

    private final SystemPerformanceService systemPerformanceService;

    @Operation(summary = "获取性能监控配置")
    @GetMapping("/config")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SystemPerformanceConfig> getConfig() {
        return ReturnResult.ok(systemPerformanceService.getConfig());
    }

    @Operation(summary = "获取系统性能快照")
    @GetMapping("/snapshot")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SystemPerformanceSnapshot> getSnapshot(
            @RequestParam(defaultValue = "false") boolean broadcast) {
        return ReturnResult.ok(systemPerformanceService.getSnapshot(broadcast));
    }

    @Operation(summary = "强制广播系统性能快照")
    @PostMapping("/broadcast")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SystemPerformanceSnapshot> broadcastSnapshot() {
        return ReturnResult.ok(systemPerformanceService.broadcastSnapshot());
    }
}
