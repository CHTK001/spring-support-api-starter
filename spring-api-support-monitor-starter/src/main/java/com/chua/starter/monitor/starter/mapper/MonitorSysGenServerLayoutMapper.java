package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLayout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器布局配置Mapper接口
 *
 * @author CH
 * @since 2025/01/03
 */
@Mapper
public interface MonitorSysGenServerLayoutMapper extends BaseMapper<MonitorSysGenServerLayout> {

    /**
     * 根据服务器ID获取布局列表
     *
     * @param serverId 服务器ID
     * @return 布局列表
     */
    List<MonitorSysGenServerLayout> selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据服务器ID获取默认布局
     *
     * @param serverId 服务器ID
     * @return 默认布局
     */
    MonitorSysGenServerLayout selectDefaultByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据服务器ID和布局类型获取布局
     *
     * @param serverId 服务器ID
     * @param layoutType 布局类型
     * @return 布局配置
     */
    MonitorSysGenServerLayout selectByServerIdAndType(@Param("serverId") Integer serverId, 
                                                     @Param("layoutType") String layoutType);

    /**
     * 清除服务器的默认布局标记
     *
     * @param serverId 服务器ID
     * @return 更新数量
     */
    int clearDefaultLayout(@Param("serverId") Integer serverId);

    /**
     * 设置默认布局
     *
     * @param layoutId 布局ID
     * @return 更新数量
     */
    int setDefaultLayout(@Param("layoutId") Integer layoutId);

    /**
     * 根据布局类型获取布局列表
     *
     * @param layoutType 布局类型
     * @return 布局列表
     */
    List<MonitorSysGenServerLayout> selectByLayoutType(@Param("layoutType") String layoutType);
}
