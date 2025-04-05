package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceScriptRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceScriptResponse;
import com.chua.starter.monitor.service.MaintenanceScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 维护脚本管理控制器
 * 提供维护脚本的增删改查、同步等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/maintenance/script")
@Tag(name = "维护脚本管理", description = "维护脚本的创建、修改、删除、查询、同步等操作，支持脚本手动同步到维护组内所有主机")
@RequiredArgsConstructor
public class MaintenanceScriptController {

    /**
     * 维护脚本服务
     */
    private final MaintenanceScriptService maintenanceScriptService;

    /**
     * 创建维护脚本
     * 脚本仅创建记录，不自动上传到主机
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping("save")
    @Operation(summary = "创建维护脚本", description = "创建一个新的维护脚本，返回创建后的脚本信息。脚本仅在数据库中创建记录，不会自动上传到主机。")
    public ReturnResult<MaintenanceScriptResponse> createMaintenanceScript(
            @Parameter(description = "维护脚本信息", required = true) 
            @RequestBody MaintenanceScriptRequest request) {
        if (request.getMaintenanceGroupId() == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceScriptService.createMaintenanceScript(request);
    }

    /**
     * 更新维护脚本
     * 脚本仅更新记录，不自动上传到主机
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新维护脚本", description = "根据ID更新维护脚本信息，返回更新后的脚本信息。脚本仅在数据库中更新记录，不会自动更新主机上的脚本。")
    public ReturnResult<MaintenanceScriptResponse> updateMaintenanceScript(
            @Parameter(description = "维护脚本信息", required = true) 
            @RequestBody MaintenanceScriptRequest request) {
        return maintenanceScriptService.updateMaintenanceScript(request);
    }

    /**
     * 删除维护脚本
     * 脚本删除后会自动删除维护组下所有启用的主机相同位置的脚本
     *
     * @param maintenanceScriptId 脚本ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护脚本", description = "根据ID删除维护脚本。删除后会自动删除维护组下所有启用的主机相同位置的脚本。")
    public ReturnResult<Boolean> deleteMaintenanceScript(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.deleteMaintenanceScript(maintenanceScriptId);
    }

    /**
     * 获取维护脚本详情
     *
     * @param maintenanceScriptId 脚本ID
     * @return 脚本详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护脚本详情", description = "根据ID获取维护脚本详细信息")
    public ReturnResult<MaintenanceScriptResponse> getMaintenanceScript(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.getMaintenanceScript(maintenanceScriptId);
    }

    /**
     * 获取维护脚本列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 脚本列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护脚本列表", description = "根据维护组ID获取该组下的所有维护脚本")
    public ReturnResult<List<MaintenanceScriptResponse>> listMaintenanceScripts(
            @Parameter(description = "维护组ID", required = true)
            @RequestParam(value = "groupId") Long maintenanceGroupId) {
        if (maintenanceGroupId == null) {
            return ReturnResult.ok(Collections.emptyList());
        }
        return maintenanceScriptService.listMaintenanceScripts(maintenanceGroupId);
    }

    /**
     * 同步脚本到远程主机
     *
     * @param maintenanceScriptId 脚本ID
     * @return 同步结果
     */
    @PostMapping("/{id}/sync")
    @Operation(summary = "同步脚本到远程主机", description = "将指定脚本同步到维护组下所有启用的主机，返回同步操作的结果。")
    public ReturnResult<Boolean> syncScript(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.syncScript(maintenanceScriptId);
    }

    /**
     * 更新脚本状态
     *
     * @param maintenanceScriptId 脚本ID
     * @param status              状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新脚本状态", description = "修改维护脚本的状态（启用/禁用等）")
    public ReturnResult<Boolean> updateScriptStatus(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId,
            @Parameter(description = "状态值: 0-禁用, 1-启用", required = true) 
            @PathVariable("status") Integer status) {
        return maintenanceScriptService.updateScriptStatus(maintenanceScriptId, status);
    }
} 