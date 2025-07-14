package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.FileSystemSetting;
import com.chua.starter.monitor.starter.service.FileSystemSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 文件系统配置管理控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "文件系统配置管理")
@Tag(name = "文件系统配置管理", description = "文件系统配置管理相关接口")
@Slf4j
@RestController
@RequestMapping("/v1/filesystem-setting")
@RequiredArgsConstructor
public class FileSystemSettingController {

    private final FileSystemSettingService fileSystemSettingService;

    /**
     * 获取所有配置列表
     */
    @ApiOperation("获取所有配置列表")
    @Operation(summary = "获取所有配置列表", description = "获取系统中所有的文件系统配置")
    @GetMapping("/list")
    public ReturnResult<List<FileSystemSetting>> getAllSettings() {

        log.info("获取所有文件系统配置列表");

        try {
            List<FileSystemSetting> settings = fileSystemSettingService.list();
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的配置
     */
    @ApiOperation("获取启用的配置")
    @Operation(summary = "获取启用的配置", description = "获取当前启用的文件系统配置")
    @GetMapping("/enabled")
    public ReturnResult<FileSystemSetting> getEnabledSetting() {

        log.info("获取启用的文件系统配置");

        try {
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("获取启用配置失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取配置详情
     */
    @ApiOperation("根据ID获取配置详情")
    @Operation(summary = "根据ID获取配置详情", description = "获取指定ID的配置详细信息")
    @GetMapping("/{settingId}")
    public ReturnResult<FileSystemSetting> getSettingById(
            @ApiParam("配置ID") @Parameter(description = "配置ID") @PathVariable Integer settingId) {

        log.info("获取文件系统配置详情: settingId={}", settingId);

        try {
            FileSystemSetting setting = fileSystemSettingService.getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("获取配置详情失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建新配置
     */
    @ApiOperation("创建新配置")
    @Operation(summary = "创建新配置", description = "创建新的文件系统配置")
    @PostMapping
    public ReturnResult<Boolean> createSetting(@RequestBody @Valid FileSystemSetting setting,
            BindingResult bindingResult) {

        log.info("创建文件系统配置: setting={}", setting);

        if (bindingResult.hasErrors()) {
            log.warn("创建配置参数验证失败: {}", bindingResult.getAllErrors());
            return ReturnResult.error("参数验证失败");
        }

        try {
            // 检查配置名称是否已存在
            FileSystemSetting existingSetting = fileSystemSettingService
                    .findBySettingName(setting.getFileSystemSettingName());
            if (existingSetting != null) {
                return ReturnResult.error("配置名称已存在");
            }

            // 设置默认值
            if (setting.getFileSystemSettingEnabled() == null) {
                setting.setFileSystemSettingEnabled(false);
            }

            boolean result = fileSystemSettingService.save(setting);
            if (result) {
                log.info("文件系统配置创建成功: settingId={}", setting.getFileSystemSettingId());
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("创建配置失败");
            }
        } catch (Exception e) {
            log.error("创建文件系统配置失败", e);
            return ReturnResult.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置
     */
    @ApiOperation("更新配置")
    @Operation(summary = "更新配置", description = "更新指定的文件系统配置")
    @PutMapping("/{settingId}")
    public ReturnResult<Boolean> updateSetting(
            @ApiParam("配置ID") @Parameter(description = "配置ID") @PathVariable Integer settingId,
            @RequestBody @Valid FileSystemSetting setting, BindingResult bindingResult) {

        log.info("更新文件系统配置: settingId={}, setting={}", settingId, setting);

        if (bindingResult.hasErrors()) {
            log.warn("更新配置参数验证失败: {}", bindingResult.getAllErrors());
            return ReturnResult.error("参数验证失败");
        }

        try {
            // 检查配置是否存在
            FileSystemSetting existingSetting = fileSystemSettingService.getById(settingId);
            if (existingSetting == null) {
                return ReturnResult.error("配置不存在");
            }

            // 检查配置名称是否与其他配置冲突
            if (!existingSetting.getFileSystemSettingName().equals(setting.getFileSystemSettingName())) {
                FileSystemSetting conflictSetting = fileSystemSettingService
                        .findBySettingName(setting.getFileSystemSettingName());
                if (conflictSetting != null && !conflictSetting.getFileSystemSettingId().equals(settingId)) {
                    return ReturnResult.error("配置名称已存在");
                }
            }

            setting.setFileSystemSettingId(settingId);
            return fileSystemSettingService.updateSettingWithCache(setting);
        } catch (Exception e) {
            log.error("更新文件系统配置失败", e);
            return ReturnResult.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除配置
     */
    @ApiOperation("删除配置")
    @Operation(summary = "删除配置", description = "删除指定的文件系统配置")
    @DeleteMapping("/{settingId}")
    public ReturnResult<Boolean> deleteSetting(
            @ApiParam("配置ID") @Parameter(description = "配置ID") @PathVariable Integer settingId) {

        log.info("删除文件系统配置: settingId={}", settingId);

        try {
            // 检查配置是否存在
            FileSystemSetting setting = fileSystemSettingService.getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            // 检查是否为启用的配置
            if (Boolean.TRUE.equals(setting.getFileSystemSettingEnabled())) {
                return ReturnResult.error("不能删除启用中的配置");
            }

            boolean result = fileSystemSettingService.removeById(settingId);
            if (result) {
                log.info("文件系统配置删除成功: settingId={}", settingId);
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("删除配置失败");
            }
        } catch (Exception e) {
            log.error("删除文件系统配置失败", e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 启用配置
     */
    @ApiOperation("启用配置")
    @Operation(summary = "启用配置", description = "启用指定的文件系统配置（会禁用其他配置）")
    @PostMapping("/enable/{settingId}")
    public ReturnResult<Boolean> enableSetting(
            @ApiParam("配置ID") @Parameter(description = "配置ID") @PathVariable Integer settingId) {

        log.info("启用文件系统配置: settingId={}", settingId);

        try {
            return fileSystemSettingService.enableSetting(settingId);
        } catch (Exception e) {
            log.error("启用文件系统配置失败", e);
            return ReturnResult.error("启用失败: " + e.getMessage());
        }
    }

    /**
     * 初始化默认配置
     */
    @ApiOperation("初始化默认配置")
    @Operation(summary = "初始化默认配置", description = "创建系统默认的文件系统配置")
    @PostMapping("/init-default")
    public ReturnResult<Boolean> initDefaultSetting() {

        log.info("初始化默认文件系统配置");

        try {
            return fileSystemSettingService.initDefaultSetting();
        } catch (Exception e) {
            log.error("初始化默认配置失败", e);
            return ReturnResult.error("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 清除配置缓存
     */
    @ApiOperation("清除配置缓存")
    @Operation(summary = "清除配置缓存", description = "清除文件系统配置的缓存数据")
    @PostMapping("/clear-cache")
    public ReturnResult<Boolean> clearCache() {

        log.info("清除文件系统配置缓存");

        try {
            fileSystemSettingService.clearCache();
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("清除配置缓存失败", e);
            return ReturnResult.error("清除失败: " + e.getMessage());
        }
    }
}
