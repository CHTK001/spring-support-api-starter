package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysTemplateGroup;

import java.util.Set;
/**
 * 系统模板组服务接口
 * 该接口提供了对系统模板组的CRUD操作，并考虑了权限相关功能
 * 通过继承IService<SysTemplateGroup>接口，实现了基础的服务功能
 *
 * @author CH
 * @since 2024/9/2
 */
public interface SysTemplateGroupService extends IService<SysTemplateGroup> {

    /**
     * 分页查询系统模板组
     * 根据查询条件和用户角色进行分页查询，返回匹配的系统模板组
     *
     * @param query 分页查询对象，包含分页信息和查询条件
     * @param sysTemplateGroup 系统模板组对象，作为查询条件
     * @param roles 用户角色集合，用于权限控制
     * @return 分页查询结果，包含符合条件的系统模板组列表和分页信息
     */
    IPage<SysTemplateGroup> page(Query<SysTemplateGroup> query, SysTemplateGroup sysTemplateGroup, Set<String> roles);

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
    ReturnResult<Boolean> update(Set<String> roles, SysTemplateGroup sysTemplateGroup);

    /**
     * 新增系统模板组
     * 新增一个系统模板组，并考虑权限控制
     *
     * @param sysTemplateGroup 新增的系统模板组对象，包含初始属性值
     * @param roles 用户角色集合，用于权限控制
     * @return 新增操作的结果，包含操作成功或失败的标志及提示信息，以及新增的系统模板组对象
     */
    ReturnResult<SysTemplateGroup> save(SysTemplateGroup sysTemplateGroup, Set<String> roles);
}
