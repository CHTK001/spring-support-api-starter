package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDictItem;

import java.util.List;
import java.util.Set;

/**
 * 定义系统字典项服务接口
 * @author CH
 * @since 2024/8/7
 */
public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 清除缓存
     *
     * @param sysDictItem  待清除缓存的字典项实体对象
     */
    void deleteCacheId(SysDictItem sysDictItem);
    /**
     * 清除缓存
     *
     * @param sysDictItem  待清除缓存的字典项实体对象
     */
    void deleteCacheCode(SysDictItem sysDictItem);

    /**
     * 清除缓存
     *
     * @param sysDictItem  待清除缓存的字典项实体对象
     */
    void deleteCachePid(SysDictItem sysDictItem);
    /**
     * 清除缓存
     *
     * @param sysDictItem  待清除缓存的字典项实体对象
     */
    void deleteCachePcode(SysDictItem sysDictItem);
    /**
     * 分页查询字典项
     *
     * @param query  查询条件对象，用于封装分页及排序等信息
     * @param sysDictId  字典项实体对象，用于过滤查询结果
     * @param roles  用户角色集合，用于进行权限控制
     * @return 分页查询结果，包含字典项列表和分页信息
     */
    IPage<SysDictItem> pageForItem(Query<SysDictItem> query, Integer sysDictId, Set<String> roles);

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

    /**
     * 查询字典项列表
     *
     * @param query  查询条件对象，用于封装分页及排序等信息
     * @param sysDictId  字典项实体对象，用于过滤查询结果
     * @param roles  用户角色集合，用于进行权限控制
     * @return 字典项列表
     */
    List<SysDictItem> listForItemId(Query<SysDictItem> query, Integer sysDictId, Set<String> roles);

    /**
     * 查询字典项列表
     *
     * @param query  查询条件对象，用于封装分页及排序等信息
     * @param sysDictCode  字典项实体对象，用于过滤查询结果
     * @param roles  用户角色集合，用于进行权限控制
     * @return 字典项列表
     */
    List<SysDictItem> listForItemCode(Query<SysDictItem> query, String sysDictCode, Set<String> roles);

    /**
     * 查询字典项列表
     *
     * @param query  查询条件对象，用于封装分页及排序等信息
     * @param sysDictCode  字典项实体对象，用于过滤查询结果
     * @param roles  用户角色集合，用于进行权限控制
     * @return 字典项列表
     */
    List<SysDictItem> listForItemPcode(Query<SysDictItem> query, String sysDictCode, Set<String> roles);

    /**
     * 根据字典项的父ID查询字典项列表，并进行权限控制
     * 此方法用于根据特定字典的父字典项ID检索字典项，同时考虑当前用户的角色来进行数据过滤，
     * 确保用户只能看到其有权限访问的字典项数据
     *
     * @param query 查询条件对象，用于指定查询条件
     * @param sysDictId 字典项的父ID，用于确定属于哪个字典的字典项
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 返回经过权限过滤的字典项列表如果用户对某些字典项没有访问权限，
     *         这些字典项将不会包含在返回的列表中
     */
    List<SysDictItem> listForItemPid(Query<SysDictItem> query, Integer sysDictId, Set<String> roles);

    /**
     * 根据字典项的父编码查询字典项列表，并进行权限控制
     * 此方法用于根据特定字典的父字典项编码检索字典项，同时考虑当前用户的角色来进行数据过滤，
     * 确保用户只能看到其有权限访问的字典项数据
     *
     * @param sysDictCode 字典项的父编码，用于确定属于哪个字典的字典项
     * @param sysDictId 当前用户的角色集合，用于权限控制
     * @return 返回经过权限过滤的字典项列表如果用户对某些字典项没有访问权限
     */
    SysDictItem getTypeIdByName(String sysDictCode, int sysDictId);
}
