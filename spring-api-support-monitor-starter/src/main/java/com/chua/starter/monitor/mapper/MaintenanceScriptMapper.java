package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MaintenanceScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 维护脚本Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MaintenanceScriptMapper extends BaseMapper<MaintenanceScript> {

    /**
     * 根据维护组ID获取脚本列表
     *
     * @param groupId 维护组ID
     * @param status  状态
     * @return 脚本列表
     */
    List<MaintenanceScript> selectScriptsByGroupId(@Param("groupId") Long groupId, @Param("status") Integer status);

    /**
     * 根据ID获取脚本
     *
     * @param maintenanceScriptId 脚本ID
     * @return 脚本
     */
    MaintenanceScript selectScriptById(@Param("maintenanceScriptId") Long maintenanceScriptId);
    
    /**
     * 根据维护组ID统计脚本数量
     *
     * @param groupId 维护组ID
     * @return 脚本数量
     */
    @Select("SELECT COUNT(*) FROM maintenance_script WHERE maintenance_group_id = #{groupId}")
    Integer countScriptsByGroupId(@Param("groupId") Long groupId);
}