package com.chua.starter.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.workflow.entity.WfTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流任务 Mapper
 */
@Mapper
public interface WfTaskMapper extends BaseMapper<WfTask> {

    /**
     * 查询用户的待办列表
     */
    List<WfTask> selectTodoByUserId(@Param("userId") Integer userId);

    /**
     * 查询实例的所有任务
     */
    List<WfTask> selectByInstId(@Param("instId") Integer instId);

    /**
     * 更新任务状态(带状态检查)
     */
    int updateTaskStatus(@Param("taskId") Integer taskId,
                         @Param("oldStatus") String oldStatus,
                         @Param("newStatus") String newStatus,
                         @Param("comment") String comment);

    /**
     * 取消实例的所有待办任务
     */
    int cancelByInstId(@Param("instId") Integer instId,
                       @Param("oldStatus") String oldStatus,
                       @Param("newStatus") String newStatus);
}
