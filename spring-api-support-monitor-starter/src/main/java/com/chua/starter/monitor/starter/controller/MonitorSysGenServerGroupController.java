package com.chua.starter.monitor.starter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerGroup;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务器分组管理控制器
 *
 * @author CH
 * @since 2025/01/11
 */
@Slf4j
@RestController
@RequestMapping("/v1/gen/server/group")
@RequiredArgsConstructor
@Api(tags = "服务器分组管理")
@Tag(name = "服务器分组管理", description = "服务器分组的增删改查操作")
public class MonitorSysGenServerGroupController {

    private final MonitorSysGenServerGroupService serverGroupService;

    /**
     * 分页查询服务器分组
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询服务器分组")
    @Operation(summary = "分页查询服务器分组", description = "根据条件分页查询服务器分组列表")
    public ReturnResult<IPage<MonitorSysGenServerGroup>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "查询条件") MonitorSysGenServerGroup entity) {
        try {
            Page<MonitorSysGenServerGroup> page = new Page<>(current, size);
            IPage<MonitorSysGenServerGroup> result = serverGroupService.pageFor(page, entity);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("分页查询服务器分组失败", e);
            return ReturnResult.error("分页查询服务器分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的分组列表
     */
    @GetMapping("/enabled")
    @ApiOperation(value = "获取启用的分组列表")
    @Operation(summary = "获取启用的分组列表", description = "获取所有启用状态的服务器分组")
    public ReturnResult<List<MonitorSysGenServerGroup>> getEnabledGroups() {
        return serverGroupService.getEnabledGroups();
    }

    /**
     * 获取所有分组列表
     */
    @GetMapping("/all")
    @ApiOperation(value = "获取所有分组列表")
    @Operation(summary = "获取所有分组列表", description = "获取所有服务器分组")
    public ReturnResult<List<MonitorSysGenServerGroup>> getAllGroups() {
        return serverGroupService.getAllGroups();
    }

    /**
     * 根据ID获取分组信息
     */
    @GetMapping("/{groupId}")
    @ApiOperation(value = "根据ID获取分组信息")
    @Operation(summary = "根据ID获取分组信息", description = "根据分组ID获取详细信息")
    public ReturnResult<MonitorSysGenServerGroup> getGroupById(
            @Parameter(description = "分组ID") @PathVariable Integer groupId) {
        return serverGroupService.getGroupById(groupId);
    }

    /**
     * 创建分组
     */
    @PostMapping
    @ApiOperation(value = "创建分组")
    @Operation(summary = "创建分组", description = "创建新的服务器分组")
    public ReturnResult<Boolean> createGroup(
            @Parameter(description = "分组信息") @RequestBody @Validated MonitorSysGenServerGroup group) {
        return serverGroupService.createGroup(group);
    }

    /**
     * 更新分组
     */
    @PutMapping
    @ApiOperation(value = "更新分组")
    @Operation(summary = "更新分组", description = "更新服务器分组信息")
    public ReturnResult<Boolean> updateGroup(
            @Parameter(description = "分组信息") @RequestBody @Validated MonitorSysGenServerGroup group) {
        return serverGroupService.updateGroup(group);
    }

    /**
     * 删除分组
     */
    @DeleteMapping("/{groupId}")
    @ApiOperation(value = "删除分组")
    @Operation(summary = "删除分组", description = "根据ID删除服务器分组")
    public ReturnResult<Boolean> deleteGroup(
            @Parameter(description = "分组ID") @PathVariable Integer groupId) {
        return serverGroupService.deleteGroup(groupId);
    }

    /**
     * 批量删除分组
     */
    @DeleteMapping("/batch")
    @ApiOperation(value = "批量删除分组")
    @Operation(summary = "批量删除分组", description = "批量删除服务器分组")
    public ReturnResult<Boolean> batchDeleteGroups(
            @Parameter(description = "分组ID列表") @RequestBody List<Integer> groupIds) {
        return serverGroupService.batchDeleteGroups(groupIds);
    }

