package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceScriptRequest;
import com.chua.starter.monitor.pojo.request.RunScriptRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceScriptResponse;
import com.chua.starter.monitor.pojo.response.RunScriptResponse;
import com.chua.starter.monitor.service.MaintenanceScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 维护脚本管理控制器
 * 提供维护脚本的增删改查、执行等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/api/maintenance/script")
@Tag(name = "维护脚本管理")
public class MaintenanceScriptController {

    /**
     * 维护脚本服务
     */
    @Resource
    private MaintenanceScriptService maintenanceScriptService;

    /**
     * 创建维护脚本
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping("save")
    @Operation(summary = "创建维护脚本")
    public ReturnResult<MaintenanceScriptResponse> createMaintenanceScript(@RequestBody MaintenanceScriptRequest request) {
        if (request.getMaintenanceGroupId() == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceScriptService.createMaintenanceScript(request);
    }

    /**
     * 更新维护脚本
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新维护脚本")
    public ReturnResult<MaintenanceScriptResponse> updateMaintenanceScript(@RequestBody MaintenanceScriptRequest request) {
        return maintenanceScriptService.updateMaintenanceScript(request);
    }

    /**
     * 删除维护脚本
     *
     * @param maintenanceScriptId 脚本ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护脚本")
    public ReturnResult<Boolean> deleteMaintenanceScript(@PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.deleteMaintenanceScript(maintenanceScriptId);
    }

    /**
     * 获取维护脚本详情
     *
     * @param maintenanceScriptId 脚本ID
     * @return 脚本详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护脚本详情")
    public ReturnResult<MaintenanceScriptResponse> getMaintenanceScript(@PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.getMaintenanceScript(maintenanceScriptId);
    }

    /**
     * 获取维护脚本列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 脚本列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护脚本列表")
    public ReturnResult<List<MaintenanceScriptResponse>> listMaintenanceScripts(
            @RequestParam(value = "groupId") Long maintenanceGroupId) {
        if (maintenanceGroupId == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceScriptService.listMaintenanceScripts(maintenanceGroupId);
    }

    /**
     * 运行脚本
     *
     * @param request 请求参数
     * @return 运行结果
     */
    @PostMapping("/run")
    @Operation(summary = "运行脚本")
    public ReturnResult<RunScriptResponse> runScript(@RequestBody RunScriptRequest request) {
        return maintenanceScriptService.runScript(request);
    }

    /**
     * 更新脚本状态
     *
     * @param maintenanceScriptId 脚本ID
     * @param status              状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新脚本状态")
    public ReturnResult<Boolean> updateScriptStatus(
            @PathVariable("id") Long maintenanceScriptId,
            @PathVariable("status") Integer status) {
        return maintenanceScriptService.updateScriptStatus(maintenanceScriptId, status);
    }
} 