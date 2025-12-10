package com.chua.starter.monitor.service.app;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.app.ApiFeatureVO;

import java.util.List;
import java.util.Map;

/**
 * API 功能开关管理服务接口
 * <p>
 * 用于管理客户端节点的 API 功能开关。
 * </p>
 *
 * @author CH
 * @since 2024/12/08
 * @version 1.0.0
 */
public interface ApiFeatureManagementService {

    /**
     * 获取节点的功能开关列表
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 功能开关列表
     */
    ReturnResult<List<ApiFeatureVO>> getNodeFeatures(String ipAddress, Integer port);

    /**
     * 获取节点功能开关统计
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getNodeFeatureStats(String ipAddress, Integer port);

    /**
     * 设置节点功能开关状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param featureId 功能标识
     * @param enabled   是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> setNodeFeatureEnabled(String ipAddress, Integer port, String featureId, boolean enabled);

    /**
     * 批量设置节点功能开关状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @param states    状态映射
     * @return 操作结果
     */
    ReturnResult<Boolean> setNodeFeatureEnabledBatch(String ipAddress, Integer port, Map<String, Boolean> states);

    /**
     * 重置节点所有功能开关到默认状态
     *
     * @param ipAddress 节点IP地址
     * @param port      节点端口
     * @return 操作结果
     */
    ReturnResult<Boolean> resetNodeAllFeatures(String ipAddress, Integer port);
}