    /**
     * 更新分组状态
     */
    @PutMapping("/{groupId}/status")
    @ApiOperation(value = "更新分组状态")
    @Operation(summary = "更新分组状态", description = "启用或禁用服务器分组")
    public ReturnResult<Boolean> updateGroupStatus(
            @Parameter(description = "分组ID") @PathVariable Integer groupId,
            @Parameter(description = "状态") @RequestParam Integer status) {
        return serverGroupService.updateGroupStatus(groupId, status);
    }

    /**
     * 更新分组排序
     */
    @PutMapping("/{groupId}/sort")
    @ApiOperation(value = "更新分组排序")
    @Operation(summary = "更新分组排序", description = "更新服务器分组排序号")
    public ReturnResult<Boolean> updateGroupSort(
            @Parameter(description = "分组ID") @PathVariable Integer groupId,
            @Parameter(description = "排序号") @RequestParam Integer sort) {
        return serverGroupService.updateGroupSort(groupId, sort);
    }

    /**
     * 批量更新分组排序
     */
    @PutMapping("/sort/batch")
    @ApiOperation(value = "批量更新分组排序")
    @Operation(summary = "批量更新分组排序", description = "批量更新服务器分组排序")
    public ReturnResult<Boolean> batchUpdateGroupSort(
            @Parameter(description = "排序数据") @RequestBody List<MonitorSysGenServerGroup> sortData) {
        return serverGroupService.batchUpdateGroupSort(sortData);
    }

    /**
     * 检查分组名称是否存在
     */
    @GetMapping("/check-name")
    @ApiOperation(value = "检查分组名称是否存在")
    @Operation(summary = "检查分组名称是否存在", description = "检查分组名称是否已被使用")
    public ReturnResult<Boolean> checkGroupNameExists(
            @Parameter(description = "分组名称") @RequestParam String groupName,
            @Parameter(description = "排除的分组ID") @RequestParam(required = false) Integer excludeId) {
        return serverGroupService.checkGroupNameExists(groupName, excludeId);
    }

    /**
     * 获取默认分组
     */
    @GetMapping("/default")
    @ApiOperation(value = "获取默认分组")
    @Operation(summary = "获取默认分组", description = "获取当前设置的默认分组")
    public ReturnResult<MonitorSysGenServerGroup> getDefaultGroup() {
        return serverGroupService.getDefaultGroup();
    }

    /**
     * 设置默认分组
     */
    @PutMapping("/{groupId}/default")
    @ApiOperation(value = "设置默认分组")
    @Operation(summary = "设置默认分组", description = "设置指定分组为默认分组")
    public ReturnResult<Boolean> setDefaultGroup(
            @Parameter(description = "分组ID") @PathVariable Integer groupId) {
        return serverGroupService.setDefaultGroup(groupId);
    }

    /**
     * 获取分组下的服务器数量
     */
    @GetMapping("/{groupId}/server-count")
    @ApiOperation(value = "获取分组下的服务器数量")
    @Operation(summary = "获取分组下的服务器数量", description = "统计指定分组下的服务器数量")
    public ReturnResult<Integer> getServerCountByGroup(
            @Parameter(description = "分组ID") @PathVariable Integer groupId) {
        return serverGroupService.getServerCountByGroup(groupId);
    }

    /**
     * 复制分组
     */
    @PostMapping("/{sourceGroupId}/copy")
    @ApiOperation(value = "复制分组")
    @Operation(summary = "复制分组", description = "复制现有分组创建新分组")
    public ReturnResult<Boolean> copyGroup(
            @Parameter(description = "源分组ID") @PathVariable Integer sourceGroupId,
            @Parameter(description = "新分组名称") @RequestParam String newGroupName) {
        return serverGroupService.copyGroup(sourceGroupId, newGroupName);
    }
}
