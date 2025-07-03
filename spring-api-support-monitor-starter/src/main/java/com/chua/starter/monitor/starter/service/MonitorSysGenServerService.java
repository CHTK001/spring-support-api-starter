package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.pojo.ServerWithConfigDto;

import java.time.LocalDateTime;
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
     * 根据ID获取服务器信息和配置信息
     *
     * @param id 服务器ID
     * @return 包含服务器信息和配置信息的DTO
     */
    ReturnResult<ServerWithConfigDto> getByIdAndConfig(String id);

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
    ReturnResult<MonitorSysGenServer> getServerInfo(String id);

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

    /**
     * 更新服务器连接状态
     *
     * @param id 服务器ID
     * @param status 连接状态
     * @param errorMessage 错误信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateConnectionStatus(String id, Integer status, String errorMessage);

    /**
     * 批量测试服务器连接
     *
     * @param ids 服务器ID列表
     * @return 测试结果
     */
    ReturnResult<Map<String, Object>> batchTestConnection(List<String> ids);

    /**
     * 获取所有服务器连接状态
     *
     * @return 连接状态列表
     */
    ReturnResult<List<Map<String, Object>>> getAllConnectionStatus();

    /**
     * 定时检查服务器连接状态
     */
    void scheduleConnectionCheck();

    /**
     * 获取Guacamole代理连接URL
     *
     * @param id 服务器ID
     * @return 代理连接URL
     */
    ReturnResult<String> getGuacamoleProxyUrl(String id);

    /**
     * 测试代理连接
     *
     * @param id 服务器ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testProxyConnection(String id);

    /**
     * 获取服务器支持的数据上报方式
     *
     * @param id 服务器ID
     * @return 支持的上报方式列表
     */
    ReturnResult<List<String>> getSupportedReportMethods(String id);

    /**
     * 根据配置查询数据
     *
     * @param id         服务器ID
     * @param queryType  查询类型 (prometheus/sql)
     * @param expression 查询表达式
     * @param timeRange  时间范围参数
     * @return 查询结果
     */
    ReturnResult<Object> queryDataByConfig(String id, String queryType, String expression, Map<String, Object> timeRange);

    /**
     * 手动检测服务器信息
     *
     * @param id 服务器ID
     * @return 检测结果
     */
    ReturnResult<Map<String, Object>> detectServerInfo(String id);

    /**
     * 批量检测服务器信息
     *
     * @return 检测结果
     */
    ReturnResult<Map<String, Object>> batchDetectServerInfo();

    /**
     * 更新服务器数据上报配置
     *
     * @param id     服务器ID
     * @param config 上报配置
     * @return 操作结果
     */
    ReturnResult<Boolean> updateReportConfig(String id, Object config);

    /**
     * 获取服务器数据上报配置
     *
     * @param id 服务器ID
     * @return 上报配置
     */
    ReturnResult<Map<String, Object>> getReportConfig(String id);

    /**
     * 获取启用本地上报的服务器列表
     *
     * @return 本地上报服务器列表
     */
    List<MonitorSysGenServer> getLocalReportServers();

    /**
     * 更新服务器在线状态
     *
     * @param serverId 服务器ID
     * @param isOnline 是否在线
     * @return 更新结果
     */
    boolean updateServerOnlineStatus(Integer serverId, boolean isOnline);

    /**
     * 保存服务器指标数据
     *
     * @param serverId 服务器ID
     * @param systemData 系统数据
     * @return 保存结果
     */
    boolean saveServerMetrics(Integer serverId, Map<String, Object> systemData);

    /**
     * 通过WebSocket推送服务器数据
     *
     * @param serverId 服务器ID
     * @param systemData 系统数据
     */
    void pushServerDataToWebSocket(Integer serverId, Map<String, Object> systemData);

    /**
     * 清理过期的指标数据
     *
     * @param expireTime 过期时间戳
     * @return 清理结果
     */
    boolean cleanupExpiredMetrics(long expireTime);

    /**
     * 更新服务器健康状态
     *
     * @param serverId 服务器ID
     * @param isHealthy 是否健康
     * @return 更新结果
     */
    boolean updateServerHealthStatus(Integer serverId, boolean isHealthy);

    /**
     * 保存服务器配置（包含业务逻辑验证）
     *
     * @param monitorSysGenServer 服务器实体
     * @return 保存结果
     */
    ReturnResult<MonitorSysGenServer> saveServer(MonitorSysGenServer monitorSysGenServer);

    /**
     * 更新服务器配置（包含业务逻辑验证）
     *
     * @param monitorSysGenServer 服务器实体
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServer(MonitorSysGenServer monitorSysGenServer);

    /**
     * 删除服务器配置（包含业务逻辑验证）
     *
     * @param id 服务器ID字符串（支持逗号分隔的多个ID）
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteServer(String id);

    /**
     * 测试本地IP检测功能（包含业务逻辑验证）
     *
     * @param host 主机地址
     * @return 检测结果
     */
    ReturnResult<Map<String, Object>> testLocalIpDetection(String host);

    /**
     * 清理过期连接状态数据
     *
     * @param expireTime 过期时间
     * @return 清理的记录数
     */
    ReturnResult<Integer> cleanupExpiredConnectionStatus(LocalDateTime expireTime);

    /**
     * 清理临时文件
     *
     * @return 清理的文件数
     */
    ReturnResult<Integer> cleanupTempFiles();

    /**
     * 清理缓存
     *
     * @return 清理结果
     */
    ReturnResult<Void> cleanupCache();

    /**
     * 优化服务器相关数据表
     *
     * @return 优化结果
     */
    ReturnResult<Void> optimizeTables();

    /**
     * 检查数据库健康状态
     *
     * @return 检查结果
     */
    ReturnResult<Void> checkDatabaseHealth();

    /**
     * 检查磁盘空间
     *
     * @return 检查结果
     */
    ReturnResult<Void> checkDiskSpace();

    /**
     * 检查内存使用情况
     *
     * @return 检查结果
     */
    ReturnResult<Void> checkMemoryUsage();

    /**
     * 获取所有在线服务器ID
     *
     * @return 在线服务器ID列表
     */
    List<Integer> getAllOnlineServerIds();

    /**
     * 获取服务器名称
     *
     * @param serverId 服务器ID
     * @return 服务器名称
     */
    String getServerName(Integer serverId);

    /**
     * 根据时间范围查询服务器指标数据
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  数据间隔（分钟）
     * @return 指标数据列表
     */
    ReturnResult<List<Map<String, Object>>> getMetricsHistory(Integer serverId, String startTime, String endTime, Integer interval);

    /**
     * 测试Prometheus连接
     *
     * @param serverId 服务器ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testPrometheusConnection(Integer serverId);

}
