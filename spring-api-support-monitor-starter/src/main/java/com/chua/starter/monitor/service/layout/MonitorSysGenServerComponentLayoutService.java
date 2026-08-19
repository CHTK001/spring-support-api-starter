package com.chua.starter.monitor.service.layout;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;

import java.util.List;
import java.util.Map;

/**
 * 服务器组件布局服务接口，提供组件布局的查询、位置更新、批量保存、
 * 启用禁用、统计、复制与重置等能力。
 *
 * @since 2026-07-28
 * @author CH
 */
public interface MonitorSysGenServerComponentLayoutService extends IService<MonitorSysGenServerComponentLayout> {

    /**
     * 查询指定服务器的全部组件布局列表，按排序值排序。
     *
     * @param serverId 服务器ID
     * @return 组件布局列表
     */
    ReturnResult<List<MonitorSysGenServerComponentLayout>> listByServer(Integer serverId);

    /**
     * 查询指定服务器已启用的组件布局列表。
     *
     * @param serverId 服务器ID
     * @return 启用状态的组件布局列表
     */
    ReturnResult<List<MonitorSysGenServerComponentLayout>> listEnabledByServer(Integer serverId);

    /**
     * 查询指定服务器下指定组件的布局记录。
     *
     * @param serverId    服务器ID
     * @param componentId 组件ID
     * @return 组件布局记录，不存在时返回失败结果
     */
    ReturnResult<MonitorSysGenServerComponentLayout> getByComponent(Integer serverId, Integer componentId);

    /**
     * 保存组件布局配置，同一服务器同一组件已存在记录时执行更新。
     *
     * @param layout 组件布局实体
     * @return 保存后的组件布局
     */
    ReturnResult<MonitorSysGenServerComponentLayout> saveComponentLayout(MonitorSysGenServerComponentLayout layout);

    /**
     * 批量更新组件位置（x/y/w/h/zIndex）。
     *
     * @param layouts 组件布局集合，每项必须包含组件布局ID
     * @return 更新成功的数量
     */
    ReturnResult<Integer> batchUpdatePosition(List<MonitorSysGenServerComponentLayout> layouts);

    /**
     * 更新单个组件的位置与尺寸。
     *
     * @param layoutId 组件布局ID
     * @param layout   携带位置信息的实体
     * @return 是否更新成功
     */
    ReturnResult<Boolean> updatePosition(Integer layoutId, MonitorSysGenServerComponentLayout layout);

    /**
     * 启用或禁用组件布局。
     *
     * @param layoutId 组件布局ID
     * @param status   状态值，1-启用，0-禁用
     * @return 是否更新成功
     */
    ReturnResult<Boolean> updateStatus(Integer layoutId, Integer status);

    /**
     * 统计指定服务器的组件布局情况（总数、启用数、禁用数）。
     *
     * @param serverId 服务器ID
     * @return 统计结果 Map，包含 total/enabled/disabled 三个键
     */
    ReturnResult<Map<String, Object>> statistics(Integer serverId);

    /**
     * 复制源服务器的全部组件布局到目标服务器，目标服务器原有布局将先被清空。
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerId 目标服务器ID
     * @return 复制的组件布局数量
     */
    ReturnResult<Integer> copyToServer(Integer sourceServerId, Integer targetServerId);

    /**
     * 重置服务器的组件布局：删除全部现有布局记录，恢复为空白状态。
     *
     * @param serverId 服务器ID
     * @return 删除的记录数量
     */
    ReturnResult<Integer> resetServer(Integer serverId);

    /**
     * 按组件删除布局记录。
     *
     * @param componentId 组件ID
     * @return 是否删除成功
     */
    ReturnResult<Boolean> deleteByComponent(Integer componentId);

    /**
     * 按服务器删除全部组件布局记录。
     *
     * @param serverId 服务器ID
     * @return 是否删除成功
     */
    ReturnResult<Boolean> deleteByServer(Integer serverId);

    /**
     * 保存指定组件的图表配置 JSON，不存在布局记录时自动创建。
     *
     * @param serverId    服务器ID
     * @param componentId 组件ID
     * @param chartConfig 图表配置 JSON 字符串
     * @return 是否保存成功
     */
    ReturnResult<Boolean> saveChartConfig(Integer serverId, Integer componentId, String chartConfig);

    /**
     * 获取指定组件的图表配置 JSON。
     *
     * @param serverId    服务器ID
     * @param componentId 组件ID
     * @return 图表配置 JSON 字符串，未配置时返回 null
     */
    ReturnResult<String> getChartConfig(Integer serverId, Integer componentId);
}
