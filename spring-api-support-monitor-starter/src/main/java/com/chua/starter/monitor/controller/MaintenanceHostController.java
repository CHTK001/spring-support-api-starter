package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceHostRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceHostResponse;
import com.chua.starter.monitor.service.MaintenanceHostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 维护主机管理控制器
 * 提供维护主机的增删改查等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/maintenance/host")
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
    public ReturnResult<List<MaintenanceHostResponse>> listMaintenanceHosts(
            @Parameter(description = "维护组ID", required = true)
            Long maintenanceGroupId) {
        if (maintenanceGroupId == null) {
            return ReturnResult.ok(Collections.emptyList());
        }
        return maintenanceHostService.listMaintenanceHosts(maintenanceGroupId);
    }

    /**
     * 测试主机连接
     *
     * @param maintenanceHostId 维护主机ID
     * @return 测试结果
     */
    @GetMapping("/{id}/test")
    @Operation(summary = "测试主机连接", description = "测试与指定维护主机的连接是否正常")
    public ReturnResult<Boolean> testConnection(
            @Parameter(description = "维护主机ID", required = true) 
            @PathVariable("id") Integer maintenanceHostId) {
        return maintenanceHostService.testConnection(maintenanceHostId);
    }
} 