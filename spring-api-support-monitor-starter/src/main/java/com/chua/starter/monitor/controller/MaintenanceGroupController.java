package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceGroupRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceGroupResponse;
import com.chua.starter.monitor.service.MaintenanceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 维护组管理控制器
 * 提供维护组的增删改查等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/maintenance/group")
@Tag(name = "维护组管理", description = "维护组的创建、修改、删除、查询等操作")
@RequiredArgsConstructor
public class MaintenanceGroupController {

    /**
     * 维护组服务
     */
    private final MaintenanceGroupService maintenanceGroupService;

    /**
     * 创建维护组
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping("save")
    @Operation(summary = "创建维护组", description = "创建一个新的维护组，返回创建后的维护组信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceGroupResponse.class))),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceGroupResponse> createMaintenanceGroup(
            @Parameter(description = "维护组信息", required = true) 
            @RequestBody MaintenanceGroupRequest request) {
        return maintenanceGroupService.createMaintenanceGroup(request);
    }

    /**
     * 更新维护组
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新维护组", description = "根据ID更新维护组信息，返回更新后的维护组信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceGroupResponse.class))),
        @ApiResponse(responseCode = "404", description = "维护组不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceGroupResponse> updateMaintenanceGroup(
            @Parameter(description = "维护组信息", required = true) 
            @RequestBody MaintenanceGroupRequest request) {
        return maintenanceGroupService.updateMaintenanceGroup(request);
    }

    /**
     * 删除维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护组", description = "根据ID删除维护组")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> deleteMaintenanceGroup(
            @Parameter(description = "维护组ID", required = true) 
            @PathVariable("id") Long maintenanceGroupId) {
        return maintenanceGroupService.deleteMaintenanceGroup(maintenanceGroupId);
    }

    /**
     * 获取维护组详情
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护组详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护组详情", description = "根据ID获取维护组详细信息，包含主机数量和脚本数量")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceGroupResponse.class))),
        @ApiResponse(responseCode = "404", description = "维护组不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceGroupResponse> getMaintenanceGroup(
            @Parameter(description = "维护组ID", required = true) 
            @PathVariable("id") Long maintenanceGroupId) {
        return maintenanceGroupService.getMaintenanceGroup(maintenanceGroupId);
    }

    /**
     * 获取维护组列表
     *
     * @return 维护组列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护组列表", description = "获取所有维护组列表，每个维护组包含主机数量和脚本数量")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceGroupResponse.class))),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
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
    @Operation(summary = "启用或停用维护组", description = "修改维护组的启用状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "维护组不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> enableMaintenanceGroup(
            @Parameter(description = "维护组ID", required = true) 
            @PathVariable("id") Long maintenanceGroupId,
            @Parameter(description = "是否启用: true-启用, false-禁用", required = true) 
            @PathVariable("enabled") Boolean enabled) {
        return maintenanceGroupService.enableMaintenanceGroup(maintenanceGroupId, enabled);
    }
} 