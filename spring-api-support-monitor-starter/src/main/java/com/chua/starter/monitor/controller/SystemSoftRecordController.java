package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.SystemSoftRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 软件安装记录控制器
 */
@RestController
@RequestMapping("/v1/soft/record")
@Tag(name = "软件安装记录")
@RequiredArgsConstructor
public class SystemSoftRecordController {

    private final SystemSoftRecordService systemSoftRecordService;

    @Operation(summary = "安装")
    @PostMapping("install")
    public ReturnResult<Boolean> install(@RequestParam Integer systemSoftId,
                                         @RequestParam Integer systemSoftVersionId,
                                         @RequestParam Integer[] serverIds) {
        return systemSoftRecordService.install(systemSoftId, systemSoftVersionId, serverIds);
    }

    @Operation(summary = "卸载")
    @PostMapping("uninstall")
    public ReturnResult<Boolean> uninstall(@RequestParam Integer systemSoftId,
                                           @RequestParam Integer systemSoftVersionId,
                                           @RequestParam Integer[] serverIds) {
        return systemSoftRecordService.uninstall(systemSoftId, systemSoftVersionId, serverIds);
    }
}

