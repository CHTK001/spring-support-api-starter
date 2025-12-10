package com.chua.starter.monitor.controller.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerGroup;
import com.chua.starter.monitor.service.MonitorSysGenServerGroupService;
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
@RequestMapping("/v1/server/group")
@RequiredArgsConstructor
@Api(tags = "服务器分组管理")
@Tag(name = "服务器分组管理", description = "服务器分组的增删改查操作")
public class MonitorSysGenServerGroupController {

    private final MonitorSysGenServerGroupService serverGroupService;

    /**
     * 分页查询服务器分组（包含服务器数量）
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询服务器分组")
    @Operation(summary = "分页查询服务器分组", description = "根据条件分页查询服务器分组列表，包含服务器数量")
    public ReturnResult<IPage<com.chua.starter.monitor.pojo.MonitorSysGenServerGroupWithCount>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "查询条件") MonitorSysGenServerGroup entity) {
        try {
            Page<com.chua.starter.monitor.pojo.MonitorSysGenServerGroupWithCount> page = new Page<>(current,
                    size);
            return serverGroupService.pageForWithServerCount(page, entity);
        } catch (Exception e) {
            log.error("分页查询服务器分组失败", e);
            return ReturnResult.error("分页查询服务器分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的分组列表（包含服务器数量）
     */
    @GetMapping("/enabled")
    @ApiOperation(value = "获取启用的分组列表")
    @Operation(summary = "获取启用的分组列表", description = "获取所有启用状态的服务器分组，包含服务器数量")
    public ReturnResult<List<com.chua.starter.monitor.pojo.MonitorSysGenServerGroupWithCount>> getEnabledGroups() {
        return serverGroupService.getEnabledGroupsWithServerCount();
    }

    /**
     * 获取所有分组列表（包含服务器数量）
     */
    @GetMapping("/all")
    @ApiOperation(value = "获取所有分组列表")
    @Operation(summary = "获取所有分组列表", description = "获取所有服务器分组，包含服务器数量")
    public ReturnResult<List<com.chua.starter.monitor.pojo.MonitorSysGenServerGroupWithCount>> getAllGroups() {
        return serverGroupService.getAllGroupsWithServerCount();
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
    public ReturnResult<Boolean> deleteGroup(@Parameter(description = "分组ID") @PathVariable Integer groupId) {
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
     * 获取默认分组
     */
    @GetMapping("/default")
    @ApiOperation(value = "获取默认分组")
    @Operation(summary = "获取默认分组", description = "获取当前设置的默认分组")
    public ReturnResult<MonitorSysGenServerGroup> getDefaultGroup() {
        return serverGroupService.getDefaultGroup();
    }

    /**
     * 检查分组名称是否存在
     */
    @GetMapping("/check-name")
    @ApiOperation(value = "检查分组名称是否存在")
    @Operation(summary = "检查分组名称是否存在", description = "检查指定的分组名称是否已存在")
    public ReturnResult<Boolean> checkGroupNameExists(@Parameter(description = "分组名称") @RequestParam String groupName,
            @Parameter(description = "排除的分组ID") @RequestParam(required = false) Integer excludeId) {
        return serverGroupService.checkGroupNameExists(groupName, excludeId);
    }

    /**
     * 复制分组
     */
    @PostMapping("/{sourceGroupId}/copy")
    @ApiOperation(value = "复制分组")
    @Operation(summary = "复制分组", description = "复制现有分组创建新分组")
    public ReturnResult<Boolean> copyGroup(@Parameter(description = "源分组ID") @PathVariable Integer sourceGroupId,
            @Parameter(description = "新分组名称") @RequestParam String newGroupName) {
        return serverGroupService.copyGroup(sourceGroupId, newGroupName);
    }
}
