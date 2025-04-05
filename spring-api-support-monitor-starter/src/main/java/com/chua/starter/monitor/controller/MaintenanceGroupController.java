package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceGroupRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceGroupResponse;
import com.chua.starter.monitor.service.MaintenanceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 维护组管理控制器
 * 提供维护组的增删改查等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/api/maintenance/group")
@Tag(name = "维护组管理")
public class MaintenanceGroupController {

    /**
     * 维护组服务
     */
    @Resource
    private MaintenanceGroupService maintenanceGroupService;

    /**
     * 创建维护组
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建维护组")
    public ReturnResult<MaintenanceGroupResponse> createMaintenanceGroup(@RequestBody MaintenanceGroupRequest request) {
        return maintenanceGroupService.createMaintenanceGroup(request);
    }

    /**
     * 更新维护组
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新维护组")
    public ReturnResult<MaintenanceGroupResponse> updateMaintenanceGroup(@RequestBody MaintenanceGroupRequest request) {
        return maintenanceGroupService.updateMaintenanceGroup(request);
    }

    /**
     * 删除维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护组")
    public ReturnResult<Boolean> deleteMaintenanceGroup(@PathVariable("id") Long maintenanceGroupId) {
        return maintenanceGroupService.deleteMaintenanceGroup(maintenanceGroupId);
    }

    /**
     * 获取维护组详情
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护组详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护组详情")
    public ReturnResult<MaintenanceGroupResponse> getMaintenanceGroup(@PathVariable("id") Long maintenanceGroupId) {
        return maintenanceGroupService.getMaintenanceGroup(maintenanceGroupId);
    }

    /**
     * 获取维护组列表
     *
     * @return 维护组列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护组列表")
    public ReturnResult<List<MaintenanceGroupResponse>> listMaintenanceGroups() {
        return maintenanceGroupService.listMaintenanceGroups();
    }

    /**
     * 启用或停用维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @param enabled            是否启用
     * @return 操作结果
     */
    @PutMapping("/{id}/enable/{enabled}")
    @Operation(summary = "启用或停用维护组")
    public ReturnResult<Boolean> enableMaintenanceGroup(
            @PathVariable("id") Long maintenanceGroupId,
            @PathVariable("enabled") Boolean enabled) {
        return maintenanceGroupService.enableMaintenanceGroup(maintenanceGroupId, enabled);
    }
} 