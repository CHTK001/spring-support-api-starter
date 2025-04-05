package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MaintenanceGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 维护组Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MaintenanceGroupMapper extends BaseMapper<MaintenanceGroup> {

    /**
     * 获取维护组列表
     *
     * @param enabled 是否启用
     * @return 维护组列表
     */
    List<MaintenanceGroup> selectMasterList(@Param("enabled") Boolean enabled);

    /**
     * 根据ID获取维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护组
     */
    MaintenanceGroup selectMasterById(@Param("maintenanceGroupId") Long maintenanceGroupId);
}