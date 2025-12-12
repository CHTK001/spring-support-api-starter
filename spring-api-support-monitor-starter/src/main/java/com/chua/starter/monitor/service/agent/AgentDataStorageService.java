package com.chua.starter.monitor.service.agent;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.agent.AgentFileHandleDTO;
import com.chua.starter.monitor.pojo.agent.AgentServiceInstanceDTO;
import com.chua.starter.monitor.pojo.agent.AgentTraceDTO;

import java.util.List;
import java.util.Map;

/**
 * Agent 数据存储服务接口
 * <p>
 * 处理 Agent 上报的链路追踪、服务实例、文件句柄等数据的存储和查询
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
public interface AgentDataStorageService {

    // ==================== 链路追踪 ====================

    /**
     * 存储链路追踪数据到 RediSearch
     *
     * @param sessionId 会话ID
     * @param traceData 链路追踪数据
     * @return 存储结果
     */
    ReturnResult<Boolean> storeTraceForAgent(String sessionId, Map<String, Object> traceData);

    /**
     * 查询历史链路追踪
     *
     * @param keyword   关键词（支持 linkId、method、address 等）
     * @param startTime 开始时间（毫秒时间戳）
     * @param endTime   结束时间（毫秒时间戳）
     * @param offset    偏移量
     * @param limit     限制数量
     * @return 链路追踪列表
     */
    ReturnResult<List<AgentTraceDTO>> searchTracesForAgent(String keyword, Long startTime, Long endTime, 
                                                           int offset, int limit);

    /**
     * 根据链路ID查询完整链路
     *
     * @param linkId 链路ID
     * @return 完整链路信息
     */
    ReturnResult<AgentTraceDTO> getTraceByLinkIdForAgent(String linkId);

    // ==================== 服务实例（关系图谱）====================

    /**
     * 存储服务实例数据到 Redis
     *
     * @param sessionId    会话ID
     * @param instanceData 服务实例数据
     * @return 存储结果
     */
    ReturnResult<Boolean> storeServiceInstanceForAgent(String sessionId, Map<String, Object> instanceData);

    /**
     * 获取服务关系图谱数据
     *
     * @return 关系图谱数据（节点和边）
     */
    ReturnResult<Map<String, Object>> getServiceTopologyForAgent();

    /**
     * 获取指定服务的调用关系
     *
     * @param serviceName 服务名称
     * @return 服务调用关系
     */
    ReturnResult<List<AgentServiceInstanceDTO>> getServiceRelationsForAgent(String serviceName);

    // ==================== 文件句柄 ====================

    /**
     * 存储文件句柄数据到 Redis
     *
     * @param sessionId  会话ID
     * @param handleData 文件句柄数据
     * @return 存储结果
     */
    ReturnResult<Boolean> storeFileHandleForAgent(String sessionId, Map<String, Object> handleData);

    /**
     * 获取指定节点的文件句柄列表
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 文件句柄列表
     */
    ReturnResult<List<AgentFileHandleDTO>> getFileHandlesForAgent(String ipAddress, Integer port);

    /**
     * 获取所有节点的文件句柄统计
     *
     * @return 文件句柄统计
     */
    ReturnResult<Map<String, Object>> getFileHandleStatsForAgent();
}
