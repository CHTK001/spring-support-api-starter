package com.chua.starter.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.entity.project.SysAiVincentTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysAiVincentTaskMapper extends BaseMapper<SysAiVincentTask> {
    /**
     * 历史任务
     *
     * @param userId               用户ID
     * @param sysAiVincentTaskType 任务类型
     * @return
     */
    IPage<SysAiVincentTask> history(
            @Param("page") Page<SysAiVincentTask> objectPage,
            @Param("userId") Integer userId,
            @Param("sysAiVincentTaskType") String sysAiVincentTaskType);
}