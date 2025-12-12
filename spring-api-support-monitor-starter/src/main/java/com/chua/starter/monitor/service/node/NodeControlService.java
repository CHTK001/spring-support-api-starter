package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.node.NodeControlRequest;
import com.chua.starter.monitor.pojo.node.NodeMetricsDTO;

import java.util.List;
import java.util.Map;

/**
 * 节点控制服务接口
 * <p>
 * 提供节点的日志管理、配置管理、重启管理等功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/08
 */
public interface NodeControlService {

    // ==================== 日志管理 ====================

    /**
     * 通过actuator获取节点日志文件列表
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 日志文件列表
     */
    ReturnResult<List<String>> getLogFilesForNode(String ipAddress, Integer port);

    /**
     * 通过actuator获取节点日志内容
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param logFile   日志文件名（可选）
     * @param lines     行数（可选，默认100）
     * @return 日志内容
     */
    ReturnResult<String> getLogContentForNode(String ipAddress, Integer port, String logFile, Integer lines);

    /**
     * 通过SyncServer通知客户端设置日志级别
     *
     * @param request 控制请求
     * @return 操作结果
     */
    ReturnResult<Boolean> setLogLevelForNode(NodeControlRequest request);

    // ==================== 配置管理 ====================

    /**
     * 通过actuator获取节点环境配置
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 环境配置信息
     */
    ReturnResult<Map<String, Object>> getEnvironmentForNode(String ipAddress, Integer port);

    /**
     * 通过actuator获取节点配置属性
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param prefix    配置前缀（可选）
     * @return 配置属性
     */
    ReturnResult<Map<String, Object>> getConfigPropsForNode(String ipAddress, Integer port, String prefix);

    /**
     * 通过actuator获取节点系统信息
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 系统信息
     */
    ReturnResult<Map<String, Object>> getSystemInfoForNode(String ipAddress, Integer port);

    // ==================== 监控数据管理 ====================

    /**
     * 获取节点监控指标（从Redis缓存）
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 监控指标
     */
    ReturnResult<NodeMetricsDTO> getNodeMetricsForNode(String ipAddress, Integer port);

    /**
     * 获取节点监控指标历史数据
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param minutes   最近多少分钟的数据
     * @return 历史监控数据列表
     */
    ReturnResult<List<NodeMetricsDTO>> getNodeMetricsHistoryForNode(String ipAddress, Integer port, Integer minutes);

    /**
     * 保存节点监控指标到Redis
     *
     * @param metrics 监控指标
     * @return 操作结果
     */
    ReturnResult<Boolean> saveNodeMetricsForNode(NodeMetricsDTO metrics);

    // ==================== 重启管理 ====================

    /**
     * 通过SyncServer通知客户端重启
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 操作结果
     */
    ReturnResult<Boolean> restartNodeForNode(String ipAddress, Integer port);

    /**
     * 通过SyncServer通知客户端优雅关闭
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 操作结果
     */
    ReturnResult<Boolean> shutdownNodeForNode(String ipAddress, Integer port);

    // ==================== 通用方法 ====================

    /**
     * 向节点发送控制命令
     *
     * @param request 控制请求
     * @return 操作结果
     */
    ReturnResult<Object> sendControlCommandForNode(NodeControlRequest request);

    /**
     * 检查节点是否在线
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 是否在线
     */
    boolean isNodeOnlineForNode(String ipAddress, Integer port);

    // ==================== 链路追踪（Actuator）====================

    /**
     * 通过 Actuator 获取节点最新的 HTTP 链路追踪
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param limit     返回条数限制（可选）
     * @return HTTP 追踪列表
     */
    ReturnResult<List<Map<String, Object>>> getHttpTracesForNode(String ipAddress, Integer port, Integer limit);
}
