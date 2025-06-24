package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 服务器配置Mapper接口
 *
 * @author CH
 * @since 2024/12/18
 */
@Mapper
public interface MonitorSysGenServerMapper extends BaseMapper<MonitorSysGenServer> {

    /**
     * 根据协议类型统计服务器数量
     *
     * @return 协议统计
     */
    List<Map<String, Object>> countByProtocol();

    /**
     * 根据状态统计服务器数量
     *
     * @return 状态统计
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 根据标签统计服务器数量
     *
     * @return 标签统计
     */
    List<Map<String, Object>> countByTags();

    /**
     * 获取启用监控的服务器列表
     *
     * @return 服务器列表
     */
    List<MonitorSysGenServer> selectMonitoringEnabledServers();

    /**
     * 根据生成器ID获取服务器列表
     *
     * @param genId 生成器ID
     * @return 服务器列表
     */
    List<MonitorSysGenServer> selectByGenId(@Param("genId") Integer genId);

    /**
     * 根据协议类型获取服务器列表
     *
     * @param protocol 协议类型
     * @return 服务器列表
     */
    List<MonitorSysGenServer> selectByProtocol(@Param("protocol") String protocol);

    /**
     * 根据标签获取服务器列表
     *
     * @param tags 标签
     * @return 服务器列表
     */
    List<MonitorSysGenServer> selectByTags(@Param("tags") String tags);

    /**
     * 获取服务器概览统计
     *
     * @return 统计信息
     */
    Map<String, Object> getServerOverview();
}
