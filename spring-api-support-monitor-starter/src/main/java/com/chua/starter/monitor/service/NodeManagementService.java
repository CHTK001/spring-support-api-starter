package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.pojo.NodeStatistics;

import java.util.List;
import java.util.Map;

/**
 * 在线节点管理服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface NodeManagementService {

    /**
     * 获取所有在线节点列表
     *
     * @return 在线节点列表
     */
    ReturnResult<List<OnlineNodeInfo>> getAllOnlineNodes();

    /**
     * 根据应用名称分组获取在线节点
     *
     * @return 按应用名称分组的节点列表
     */
    ReturnResult<Map<String, List<OnlineNodeInfo>>> getOnlineNodesByApplication();

    /**
     * 获取节点统计信息
     *
     * @return 节点统计信息
     */
    ReturnResult<NodeStatistics> getNodeStatistics();

    /**
     * 根据节点ID获取节点详细信息
     *
     * @param nodeId 节点ID
     * @return 节点详细信息
     */
    ReturnResult<OnlineNodeInfo> getNodeDetails(String nodeId);

    /**
     * 搜索节点
     *
     * @param keyword 搜索关键词（支持节点名称、IP地址、应用名称）
     * @return 匹配的节点列表
     */
    ReturnResult<List<OnlineNodeInfo>> searchNodes(String keyword);

    /**
     * 根据IP地址和端口获取在线节点信息
     *
     * @param ipAddress IP地址
     * @param port      端口号
     * @return 在线节点信息
     */
    OnlineNodeInfo getOnlineNode(String ipAddress, Integer port);
}
