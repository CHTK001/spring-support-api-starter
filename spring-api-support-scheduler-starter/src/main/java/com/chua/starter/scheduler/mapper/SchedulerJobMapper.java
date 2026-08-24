package com.chua.starter.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 调度任务 Mapper 接口
 *
 * @version 1.0.0
 * @since 2025/1/1
 * @author CH
 */
@Mapper
public interface SchedulerJobMapper {

    /**
     * 按任务分组查询未删除的任务编号列表
     *
     * @param group 任务分组
     * @return 任务编号列表
     */
    @Select("SELECT sys_job_no FROM sys_job WHERE sys_job_group = #{group} AND sys_job_is_deleted = 0")
    List<String> selectJobNosByGroup(@Param("group") String group);
}
