package com.chua.starter.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.entity.project.SysProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceMerchantMapper extends BaseMapper<SysProject> {

    IPage<SysProject> selectPageFor(
            @Param("page") Page<SysProject> page,
            @Param("request") SysProject sysProject);

    default SysProject getOne(Integer sysProjectId) {
        SysProject sysProject = new SysProject();
        sysProject.setSysProjectId(sysProjectId);
        IPage<SysProject> page = selectPageFor(new Page<>(1, 1), sysProject);
        if (page.getTotal() > 0) {
            return page.getRecords().get(0);
        }
        return null;
    }
}
