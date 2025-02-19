package com.chua.starter.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysEmailTemplate;
import com.chua.starter.system.pojo.email.EmailPageV1Request;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 邮件模板
 *
 * @author CH
 */
@Mapper
public interface SysEmailTemplateMapper extends BaseMapper<SysEmailTemplate> {
    /**
     * 分页
     *
     * @param page 分页
     * @param request 请求
     * @return 分页
     */
    IPage<SysEmailTemplate> pageFor(@Param("page")Page<SysEmailTemplate> page,
                                  @Param("request") EmailPageV1Request request);
}