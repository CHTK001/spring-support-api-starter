package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MaintenanceHost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 维护主机Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MaintenanceHostMapper extends BaseMapper<MaintenanceHost> {

    /**
     * 根据维护组ID获取维护主机列表
     *
     * @param groupId 维护组ID
     * @param enabled 是否启用
     * @return 维护主机列表
     */
    List<MaintenanceHost> selectHostsByGroupId(@Param("groupId") Long groupId, @Param("enabled") Boolean enabled);

    /**
     * 根据ID获取维护主机
     *
     * @param maintenanceHostId 维护主机ID
     * @return 维护主机
     */
    MaintenanceHost selectHostById(@Param("maintenanceHostId") Long maintenanceHostId);
}