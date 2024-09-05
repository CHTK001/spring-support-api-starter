package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @since 2024/9/2
 * @author CH    
 */
@Mapper
public interface SysTemplateMapper extends BaseMapper<SysTemplate> {

    /**
     * 分页查询
     * @param page 分页参数
     * @param sysTemplate 查询参数
     * @param isAdmin 是否是管理员
     * @return 分页结果
     */
    IPage<SysTemplate> selectPageForRoles(
            @Param("page") Page<SysTemplate> page,
            @Param("query") SysTemplate sysTemplate,
            @Param("isAdmin") boolean isAdmin);

    /**
     * 根据模板分组查询模板
     * @param sysTemplateCategoryCode 模板分组
     * @return 模板列表
     */
    List<SysTemplate> listForItemCode(@Param("sysTemplateCategoryCode") String sysTemplateCategoryCode);

    /**
     * 根据模板分组查询模板
     * @param sysTemplateCategoryId 类别ID
     * @return 模板列表
     */
    List<SysTemplate> listForItem(@Param("sysTemplateCategoryId")Integer sysTemplateCategoryId);
}