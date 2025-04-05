package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceHostRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceHostResponse;
import com.chua.starter.monitor.service.MaintenanceHostService;
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
 * 维护主机管理控制器
 * 提供维护主机的增删改查等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/api/maintenance/host")
@Tag(name = "维护主机管理", description = "维护主机的创建、修改、删除、查询等操作")
@RequiredArgsConstructor
public class MaintenanceHostController {

    /**
     * 维护主机服务
     */
    private final MaintenanceHostService maintenanceHostService;

    /**
     * 创建维护主机
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping("save")
    @Operation(summary = "创建维护主机", description = "创建一个新的维护主机，返回创建后的维护主机信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceHostResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceHostResponse> createMaintenanceHost(
            @Parameter(description = "维护主机信息", required = true) 
            @RequestBody MaintenanceHostRequest request) {
        if (request.getMaintenanceGroupId() == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceHostService.createMaintenanceHost(request);
    }

    /**
     * 更新维护主机
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新维护主机", description = "根据ID更新维护主机信息，返回更新后的维护主机信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceHostResponse.class))),
        @ApiResponse(responseCode = "404", description = "维护主机不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceHostResponse> updateMaintenanceHost(
            @Parameter(description = "维护主机信息", required = true) 
            @RequestBody MaintenanceHostRequest request) {
        return maintenanceHostService.updateMaintenanceHost(request);
    }

    /**
     * 删除维护主机
     *
     * @param maintenanceHostId 维护主机ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护主机", description = "根据ID删除维护主机")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> deleteMaintenanceHost(
            @Parameter(description = "维护主机ID", required = true) 
            @PathVariable("id") Long maintenanceHostId) {
        return maintenanceHostService.deleteMaintenanceHost(maintenanceHostId);
    }

    /**
     * 获取维护主机详情
     *
     * @param maintenanceHostId 维护主机ID
     * @return 维护主机详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护主机详情", description = "根据ID获取维护主机详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceHostResponse.class))),
        @ApiResponse(responseCode = "404", description = "维护主机不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceHostResponse> getMaintenanceHost(
            @Parameter(description = "维护主机ID", required = true) 
            @PathVariable("id") Long maintenanceHostId) {
        return maintenanceHostService.getMaintenanceHost(maintenanceHostId);
    }

    /**
     * 获取维护主机列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护主机列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护主机列表", description = "根据维护组ID获取该组下的所有维护主机列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceHostResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<List<MaintenanceHostResponse>> listMaintenanceHosts(
            @Parameter(description = "维护组ID", required = true) 
            @RequestParam(value = "groupId") Long maintenanceGroupId) {
        if (maintenanceGroupId == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceHostService.listMaintenanceHosts(maintenanceGroupId);
    }

    /**
     * 启用或停用维护主机
     *
     * @param maintenanceHostId 维护主机ID
     * @param enabled           是否启用
     * @return 操作结果
     */
    @PutMapping("/{id}/enable/{enabled}")
    @Operation(summary = "启用或停用维护主机", description = "修改维护主机的启用状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "维护主机不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> enableMaintenanceHost(
            @Parameter(description = "维护主机ID", required = true) 
            @PathVariable("id") Long maintenanceHostId,
            @Parameter(description = "是否启用: true-启用, false-禁用", required = true) 
            @PathVariable("enabled") Boolean enabled) {
        return maintenanceHostService.enableMaintenanceHost(maintenanceHostId, enabled);
    }

    /**
     * 测试主机连接
     *
     * @param maintenanceHostId 维护主机ID
     * @return 测试结果
     */
    @GetMapping("/{id}/test")
    @Operation(summary = "测试主机连接", description = "测试与指定维护主机的连接是否正常")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "测试成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "维护主机不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> testConnection(
            @Parameter(description = "维护主机ID", required = true) 
            @PathVariable("id") Integer maintenanceHostId) {
        return maintenanceHostService.testConnection(maintenanceHostId);
    }
} 