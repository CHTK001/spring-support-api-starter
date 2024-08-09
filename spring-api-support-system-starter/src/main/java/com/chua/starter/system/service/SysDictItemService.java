package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDictItem;

import java.util.Set;

/**
 * @author CH
 * @since 2024/8/7
 */
// 定义系统字典项服务接口
public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 分页查询字典项
     *
     * @param query  查询条件对象，用于封装分页及排序等信息
     * @param sysDictItem  字典项实体对象，用于过滤查询结果
     * @param roles  用户角色集合，用于进行权限控制
     * @return 分页查询结果，包含字典项列表和分页信息
     */
    IPage<SysDictItem> pageForDictItem(Query<SysDictItem> query, SysDictItem sysDictItem, Set<String> roles);

    /**
     * 删除指定的字典项
     *
     * @param dictItemId  字典项的唯一标识
     * @return 删除操作的结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> delete(Long dictItemId);

    /**
     * 更新字典项信息
     *
     * @param roles  用户角色集合，用于进行权限控制
     * @param sysDictItem  待更新的字典项实体对象
     * @return 更新操作的结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> update(Set<String> roles, SysDictItem sysDictItem);

    /**
     * 新增字典项
     *
     * @param sysDictItem  待新增的字典项实体对象
     * @param roles  用户角色集合，用于进行权限控制
     * @return 新增操作的结果，包含成功与否的标志和消息，以及新增的字典项信息
     */
    ReturnResult<SysDictItem> save(SysDictItem sysDictItem, Set<String> roles);
}
