package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDict;

import java.util.Set;

/**
 * 系统字典服务接口，继承自通用服务接口IService
 * 本接口提供系统字典相关的业务操作方法
 * 包括分页查询、删除、更新和保存系统字典信息
 *
 * @author CH
 * @since 2024/8/7
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 分页查询系统字典信息
     * 根据查询条件和用户角色进行筛选
     *
     * @param query  查询对象，用于指定分页信息和查询条件
     * @param sysDict 系统字典对象，作为查询条件
     * @param roles  用户角色集合，用于权限控制
     * @return 分页查询结果，包含查询到的系统字典信息
     */
    IPage<SysDict> pageForDict(Query<SysDict> query, SysDict sysDict, Set<String> roles);

    /**
     * 删除指定的系统字典
     *
     * @param dictId 字典ID，唯一标识要删除的字典
     * @return 删除操作的结果，包含成功与否的标志
     */
    ReturnResult<Boolean> delete(Long dictId);

    /**
     * 更新系统字典信息
     *
     * @param roles 用户角色集合，用于权限控制
     * @param sysDict 待更新的系统字典对象
     * @return 更新操作的结果，包含成功与否的标志
     */
    ReturnResult<Boolean> update(Set<String> roles, SysDict sysDict);

    /**
     * 保存新的系统字典信息
     *
     * @param sysDict 待保存的系统字典对象
     * @param roles 用户角色集合，用于权限控制
     * @return 保存操作的结果，包含成功与否的标志以及保存的字典信息
     */
    ReturnResult<SysDict> save(SysDict sysDict, Set<String> roles);
}
