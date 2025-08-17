package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorArthasTunnelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Arthas Tunnel 配置 Mapper
 */
@Mapper
public interface MonitorArthasTunnelConfigMapper extends BaseMapper<MonitorArthasTunnelConfig> {

    /**
     * 根据服务器ID查询配置
     */
    MonitorArthasTunnelConfig selectByServerId(@Param("serverId") String serverId);
}
