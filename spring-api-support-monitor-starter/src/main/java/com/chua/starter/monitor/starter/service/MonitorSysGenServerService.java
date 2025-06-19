package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;

import java.util.List;
import java.util.Map;

/**
 * 服务器管理服务接口
 *
 * @author CH
 * @since 2024/12/18
 */
public interface MonitorSysGenServerService extends IService<MonitorSysGenServer> {

    /**
     * 分页查询服务器列表
     *
     * @param page 分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<MonitorSysGenServer> pageFor(Page<MonitorSysGenServer> page, MonitorSysGenServer entity);

    /**
     * 测试服务器连接
     *
     * @param id 服务器ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testConnection(String id);

    /**
     * 连接服务器
     *
     * @param id 服务器ID
     * @return 连接结果
     */
    ReturnResult<Boolean> connect(String id);

    /**
     * 断开服务器连接
     *
     * @param id 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> disconnect(String id);

    /**
     * 获取服务器连接状态
     *
     * @param id 服务器ID
     * @return 连接状态
     */
    ReturnResult<Map<String, Object>> getConnectionStatus(String id);

    /**
     * 执行服务器命令
     *
     * @param id 服务器ID
     * @param command 命令
     * @return 执行结果
     */
    ReturnResult<String> executeCommand(String id, String command);

    /**
     * 获取服务器信息
     *
     * @param id 服务器ID
     * @return 服务器信息
     */
    ReturnResult<Map<String, Object>> getServerInfo(String id);

    /**
     * 发送数据到服务器
     *
     * @param id 服务器ID
     * @param data 数据
     * @return 操作结果
     */
    ReturnResult<Boolean> sendData(String id, String data);

    /**
     * 调整终端大小
     *
     * @param id 服务器ID
     * @param width 宽度
     * @param height 高度
     * @return 操作结果
     */
    ReturnResult<Boolean> resizeTerminal(String id, int width, int height);

    /**
     * 获取文件列表
     *
     * @param id 服务器ID
     * @param path 路径
     * @return 文件列表
     */
    ReturnResult<Map<String, Object>> listFiles(String id, String path);

    /**
     * 上传文件
     *
     * @param id 服务器ID
     * @param localPath 本地路径
     * @param remotePath 远程路径
     * @return 上传结果
     */
    ReturnResult<Boolean> uploadFile(String id, String localPath, String remotePath);

    /**
     * 下载文件
     *
     * @param id 服务器ID
     * @param remotePath 远程路径
     * @param localPath 本地路径
     * @return 下载结果
     */
    ReturnResult<Boolean> downloadFile(String id, String remotePath, String localPath);

    /**
     * 启用服务器监控
     *
     * @param id 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> enableMonitoring(String id);

    /**
     * 停用服务器监控
     *
     * @param id 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> disableMonitoring(String id);

    /**
     * 获取服务器监控状态
     *
     * @param id 服务器ID
     * @return 监控状态
     */
    ReturnResult<Map<String, Object>> getMonitoringStatus(String id);

    /**
     * 手动收集服务器指标
     *
     * @param id 服务器ID
     * @return 收集结果
     */
    ReturnResult<MonitorSysGenServerMetrics> collectMetrics(String id);

    /**
     * 获取服务器列表（按标签分组）
     *
     * @return 分组的服务器列表
     */
    ReturnResult<Map<String, List<MonitorSysGenServer>>> getServersByTags();

    /**
     * 批量操作服务器
     *
     * @param ids 服务器ID列表
     * @param action 操作类型 (connect/disconnect/enable_monitoring/disable_monitoring)
     * @return 操作结果
     */
    ReturnResult<Map<String, Object>> batchOperation(List<String> ids, String action);

    /**
     * 获取服务器统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getServerStatistics();

    /**
     * 导出服务器配置
     *
     * @param ids 服务器ID列表
     * @return 配置数据
     */
    ReturnResult<String> exportServerConfig(List<String> ids);

    /**
     * 导入服务器配置
     *
     * @param configData 配置数据
     * @return 导入结果
     */
    ReturnResult<Map<String, Object>> importServerConfig(String configData);

    /**
     * 克隆服务器配置
     *
     * @param sourceId 源服务器ID
     * @param targetName 目标服务器名称
     * @return 克隆结果
     */
    ReturnResult<MonitorSysGenServer> cloneServer(String sourceId, String targetName);
}
