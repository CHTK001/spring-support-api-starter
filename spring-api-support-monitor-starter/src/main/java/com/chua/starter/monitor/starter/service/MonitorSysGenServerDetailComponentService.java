package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerDetailComponent;
import com.chua.common.support.lang.code.ReturnResult;

import java.util.List;
import java.util.Map;

/**
 * 服务器详情页组件配置服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface MonitorSysGenServerDetailComponentService extends IService<MonitorSysGenServerDetailComponent> {

    /**
     * 根据服务器ID获取组件列表
     *
     * @param serverId 服务器ID
     * @return 组件列表
     */
    ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerId(Integer serverId);

    /**
     * 根据服务器ID和组件类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param componentType 组件类型
     * @return 组件列表
     */
    ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerIdAndType(Integer serverId, String componentType);

    /**
     * 根据服务器ID和表达式类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param expressionType 表达式类型
     * @return 组件列表
     */
    ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerIdAndExpressionType(Integer serverId, String expressionType);

    /**
     * 获取启用的组件列表
     *
     * @param serverId 服务器ID
     * @return 启用的组件列表
     */
    ReturnResult<List<MonitorSysGenServerDetailComponent>> getEnabledComponentsByServerId(Integer serverId);

    /**
     * 保存或更新组件
     *
     * @param component 组件信息
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerDetailComponent> saveOrUpdateComponent(MonitorSysGenServerDetailComponent component);

    /**
     * 批量更新组件位置
     *
     * @param components 组件列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchUpdateComponentPosition(List<MonitorSysGenServerDetailComponent> components);

    /**
     * 删除组件
     *
     * @param componentId 组件ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteComponent(Long componentId);

    /**
     * 根据服务器ID删除所有组件
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteComponentsByServerId(Integer serverId);

    /**
     * 获取组件模板列表
     *
     * @return 模板列表
     */
    ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentTemplates();

    /**
     * 为服务器初始化默认组件
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> initDefaultComponentsForServer(Integer serverId);

    /**
     * 复制模板组件到指定服务器
     *
     * @param serverId 目标服务器ID
     * @param templateIds 模板ID列表
     * @return 操作结果
     */
    ReturnResult<Boolean> copyTemplatesForServer(Integer serverId, List<Long> templateIds);

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
    ReturnResult<MonitorSysGenServerDetailComponent> cloneComponent(Long sourceComponentId, Integer targetServerId);

    /**
     * 启用/禁用组件
     *
     * @param componentId 组件ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> toggleComponentEnabled(Long componentId, Boolean enabled);
}
