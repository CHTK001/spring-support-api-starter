package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceHostRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceHostResponse;
import com.chua.starter.monitor.service.MaintenanceHostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
@Tag(name = "维护主机管理")
public class MaintenanceHostController {

    /**
     * 维护主机服务
     */
    @Resource
    private MaintenanceHostService maintenanceHostService;

    /**
     * 创建维护主机
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping("save")
    @Operation(summary = "创建维护主机")
    public ReturnResult<MaintenanceHostResponse> createMaintenanceHost(@RequestBody MaintenanceHostRequest request) {
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
    @Operation(summary = "更新维护主机")
    public ReturnResult<MaintenanceHostResponse> updateMaintenanceHost(@RequestBody MaintenanceHostRequest request) {
        return maintenanceHostService.updateMaintenanceHost(request);
    }

    /**
     * 删除维护主机
     *
     * @param maintenanceHostId 维护主机ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护主机")
    public ReturnResult<Boolean> deleteMaintenanceHost(@PathVariable("id") Long maintenanceHostId) {
        return maintenanceHostService.deleteMaintenanceHost(maintenanceHostId);
    }

    /**
     * 获取维护主机详情
     *
     * @param maintenanceHostId 维护主机ID
     * @return 维护主机详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护主机详情")
    public ReturnResult<MaintenanceHostResponse> getMaintenanceHost(@PathVariable("id") Long maintenanceHostId) {
        return maintenanceHostService.getMaintenanceHost(maintenanceHostId);
    }

    /**
     * 获取维护主机列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护主机列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护主机列表")
    public ReturnResult<List<MaintenanceHostResponse>> listMaintenanceHosts(
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
    @Operation(summary = "启用或停用维护主机")
    public ReturnResult<Boolean> enableMaintenanceHost(
            @PathVariable("id") Long maintenanceHostId,
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
    @Operation(summary = "测试主机连接")
    public ReturnResult<Boolean> testConnection(@PathVariable("id") Integer maintenanceHostId) {
        return maintenanceHostService.testConnection(maintenanceHostId);
    }
} 