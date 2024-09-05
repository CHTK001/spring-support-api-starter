package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysTemplateCategory;
import com.chua.starter.system.pojo.category.SysTemplateCategoryTree;

import java.util.List;
import java.util.Set;

/**
 *
 * @since 2024/9/5
 * @author CH    
 */
public interface SysTemplateCategoryService extends IService<SysTemplateCategory>{

    /**
     * 分页查询系统模板组
     * 根据查询条件和用户角色进行分页查询，返回匹配的系统模板组
     *
     * @param query 分页查询对象，包含分页信息和查询条件
     * @param sysTemplateGroup 系统模板组对象，作为查询条件
     * @param roles 用户角色集合，用于权限控制
     * @return 分页查询结果，包含符合条件的系统模板组列表和分页信息
     */
    IPage<SysTemplateCategory> page(Query<SysTemplateCategory> query, SysTemplateCategory sysTemplateGroup, Set<String> roles);

    /**
     * 查询系统模板组列表
     * 根据查询条件和用户角色进行查询，返回匹配的系统模板组列表
     *
     * @param query 查询对象，包含查询条件
     * @param sysTemplateCategory 系统模板组对象，作为查询条件
     * @param roles 用户角色集合，用于权限控制
     * @return 匹配的系统模板组列表
     */
    List<SysTemplateCategory> list(Query<SysTemplateCategory> query, SysTemplateCategory sysTemplateCategory, Set<String> roles);

    /**
     * 删除系统模板组
     * 根据提供的系统模板ID删除对应的系统模板组
     *
     * @param sysTemplateId 要删除的系统模板组的ID
     * @return 删除操作的结果，包含操作成功或失败的标志及提示信息
     */
    ReturnResult<Boolean> delete(Long sysTemplateId);

    /**
     * 更新系统模板组
     * 更新系统模板组的信息，同时考虑权限控制
     *
     * @param roles 用户角色集合，用于权限控制
     * @param sysTemplateGroup 要更新的系统模板组对象，包含新的属性值
     * @return 更新操作的结果，包含操作成功或失败的标志及提示信息
     */
    ReturnResult<Boolean> update(Set<String> roles, SysTemplateCategory sysTemplateGroup);

    /**
     * 新增系统模板组
     * 新增一个系统模板组，并考虑权限控制
     *
     * @param sysTemplateGroup 新增的系统模板组对象，包含初始属性值
     * @param roles 用户角色集合，用于权限控制
     * @return 新增操作的结果，包含操作成功或失败的标志及提示信息，以及新增的系统模板组对象
     */
    ReturnResult<SysTemplateCategory> save(SysTemplateCategory sysTemplateGroup, Set<String> roles);

    /**
     * 回滚系统模板组
     * 根据提供的系统模板组对象进行回滚操作，并考虑权限控制
     *
     * @param sysTemplateGroup 要回滚的系统模板组对象
     * @param roles 用户角色集合，用于权限控制
     * @return 回滚操作的结果，包含操作成功或失败的标志及提示信息
     */
    ReturnResult<Boolean> rollback(Set<String> roles, SysTemplateCategory sysTemplateGroup);


    /**
     * 获取模板组树
     * 根据查询条件和用户角色，获取模板组树，用于展示模板组层级关系
     *
     * @param query 查询对象，包含查询条件
     * @param sysTemplateCategory 系统模板组对象，作为查询条件
     * @param roles 用户角色集合，用于权限控制
     * @return 模板组树列表，包含模板组节点和子节点信息
     */
    ReturnResult<List<SysTemplateCategoryTree>> tree(Query<SysTemplateCategory> query, SysTemplateCategory sysTemplateCategory, Set<String> roles);
}
