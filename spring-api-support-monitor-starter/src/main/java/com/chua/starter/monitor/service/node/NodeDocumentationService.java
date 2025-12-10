package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.NodeApiExecuteRequest;
import com.chua.starter.monitor.pojo.NodeApiGroup;

import java.util.List;
import java.util.Map;

/**
 * 节点文档服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface NodeDocumentationService {

    /**
     * 获取节点API文档
     *
     * @param nodeId      节点ID
     * @param nodeAddress 节点地址
     * @param contextPath 上下文路径
     * @return API文档列表
     */
    ReturnResult<List<NodeApiGroup>> getNodeApiDocs(String nodeId, String nodeAddress, String contextPath);

    /**
     * 获取节点Swagger资源
     *
     * @param nodeId      节点ID
     * @param nodeAddress 节点地址
     * @param contextPath 上下文路径
     * @return Swagger资源列表
     */
    ReturnResult<List<Map<String, Object>>> getNodeSwaggerResources(String nodeId, String nodeAddress,
                                                                    String contextPath);

    /**
     * 执行节点API
     *
     * @param request 执行请求
     * @return 执行结果
     */
    ReturnResult<Map<String, Object>> executeNodeApi(NodeApiExecuteRequest request);

    /**
     * 获取节点健康状态
     *
     * @param nodeId 节点ID
     * @return 健康状态
     */
    ReturnResult<Map<String, Object>> getNodeHealth(String nodeId);

    /**
     * 获取节点基本信息
     *
     * @param nodeId 节点ID
     * @return 节点信息
     */
    ReturnResult<Map<String, Object>> getNodeInfo(String nodeId);
}
