package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysTemplate;

import java.util.List;
import java.util.Set;

/**
 * @author CH
 * @since 2024/9/2
 */
// 系统模板服务接口
public interface SysTemplateService extends IService<SysTemplate> {

    /**
     * 分页查询系统模板
     *
     * @param query 分页查询对象，携带分页和排序信息
     * @param sysTemplate 系统模板条件对象，用于模糊查询
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 分页查询结果，包含系统模板列表和分页信息
     */
    IPage<SysTemplate> page(Query<SysTemplate> query, SysTemplate sysTemplate, Set<String> roles);

    /**
     * 删除系统模板
     *
     * @param sysTemplateId 模板的唯一标识符
     * @return 删除操作的结果，包含成功标志和消息
     */
    ReturnResult<Boolean> delete(Long sysTemplateId);

    /**
     * 更新系统模板信息
     *
     * @param roles 当前用户的角色集合，用于权限控制
     * @param sysTemplate 需要更新的系统模板对象
     * @return 更新操作的结果，包含成功标志和消息
     */
    ReturnResult<Boolean> update(Set<String> roles, SysTemplate sysTemplate);

    /**
     * 新增系统模板
     *
     * @param sysTemplate 新增的系统模板对象
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 新增操作的结果，包含成功标志和消息以及可能的新增模板对象
     */
    ReturnResult<SysTemplate> save(SysTemplate sysTemplate, Set<String> roles);

    /**
     * 删除缓存
     *
     * @param sysTemplateCategory 系统模板对象，用于删除缓存
     */
    void deleteCacheId(SysTemplateCategory sysTemplateCategory);

    /**
     * 删除缓存
     *
     * @param sysTemplateCategory 系统模板对象，用于删除缓存
     */
    void deleteCacheCode(SysTemplateCategory sysTemplateCategory);
    /**
     * 回滚系统模板
     *
     * @param roles 当前用户的角色集合，用于权限控制
     * @param sysTemplate 需要回滚的系统模板对象
     * @return 回滚操作的结果，包含成功标志和消息
     */
    ReturnResult<Boolean> rollback(Set<String> roles, SysTemplate sysTemplate);

    /**
     * 根据模板组代码获取模板列表
     *
     * @param query 分页查询对象，携带分页和排序信息
     * @param sysTemplateCategoryCode 模板组代码，用于查询模板列表
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 模板列表
     */
    List<SysTemplate> listForItemCode(Query<SysDictItem> query, String sysTemplateCategoryCode, Set<String> roles);

    /**
     * 根据模板组ID获取模板列表
     *
     * @param query 分页查询对象，携带分页和排序信息
     * @param sysTemplateGroupId 模板组ID，用于查询模板列表
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 模板列表
     */
    List<SysTemplate> listForItem(Query<SysDictItem> query, Integer sysTemplateGroupId, Set<String> roles);
}
