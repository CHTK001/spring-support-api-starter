package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysTemplateCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @since 2024/9/5
 * @author CH    
 */
@Mapper
public interface SysTemplateCategoryMapper extends BaseMapper<SysTemplateCategory> {
    /**
     * 根据角色选择分页模板组
     *
     * @param page 分页对象，包含了分页的详细信息
     * @param sysTemplateGroup 系统模板组对象，用于查询条件
     * @param isAdmin 是否是管理员，影响查询逻辑
     * @return 返回分页查询结果，包含模板组的列表和分页信息
     *
     * 该方法用于根据用户的角色来分页查询对应的模板组信息
     * 当用户是管理员时，可能使用不同的查询逻辑
     * 该方法结合了分页查询和角色权限，以提供定制化的模板组数据查询能力
     */
    IPage<SysTemplateCategory> selectPageByRoles(@Param("page") Page<SysTemplateCategory> page,
                                              @Param("query")SysTemplateCategory sysTemplateGroup,
                                              @Param("isAdmin")boolean isAdmin);

    /**
     * 根据角色选择模板组列表
     *
     * @param sysTemplateGroup 系统模板组对象，用于查询条件
     * @param isAdmin 是否是管理员，影响查询逻辑
     * @return 返回模板组列表
     *
     * 该方法用于根据用户的角色来查询对应的模板组信息列表
     * 当用户是管理员时，可能使用不同的查询逻辑
     * 该方法结合了查询和角色权限，以提供定制化的模板组数据查询能力
     */
    List<SysTemplateCategory> selectListByRoles(@Param("page") Page<SysTemplateCategory> page,
                                                @Param("query")SysTemplateCategory sysTemplateGroup,
                                                @Param("isAdmin")boolean isAdmin);
}