package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目
 *
 * @author CH
 */
@Mapper
public interface SysProjectMapper extends BaseMapper<SysProject> {
    /**
     * 分页查询
     *
     * @param page       分页
     * @param sysProject 条件
     * @return 结果
     */
    IPage<SysProject> selectPageFor(
            @Param("page") Page<SysProject> page,
            @Param("request") SysProject sysProject);
}