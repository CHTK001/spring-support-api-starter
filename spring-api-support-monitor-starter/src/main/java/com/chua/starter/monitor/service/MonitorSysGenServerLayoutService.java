package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLayout;

import java.util.List;
import java.util.Map;

/**
 * 服务器布局配置服务接口
 *
 * @author CH
 * @since 2025/01/03
 */
public interface MonitorSysGenServerLayoutService extends IService<MonitorSysGenServerLayout> {

    /**
     * 根据服务器ID获取布局列表
     *
     * @param serverId 服务器ID
     * @return 布局列表
     */
    ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByServerId(Integer serverId);

    /**
     * 获取服务器的默认布局
     *
     * @param serverId 服务器ID
     * @return 默认布局
     */
    ReturnResult<MonitorSysGenServerLayout> getDefaultLayout(Integer serverId);

    /**
     * 保存服务器布局配置
     *
     * @param layout 布局配置
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerLayout> saveLayout(MonitorSysGenServerLayout layout);

    /**
     * 更新服务器布局配置
     *
     * @param layout 布局配置
     * @return 操作结果
     */
    ReturnResult<Boolean> updateLayout(MonitorSysGenServerLayout layout);

    /**
     * 删除服务器布局配置
     *
     * @param layoutId 布局ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteLayout(Integer layoutId);

    /**
     * 设置默认布局
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 操作结果
     */
    ReturnResult<Boolean> setDefaultLayout(Integer serverId, Integer layoutId);

    /**
     * 根据组件配置生成布局
     *
     * @param serverId 服务器ID
     * @param layoutName 布局名称
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerLayout> generateLayoutFromComponents(Integer serverId, String layoutName);

    /**
     * 应用布局到服务器组件
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 操作结果
     */
    ReturnResult<Boolean> applyLayoutToComponents(Integer serverId, Integer layoutId);

    /**
     * 复制布局到其他服务器
     *
     * @param sourceLayoutId 源布局ID
     * @param targetServerId 目标服务器ID
     * @param layoutName 新布局名称
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerLayout> copyLayoutToServer(Integer sourceLayoutId, Integer targetServerId, String layoutName);

    /**
     * 获取布局详情
     *
     * @param layoutId 布局ID
     * @return 布局详情
     */
    ReturnResult<MonitorSysGenServerLayout> getLayoutById(Integer layoutId);

    /**
     * 根据布局类型获取布局列表
     *
     * @param layoutType 布局类型
     * @return 布局列表
     */
    ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByType(String layoutType);

    /**
     * 创建默认布局
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerLayout> createDefaultLayout(Integer serverId);

    /**
     * 保存单个组件的图表配置
     *
     * @param serverId 服务器ID
     * @param componentConfig 组件配置
     * @return 操作结果
     */
    ReturnResult<Boolean> saveComponentChartConfig(Integer serverId, Map<String, Object> componentConfig);

    /**
     * 获取组件的图表配置
     *
     * @param serverId 服务器ID
     * @param componentId 组件ID
     * @return 图表配置
     */
    ReturnResult<Map<String, Object>> getComponentChartConfig(Integer serverId, String componentId);

    /**
     * 批量保存组件图表配置
     *
     * @param serverId 服务器ID
     * @param configs 组件配置列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchSaveComponentChartConfig(Integer serverId, List<Map<String, Object>> configs);
}
