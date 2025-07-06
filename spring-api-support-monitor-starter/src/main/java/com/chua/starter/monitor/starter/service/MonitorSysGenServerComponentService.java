package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerComponent;

import java.util.List;
import java.util.Map;

/**
 * 服务器组件配置服务接口
 *
 * @author CH
 * @since 2025/01/03
 */
public interface MonitorSysGenServerComponentService extends IService<MonitorSysGenServerComponent> {

    /**
     * 根据服务器ID获取组件列表
     *
     * @param serverId 服务器ID
     * @return 组件列表
     */
    ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerId(Integer serverId);

    /**
     * 根据组件ID获取组件信息
     *
     * @param componentId 组件ID
     * @return 组件信息
     */
    ReturnResult<MonitorSysGenServerComponent> getComponentById(Long componentId);

    /**
     * 更新组件信息
     *
     * @param component 组件信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateComponent(MonitorSysGenServerComponent component);

    /**
     * 根据服务器ID和组件类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param componentType 组件类型
     * @return 组件列表
     */
    ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerIdAndType(Integer serverId, String componentType);

    /**
     * 根据服务器ID和表达式类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param expressionType 表达式类型
     * @return 组件列表
     */
    ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerIdAndExpressionType(Integer serverId, String expressionType);

    /**
     * 获取启用的组件列表
     *
     * @param serverId 服务器ID
     * @return 启用的组件列表
     */
    ReturnResult<List<MonitorSysGenServerComponent>> getEnabledComponentsByServerId(Integer serverId);

    /**
     * 批量更新组件位置信息
     *
     * @param components 组件列表
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdatePosition(List<MonitorSysGenServerComponent> components);

    /**
     * 为服务器初始化默认组件
     *
     * @param serverId 服务器ID
     * @return 初始化结果
     */
    ReturnResult<Boolean> initDefaultComponentsForServer(Integer serverId);

    /**
     * 获取服务器组件统计信息
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getComponentStatistics(Integer serverId);

    /**
     * 执行组件查询
     *
     * @param componentId 组件ID
     * @param timeRange 时间范围参数
     * @return 查询结果
     */
    ReturnResult<Object> executeComponentQuery(Long componentId, Map<String, Object> timeRange);

    /**
     * 验证组件表达式
     *
     * @param expressionType 表达式类型
     * @param expression 表达式内容
     * @param serverId 服务器ID
     * @return 验证结果
     */
    ReturnResult<Boolean> validateComponentExpression(String expressionType, String expression, Integer serverId);

    /**
     * 克隆组件
     *
     * @param sourceComponentId 源组件ID
     * @param targetServerId 目标服务器ID
     * @return 克隆结果
     */
    ReturnResult<MonitorSysGenServerComponent> cloneComponent(Long sourceComponentId, Integer targetServerId);

    /**
     * 删除服务器的所有组件
     *
     * @param serverId 服务器ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteComponentsByServerId(Integer serverId);

    /**
     * 启用/禁用组件
     *
     * @param componentId 组件ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> toggleComponentEnabled(Long componentId, Boolean enabled);

    /**
     * 更新组件排序
     *
     * @param componentId 组件ID
     * @param sortOrder 排序序号
     * @return 更新结果
     */
    ReturnResult<Boolean> updateComponentSortOrder(Long componentId, Integer sortOrder);

    /**
     * 查询组件数据（支持时间范围）
     *
     * @param componentId 组件ID
     * @param startTime   开始时间（时间戳，秒）
     * @param endTime     结束时间（时间戳，秒）
     * @param step        步长（秒）
     * @return 组件数据
     */
    ReturnResult<Object> getComponentData(Long componentId, Long startTime, Long endTime, Integer step);

    /**
     * 批量查询组件数据
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间（时间戳，秒）
     * @param endTime   结束时间（时间戳，秒）
     * @param step      步长（秒）
     * @return 组件数据集合
     */
    ReturnResult<Map<String, Object>> getBatchComponentData(Integer serverId, Long startTime, Long endTime, Integer step);

    /**
     * 获取组件实时数据
     *
     * @param componentId 组件ID
     * @return 实时数据
     */
    ReturnResult<Object> getComponentRealtimeData(Long componentId);

    /**
     * 根据表达式类型查询时序数据（COMPONENT类型）
     *
     * @param componentId 组件ID
     * @param indicatorName 指标名称
     * @param startTime 开始时间（时间戳，秒）
     * @param endTime 结束时间（时间戳，秒）
     * @param step 步长（秒）
     * @return 时序数据
     */
    ReturnResult<Object> queryTimeSeriesData(Long componentId, String indicatorName, Long startTime, Long endTime, Integer step);

    /**
     * 根据表达式类型查询Prometheus数据（PROMETHEUS类型）
     *
     * @param componentId 组件ID
     * @param promQL Prometheus查询表达式
     * @param startTime 开始时间（时间戳，秒）
     * @param endTime 结束时间（时间戳，秒）
     * @param step 步长（秒）
     * @return Prometheus查询结果
     */
    ReturnResult<Object> queryPrometheusData(Long componentId, String promQL, Long startTime, Long endTime, Integer step);


}
