package com.chua.starter.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 调度任务 映射器 接口
*
* @版本 1.0.0
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
    List<String> selectJobNosByGroup(@Param("group") String group);
}
