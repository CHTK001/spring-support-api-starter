package com.chua.starter.monitor.controller.script;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord;
import com.chua.starter.monitor.service.script.MonitorSysGenScriptUploadRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 脚本上传记录 控制器
 * <p>
 * 职责：提供上传记录分页查询等接口
 *
 * @author CH
 */
@Slf4j
@RestController
@RequestMapping("v1/script/upload-record")
@RequiredArgsConstructor
@Tag(name = "脚本上传记录", description = "脚本上传记录相关接口")
public class MonitorSysGenScriptUploadRecordController {

    private final MonitorSysGenScriptUploadRecordService uploadRecordService;

    /**
     * 分页查询脚本上传记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询脚本上传记录", description = "按脚本ID可选过滤，按记录ID倒序")
    public ReturnResult<IPage<MonitorSysGenScriptUploadRecord>> getUploadRecords(
            @Parameter(description = "脚本ID，可选") @RequestParam(required = false) Integer scriptId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<MonitorSysGenScriptUploadRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<MonitorSysGenScriptUploadRecord> qw = new LambdaQueryWrapper<>();
            if (scriptId != null) {
                qw.eq(MonitorSysGenScriptUploadRecord::getMonitorSysGenScriptId, scriptId);
            }
            qw.orderByDesc(MonitorSysGenScriptUploadRecord::getMonitorSysGenScriptUploadRecordId);

            IPage<MonitorSysGenScriptUploadRecord> data = uploadRecordService.page(page, qw);
            return ReturnResult.ok(data);
        } catch (Exception e) {
            log.error("查询上传记录失败", e);
            return ReturnResult.error("查询上传记录失败: " + e.getMessage());
        }
    }
}

