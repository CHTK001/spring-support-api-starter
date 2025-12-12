package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;

import java.util.Map;

/**
 * 节点日志服务接口
 * <p>
 * 提供节点日志订阅和推送功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
public interface NodeLogService {

    /**
     * 订阅节点日志
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param level     日志级别（TRACE/DEBUG/INFO/WARN/ERROR）
     * @return 订阅结果
     */
    ReturnResult<Map<String, Object>> subscribeLogForNode(String ipAddress, Integer port, String level);

    /**
     * 取消订阅节点日志
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 取消结果
     */
    ReturnResult<Boolean> unsubscribeLogForNode(String ipAddress, Integer port);

    /**
     * 处理接收到的日志（从 Agent 推送）
     *
     * @param logData 日志数据
     */
    void handleLogPush(Map<String, Object> logData);
}
