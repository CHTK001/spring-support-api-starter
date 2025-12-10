package com.chua.starter.monitor.service.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import com.chua.common.support.lang.code.ReturnResult;

import java.util.List;
import java.util.Map;

/**
 * 服务器组件布局配置服务接口
 *
 * @author CH
 * @since 2025/01/06
 */
public interface MonitorSysGenServerComponentLayoutService extends IService<MonitorSysGenServerComponentLayout> {

    /**
     * 根据服务器ID获取组件布局列表
     *
     * @param serverId 服务器ID
     * @return 组件布局列表
     */
    ReturnResult<List<MonitorSysGenServerComponentLayout>> getLayoutsByServerId(Integer serverId);

    /**
     * 根据服务器ID和组件ID获取布局配置
     *
     * @param serverId 服务器ID
     * @param componentId 组件ID
     * @return 布局配置
     */
    ReturnResult<MonitorSysGenServerComponentLayout> getLayoutByServerIdAndComponentId(Integer serverId, Long componentId);

    /**
     * 根据服务器ID获取启用的组件布局列表
     *
     * @param serverId 服务器ID
     * @return 启用的组件布局列表
     */
    ReturnResult<List<MonitorSysGenServerComponentLayout>> getEnabledLayoutsByServerId(Integer serverId);

    /**
     * 保存组件布局配置
     *
     * @param layout 布局配置
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerComponentLayout> saveLayout(MonitorSysGenServerComponentLayout layout);

    /**
     * 更新组件布局配置
     *
     * @param layout 布局配置
     * @return 操作结果
     */
    ReturnResult<Boolean> updateLayout(MonitorSysGenServerComponentLayout layout);

    /**
     * 删除组件布局配置
     *
     * @param layoutId 布局ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteLayout(Long layoutId);

    /**
     * 根据组件ID删除布局配置
     *
     * @param componentId 组件ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteLayoutByComponentId(Long componentId);

    /**
     * 根据服务器ID删除所有布局配置
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteLayoutsByServerId(Integer serverId);

    /**
     * 批量更新组件布局位置信息
     *
     * @param serverId
     * @param layouts  布局列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchUpdatePosition(Integer serverId, List<MonitorSysGenServerComponentLayout> layouts);

    /**
     * 创建组件布局配置
     *
     * @param serverId 服务器ID
     * @param componentId 组件ID
     * @param x X坐标
     * @param y Y坐标
     * @param w 宽度
     * @param h 高度
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerComponentLayout> createLayout(Integer serverId, Long componentId, 
                                                                  Integer x, Integer y, Integer w, Integer h);

    /**
     * 更新组件布局位置
     *
     * @param layoutId 布局ID
     * @param x X坐标
     * @param y Y坐标
     * @param w 宽度
     * @param h 高度
     * @return 操作结果
     */
    ReturnResult<Boolean> updateLayoutPosition(Long layoutId, Integer x, Integer y, Integer w, Integer h);

    /**
     * 启用/禁用组件布局
     *
     * @param layoutId 布局ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> setLayoutEnabled(Long layoutId, Boolean enabled);

    /**
     * 获取服务器组件布局统计信息
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getLayoutStatistics(Integer serverId);

    /**
     * 复制布局配置到其他服务器
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerId 目标服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> copyLayoutsToServer(Integer sourceServerId, Integer targetServerId);

    /**
     * 重置服务器布局配置
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> resetServerLayouts(Integer serverId);
}
