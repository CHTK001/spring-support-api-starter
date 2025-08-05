package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器配置设置Mapper接口
 *
 * @author CH
 * @since 2024/12/26
 */
@Mapper
public interface MonitorSysGenServerSettingMapper extends BaseMapper<MonitorSysGenServerSetting> {

    /**
     * 根据服务器ID获取配置
     *
     * @param serverId 服务器ID
     * @return 服务器配置
     */
    MonitorSysGenServerSetting selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据服务器ID列表批量获取配置
     *
     * @param serverIds 服务器ID列表
     * @return 服务器配置列表
     */
    List<MonitorSysGenServerSetting> selectByServerIds(@Param("serverIds") List<Integer> serverIds);

    /**
     * 获取启用监控的服务器配置
     *
     * @return 启用监控的服务器配置列表
     */
    List<MonitorSysGenServerSetting> selectMonitorEnabledSettings();

    /**
     * 获取启用告警的服务器配置
     *
     * @return 启用告警的服务器配置列表
     */
    List<MonitorSysGenServerSetting> selectAlertEnabledSettings();

    /**
     * 获取启用上报的服务器配置
     *
     * @param reportMethod 上报方式
     * @return 启用上报的服务器配置列表
     */
    List<MonitorSysGenServerSetting> selectReportEnabledSettings(@Param("reportMethod") String reportMethod);

    /**
     * 根据服务器ID删除配置
     *
     * @param serverId 服务器ID
     * @return 删除数量
     */
    int deleteByServerId(@Param("serverId") Integer serverId);

    /**
     * 批量插入或更新配置
     *
     * @param settings 配置列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(@Param("settings") List<MonitorSysGenServerSetting> settings);

    /**
     * 更新服务器配置状态
     *
     * @param serverId 服务器ID
     * @param status 状态
     * @return 更新数量
     */
    int updateStatusByServerId(@Param("serverId") Integer serverId, @Param("status") Integer status);

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    List<java.util.Map<String, Object>> getSettingsStatistics();
}
