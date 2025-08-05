package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerComponentLayout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器组件布局配置Mapper接口
 *
 * @author CH
 * @since 2025/01/06
 */
@Mapper
public interface MonitorSysGenServerComponentLayoutMapper extends BaseMapper<MonitorSysGenServerComponentLayout> {

    /**
     * 根据服务器ID获取组件布局列表
     *
     * @param serverId 服务器ID
     * @return 组件布局列表
     */
    List<MonitorSysGenServerComponentLayout> selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据服务器ID和组件ID获取布局配置
     *
     * @param serverId 服务器ID
     * @param componentId 组件ID
     * @return 布局配置
     */
    MonitorSysGenServerComponentLayout selectByServerIdAndComponentId(@Param("serverId") Integer serverId, 
                                                                      @Param("componentId") Long componentId);

    /**
     * 根据服务器ID获取启用的组件布局列表
     *
     * @param serverId 服务器ID
     * @return 启用的组件布局列表
     */
    List<MonitorSysGenServerComponentLayout> selectEnabledByServerId(@Param("serverId") Integer serverId);

    /**
     * 批量更新组件布局位置信息
     *
     * @param layouts 布局列表
     * @return 更新数量
     */
    int batchUpdatePosition(@Param("layouts") List<MonitorSysGenServerComponentLayout> layouts);

    /**
     * 根据组件ID删除布局配置
     *
     * @param componentId 组件ID
     * @return 删除数量
     */
    int deleteByComponentId(@Param("componentId") Long componentId);

    /**
     * 根据服务器ID删除所有布局配置
     *
     * @param serverId 服务器ID
     * @return 删除数量
     */
    int deleteByServerId(@Param("serverId") Integer serverId);

    /**
     * 获取服务器组件布局统计信息
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    List<java.util.Map<String, Object>> getLayoutStatistics(@Param("serverId") Integer serverId);
}
