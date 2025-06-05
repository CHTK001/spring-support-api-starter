package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.monitor.entity.MonitorSysGenBackupLog;
import com.chua.starter.monitor.service.MonitorSysGenBackupLogService;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 备份日志控制器
 */
@RestController
@RequestMapping("/backup/log")
@Tag(name = "备份日志管理")
@Api(tags = "备份日志管理")
@RequiredArgsConstructor
public class MonitorSysGenBackupLogController {
    private final MonitorSysGenBackupLogService logService;

    /**
     * 获取所有备份日志
     */
    @GetMapping("/{backupId}")
    @ApiOperation(value = "获取所有备份日志")
    public ReturnPageResult<MonitorSysGenBackupLog> page(
            Query<MonitorSysGenBackupLog> query,
            @PathVariable Integer backupId

    ) {
        return logService.pageFor(query, backupId);
    }

}