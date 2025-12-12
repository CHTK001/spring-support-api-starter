package com.chua.starter.monitor.controller.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.node.NodeMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 节点维护控制器
 * <p>
 * 提供节点的备份、升级、还原功能的 REST API
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@RestController
@RequestMapping("/v1/node/maintenance")
@RequiredArgsConstructor
@Tag(name = "节点维护", description = "节点备份、升级、还原功能")
public class NodeMaintenanceController {

    private final NodeMaintenanceService nodeMaintenanceService;

    // ==================== 备份功能 ====================

    /**
     * 创建节点配置备份
     */
    @PostMapping("/backup/create")
    @Operation(summary = "创建备份", description = "创建节点的配置备份")
    public ReturnResult<Map<String, Object>> createBackupForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "备份描述") @RequestParam(defaultValue = "手动备份") String description) {
        return nodeMaintenanceService.createBackupForNode(ipAddress, port, description);
    }

    /**
     * 获取节点备份列表
     */
    @GetMapping("/backup/list")
    @Operation(summary = "获取备份列表", description = "获取节点的所有备份")
    public ReturnResult<List<Map<String, Object>>> listBackupsForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeMaintenanceService.listBackupsForNode(ipAddress, port);
    }

    /**
     * 删除节点备份
     */
    @DeleteMapping("/backup/delete")
    @Operation(summary = "删除备份", description = "删除指定的备份文件")
    public ReturnResult<Boolean> deleteBackupForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "备份文件名") @RequestParam String fileName) {
        return nodeMaintenanceService.deleteBackupForNode(ipAddress, port, fileName);
    }

    /**
     * 获取备份内容
     */
    @GetMapping("/backup/content")
    @Operation(summary = "获取备份内容", description = "获取备份文件的详细内容")
    public ReturnResult<Map<String, Object>> getBackupContentForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "备份文件名") @RequestParam String fileName) {
        return nodeMaintenanceService.getBackupContentForNode(ipAddress, port, fileName);
    }

    // ==================== 升级功能 ====================

    /**
     * 获取升级包列表
     */
    @GetMapping("/upgrade/list")
    @Operation(summary = "获取升级包列表", description = "获取节点上的升级包列表")
    public ReturnResult<List<Map<String, Object>>> listUpgradePackagesForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeMaintenanceService.listUpgradePackagesForNode(ipAddress, port);
    }

    /**
     * 上传升级包
     */
    @PostMapping("/upgrade/upload")
    @Operation(summary = "上传升级包", description = "上传升级包到节点")
    public ReturnResult<Map<String, Object>> uploadUpgradePackageForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "文件名") @RequestParam String fileName,
            @Parameter(description = "文件内容（base64）") @RequestBody String fileContent) {
        return nodeMaintenanceService.uploadUpgradePackageForNode(ipAddress, port, fileName, fileContent);
    }

    /**
     * 执行升级
     */
    @PostMapping("/upgrade/execute")
    @Operation(summary = "执行升级", description = "执行节点升级")
    public ReturnResult<Map<String, Object>> executeUpgradeForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "升级包文件名") @RequestParam String fileName,
            @Parameter(description = "是否自动重启") @RequestParam(defaultValue = "true") boolean autoRestart,
            @Parameter(description = "是否自动备份") @RequestParam(defaultValue = "true") boolean autoBackup) {
        return nodeMaintenanceService.executeUpgradeForNode(ipAddress, port, fileName, autoRestart, autoBackup);
    }

    /**
     * 获取升级状态
     */
    @GetMapping("/upgrade/status")
    @Operation(summary = "获取升级状态", description = "获取节点的当前版本和升级状态")
    public ReturnResult<Map<String, Object>> getUpgradeStatusForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeMaintenanceService.getUpgradeStatusForNode(ipAddress, port);
    }

    // ==================== 还原功能 ====================

    /**
     * 预览还原
     */
    @GetMapping("/restore/preview")
    @Operation(summary = "预览还原", description = "预览还原操作，对比当前配置与备份的差异")
    public ReturnResult<Map<String, Object>> previewRestoreForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "备份文件名") @RequestParam String fileName) {
        return nodeMaintenanceService.previewRestoreForNode(ipAddress, port, fileName);
    }

    /**
     * 执行还原
     */
    @PostMapping("/restore/execute")
    @Operation(summary = "执行还原", description = "执行配置还原")
    public ReturnResult<Map<String, Object>> executeRestoreForMaintenance(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "备份文件名") @RequestParam String fileName) {
        return nodeMaintenanceService.executeRestoreForNode(ipAddress, port, fileName);
    }
}
