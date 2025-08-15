package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 告警推送配置Mapper
 */
@Mapper
public interface MonitorSysGenAlertPushConfigMapper extends BaseMapper<MonitorSysGenAlertPushConfig> {

    /**
     * 按 serverId + 类型 查询启用的配置（若 serverId 为空或0，也包含全局配置）
     */
    List<MonitorSysGenAlertPushConfig> selectEnabledByServerAndType(@Param("serverId") Integer serverId,
                                                                    @Param("type") String typeCode);
}

