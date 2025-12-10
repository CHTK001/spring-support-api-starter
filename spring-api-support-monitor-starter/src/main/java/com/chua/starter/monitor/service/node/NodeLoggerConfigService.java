package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.BatchOperationResultDTO;
import com.chua.starter.monitor.pojo.node.NodeLoggerConfigDTO;
import com.chua.starter.monitor.pojo.node.NodeLoggerStatsDTO;

import java.util.List;

/**
 * 节点日志器配置服务接口
 *
 * 纯粹通过 Spring Boot Actuator 端点管理节点日志器配置 不涉及数据库操作，所有数据都是实时从节点获取
 *
 * @author CH
 * @since 2025/01/17
 */
public interface NodeLoggerConfigService {

    /**
     * 获取节点的所有日志器配置
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 日志器配置列表
     */
    ReturnResult<List<NodeLoggerConfigDTO>> getNodeLoggers(String nodeUrl);

    /**
     * 获取指定日志器的详细配置
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @return 日志器配置详情
     */
    ReturnResult<NodeLoggerConfigDTO> getLoggerConfig(String nodeUrl, String loggerName);

    /**
     * 设置日志器等级
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @param level      日志等级
     * @return 设置结果
     */
    ReturnResult<Boolean> setLoggerLevel(String nodeUrl, String loggerName, String level);

    /**
     * 批量设置相同应用的所有节点日志等级
     *
     * @param applicationName 应用名称
     * @param loggerName      日志器名称
     * @param level           日志等级
     * @return 批量设置结果
     */
    ReturnResult<BatchOperationResultDTO> setLoggerLevelForAllNodes(String applicationName, String loggerName,
            String level);

    /**
     * 获取相同应用名称的所有节点URL
     *
     * @param applicationName 应用名称
     * @return 节点URL列表
     */
    ReturnResult<List<String>> getNodesByApplicationName(String applicationName);

    /**
     * 获取所有应用名称列表
     *
     * @return 应用名称列表
     */
    ReturnResult<List<String>> getAllApplicationNames();

    /**
     * 获取节点日志器配置统计信息
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 统计信息
     */
    ReturnResult<NodeLoggerStatsDTO> getLoggerConfigStats(String nodeUrl);

    /**
     * 重置日志器配置（恢复为继承状态）
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @return 重置结果
     */
    ReturnResult<Boolean> resetLoggerLevel(String nodeUrl, String loggerName);

    /**
     * 批量重置应用的所有节点日志器配置
     *
     * @param applicationName 应用名称
     * @param loggerName      日志器名称
     * @return 批量重置结果
     */
    ReturnResult<BatchOperationResultDTO> resetLoggerLevelForAllNodes(String applicationName, String loggerName);

    /**
     * 检查节点是否在线并可访问
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 节点状态
     */
    ReturnResult<Boolean> checkNodeStatus(String nodeUrl);

    /**
     * 从远程节点获取实时日志器配置
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 远程日志器配置
     */
    ReturnResult<List<NodeLoggerConfigDTO>> fetchRemoteLoggers(String nodeUrl);

    /**
     * 向远程节点设置日志器等级
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @param level      日志等级
     * @return 设置结果
     */
    ReturnResult<Boolean> setRemoteLoggerLevel(String nodeUrl, String loggerName, String level);

    /**
     * 解码节点URL
     *
     * @param encodedNodeUrl Base64编码的节点URL
     * @return 解码后的IP:Port格式
     */
    String decodeNodeUrl(String encodedNodeUrl);

    /**
     * 编码节点URL
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return Base64编码的节点URL
     */
    String encodeNodeUrl(String ipAddress, Integer port);

    /**
     * 注册节点到应用缓存
     *
     * @param nodeUrl         节点URL（Base64编码）
     * @param applicationName 应用名称
     */
    void registerNodeToApplication(String nodeUrl, String applicationName);

    /**
     * 从应用缓存中移除节点
     *
     * @param nodeUrl 节点URL（Base64编码）
     */
    void unregisterNodeFromApplication(String nodeUrl);
}
