package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.entity.MonitorSysGenBackup;
import com.chua.starter.monitor.service.MonitorSysGenBackupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据源备份控制器
 */
@RestController
@RequestMapping("v1/gen/backup")
@Tag(name = "数据源备份管理")
@Api(tags = "数据源备份管理")
@RequiredArgsConstructor
public class MonitorSysGenBackupController {
    private final MonitorSysGenBackupService backupService;

    /**
     * 根据ID获取备份配置
     */
    @GetMapping("/{monitorSysGenId}")
    @ApiOperation(value = "根据ID获取备份配置")
    public ReturnResult<MonitorSysGenBackup> findBackupById(@PathVariable Integer monitorSysGenId) {
        return ReturnResult.success(backupService.findBackupById(monitorSysGenId));
    }

    /**
     * 根据ID获取备份配置
     */
    @GetMapping("/run/{id}")
    @ApiOperation(value = "根据ID手动备份")
    public ReturnResult<Boolean> run(@PathVariable Integer id) {
        return backupService.backup(id);
    }

    /**
     * 创建新的备份配置
     */
    @PostMapping("/save")
    @ApiOperation(value = "创建新的备份配置")
    public ReturnResult<Boolean> createBackup(@Validated(AddGroup.class) @RequestBody MonitorSysGenBackup backup, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return backupService.saveFor(backup);
    }

    /**
     * 更新备份配置
     */
    @PutMapping("update")
    @ApiOperation(value = "更新备份配置")
    public ReturnResult<Boolean> updateBackup(@Validated(UpdateGroup.class) @RequestBody MonitorSysGenBackup backup, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return backupService.updateFor(backup);
    }

    /**
     * 删除备份配置
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除备份配置")
    public ReturnResult<Boolean> deleteBackup(@PathVariable Integer id) {
        return backupService.removeFor(id);
    }
}