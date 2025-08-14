package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.dto.SystemSoftInstallRequest;
import com.chua.starter.monitor.service.SystemSoftRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 软件安装记录控制器
 *
 * 提供安装/卸载接口，支持 JSON 传参配置安装方式与参数。
 *
 * 作者: CH 时间: 2025/08/14
 */
@RestController
@RequestMapping("/v1/soft/record")
@Tag(name = "软件安装记录")
@RequiredArgsConstructor
public class SystemSoftRecordController {

    private final SystemSoftRecordService systemSoftRecordService;

    /**
     * 安装软件
     *
     * 入参(JSON): SystemSoftInstallRequest 示例: { "systemSoftId": 1,
     * "systemSoftVersionId": 10, "serverIds": [1,2,3], "method": "DOCKER_CLI",
     * "params": "{\"env\":[\"TZ=Asia/Shanghai\"]}" }
     */
    @Operation(summary = "安装")
    @PostMapping("install")
    public ReturnResult<Boolean> install(@RequestBody SystemSoftInstallRequest request) {
        return systemSoftRecordService.install(request.getSystemSoftId(), request.getSystemSoftVersionId(),
                request.getServerIds(), request.getMethod(), request.getParams());
    }

    /**
     * 卸载软件
     *
     * 入参: systemSoftId, systemSoftVersionId, serverIds 示例: POST
     * /v1/soft/record/uninstall?systemSoftId=1&systemSoftVersionId=10&serverIds=1&serverIds=2
     */
    @Operation(summary = "卸载")
    @PostMapping("uninstall")
    public ReturnResult<Boolean> uninstall(@RequestParam Integer systemSoftId,
            @RequestParam Integer systemSoftVersionId, @RequestParam Integer[] serverIds) {
        return systemSoftRecordService.uninstall(systemSoftId, systemSoftVersionId, serverIds);
    }
}
