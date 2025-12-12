package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;

import java.util.List;
import java.util.Map;

/**
 * 节点维护服务接口
 * <p>
 * 提供节点的备份、升级、还原功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
public interface NodeMaintenanceService {

    // ==================== 备份功能 ====================

    /**
     * 创建节点配置备份
     *
     * @param ipAddress   节点IP地址
     * @param port        节点端口
     * @param description 备份描述
     * @return 备份结果
     */
    ReturnResult<Map<String, Object>> createBackupForNode(String ipAddress, Integer port, String description);

    /**
     * 获取节点备份列表
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 备份列表
     */
    ReturnResult<List<Map<String, Object>>> listBackupsForNode(String ipAddress, Integer port);

    /**
     * 删除节点备份
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param fileName  备份文件名
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteBackupForNode(String ipAddress, Integer port, String fileName);

    /**
     * 获取备份内容
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param fileName  备份文件名
     * @return 备份内容
     */
    ReturnResult<Map<String, Object>> getBackupContentForNode(String ipAddress, Integer port, String fileName);

    // ==================== 升级功能 ====================

    /**
     * 获取升级包列表
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 升级包列表
     */
    ReturnResult<List<Map<String, Object>>> listUpgradePackagesForNode(String ipAddress, Integer port);

    /**
     * 上传升级包
     *
     * @param ipAddress   节点IP地址
     * @param port        节点端口
     * @param fileName    文件名
     * @param fileContent 文件内容（base64）
     * @return 上传结果
     */
    ReturnResult<Map<String, Object>> uploadUpgradePackageForNode(String ipAddress, Integer port, 
                                                                   String fileName, String fileContent);

    /**
     * 执行升级
     *
     * @param ipAddress   节点IP地址
     * @param port        节点端口
     * @param fileName    升级包文件名
     * @param autoRestart 是否自动重启
     * @param autoBackup  是否自动备份
     * @return 升级结果
     */
    ReturnResult<Map<String, Object>> executeUpgradeForNode(String ipAddress, Integer port,
                                                             String fileName, boolean autoRestart, boolean autoBackup);

    /**
     * 获取升级状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 升级状态
     */
    ReturnResult<Map<String, Object>> getUpgradeStatusForNode(String ipAddress, Integer port);

    // ==================== 还原功能 ====================

    /**
     * 预览还原（对比差异）
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param fileName  备份文件名
     * @return 差异信息
     */
    ReturnResult<Map<String, Object>> previewRestoreForNode(String ipAddress, Integer port, String fileName);

    /**
     * 执行还原
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param fileName  备份文件名
     * @return 还原结果
     */
    ReturnResult<Map<String, Object>> executeRestoreForNode(String ipAddress, Integer port, String fileName);
}
