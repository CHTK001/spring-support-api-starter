package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerProxy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器代理关联Mapper接口
 *
 * @author CH
 * @since 2024/12/25
 */
@Mapper
public interface MonitorSysGenServerProxyMapper extends BaseMapper<MonitorSysGenServerProxy> {

    /**
     * 根据服务器ID查询代理关联列表
     *
     * @param serverId 服务器ID
     * @return 代理关联列表
     */
    List<MonitorSysGenServerProxy> selectByServerId(@Param("serverId") Integer serverId);



    /**
     * 根据服务器ID和代理类型查询代理关联
     *
     * @param serverId  服务器ID
     * @param proxyType 代理类型
     * @return 代理关联
     */
    MonitorSysGenServerProxy selectByServerIdAndProxyType(@Param("serverId") Integer serverId, 
                                                          @Param("proxyType") String proxyType);

    /**
     * 批量更新代理状态
     *
     * @param proxyIds 代理关联ID列表
     * @param status   状态
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("proxyIds") List<Integer> proxyIds, @Param("status") Integer status);

    /**
     * 根据服务器ID删除代理关联
     *
     * @param serverId 服务器ID
     * @return 删除数量
     */
    int deleteByServerId(@Param("serverId") Integer serverId);


}
