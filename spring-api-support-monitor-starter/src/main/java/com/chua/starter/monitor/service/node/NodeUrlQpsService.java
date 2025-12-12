package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.node.NodeUrlMappingDTO;
import com.chua.starter.monitor.pojo.node.NodeUrlQpsDTO;

import java.util.List;
import java.util.Map;

/**
 * 节点 URL QPS 统计服务接口
 * <p>
 * 接收客户端上报的 URL QPS 数据，提供查询接口
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
public interface NodeUrlQpsService {

    /**
     * 处理客户端上报的 URL QPS 数据
     *
     * @param data 上报数据
     */
    void handleQpsReport(Map<String, Object> data);

    /**
     * 获取节点的 URL QPS 统计数据
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return QPS 统计列表
     */
    ReturnResult<List<NodeUrlQpsDTO>> getNodeUrlQps(String nodeUrl);

    /**
     * 获取节点的 URL 映射列表（通过 actuator 获取）
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return URL 映射列表
     */
    ReturnResult<List<NodeUrlMappingDTO>> getNodeUrlMappings(String nodeUrl);

    /**
     * 获取节点的 QPS 汇总统计
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 汇总统计
     */
    ReturnResult<Map<String, Object>> getNodeQpsSummary(String nodeUrl);

    /**
     * 清除节点的 QPS 统计数据
     *
     * @param nodeUrl 节点URL（Base64编码）
     */
    void clearNodeQpsData(String nodeUrl);
}
