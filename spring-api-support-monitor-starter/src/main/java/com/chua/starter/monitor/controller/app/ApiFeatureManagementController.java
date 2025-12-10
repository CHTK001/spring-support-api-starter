package com.chua.starter.monitor.controller.app;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.ApiFeatureVO;
import com.chua.starter.monitor.service.ApiFeatureManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API 功能开关管理控制器
 * <p>
 * 用于管理客户端节点的 API 功能开关。
 * </p>
 *
 * @author CH
 * @since 2024/12/08
 * @version 1.0.0
 */
@RestController
@RequestMapping("/monitor/api-features")
@RequiredArgsConstructor
@Api(tags = "API功能开关管理")
public class ApiFeatureManagementController {

    private final ApiFeatureManagementService apiFeatureManagementService;

    /**
     * 获取节点的功能开关列表
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 功能开关列表
     */
    @GetMapping("/node")
    @ApiOperation("获取节点功能开关列表")
    public ReturnResult<List<ApiFeatureVO>> getNodeFeaturesForMonitor(
            @ApiParam("节点IP地址") @RequestParam String ipAddress,
            @ApiParam("节点端口") @RequestParam Integer port) {
        return apiFeatureManagementService.getNodeFeatures(ipAddress, port);
    }

    /**
     * 获取节点功能开关统计
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 统计信息
     */
    @GetMapping("/node/stats")
    @ApiOperation("获取节点功能开关统计")
    public ReturnResult<Map<String, Object>> getNodeFeatureStatsForMonitor(
            @ApiParam("节点IP地址") @RequestParam String ipAddress,
            @ApiParam("节点端口") @RequestParam Integer port) {
        return apiFeatureManagementService.getNodeFeatureStats(ipAddress, port);
    }

    /**
     * 设置节点功能开关状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param featureId 功能标识
     * @param enabled   是否启用
     * @return 操作结果
     */
    @PutMapping("/node/{featureId}")
    @ApiOperation("设置节点功能开关状态")
    public ReturnResult<Boolean> setNodeFeatureEnabledForMonitor(
            @ApiParam("节点IP地址") @RequestParam String ipAddress,
            @ApiParam("节点端口") @RequestParam Integer port,
            @ApiParam("功能标识") @PathVariable String featureId,
            @ApiParam("是否启用") @RequestParam boolean enabled) {
        return apiFeatureManagementService.setNodeFeatureEnabled(ipAddress, port, featureId, enabled);
    }

    /**
     * 批量设置节点功能开关状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param states    状态映射
     * @return 操作结果
     */
    @PutMapping("/node/batch")
    @ApiOperation("批量设置节点功能开关状态")
    public ReturnResult<Boolean> setNodeFeatureEnabledBatchForMonitor(
            @ApiParam("节点IP地址") @RequestParam String ipAddress,
            @ApiParam("节点端口") @RequestParam Integer port,
            @ApiParam("状态映射") @RequestBody Map<String, Boolean> states) {
        return apiFeatureManagementService.setNodeFeatureEnabledBatch(ipAddress, port, states);
    }

    /**
     * 重置节点所有功能开关到默认状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 操作结果
     */
    @PostMapping("/node/reset-all")
    @ApiOperation("重置节点所有功能开关")
    public ReturnResult<Boolean> resetNodeAllFeaturesForMonitor(
            @ApiParam("节点IP地址") @RequestParam String ipAddress,
            @ApiParam("节点端口") @RequestParam Integer port) {
        return apiFeatureManagementService.resetNodeAllFeatures(ipAddress, port);
    }
}
