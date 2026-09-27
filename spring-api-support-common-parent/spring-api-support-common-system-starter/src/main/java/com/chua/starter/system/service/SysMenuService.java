package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.pojo.menu.SysMenuSettingVO;
import com.chua.starter.system.pojo.menu.SysMenuVO;

import java.util.List;
import java.util.Set;

/**
 * 系统菜单服务接口，定义了与系统菜单相关的业务操作
 * 继承了通用的服务接口i服务<SysMenu>，提供了更具体的菜单操作方法
 *
 * @since 2024/8/7
 * @author CH
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据条件查询系统菜单列表
     *
     * @param sysMenu 作为查询条件的系统菜单对象，可以指定部分属性进行模糊查询
     * @param roles   当前用户拥有的角色集合，用于权限过滤
     * @param ignoreType 忽略的类型
     * @param username 当前登录账号；内置 admin 账号拥有全部菜单，其余账号按角色授权
     * @return 返回匹配的系统菜单列表
     */
    ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles, int ignoreType, String username);

    /**
     * 查询所有系统菜单的缓存列表
     * 该方法返回的结果会被缓存，使用了@Cacheable注解进行缓存管理
     *
     * @return 返回所有系统菜单的列表
     */
    List<SysMenu> listAll();

    /**
     * 分页查询系统菜单列表
     *
     * @param query 分页查询对象，设置了分页的大小和当前页码
     * @param sysMenu 作为查询条件的系统菜单对象，可以指定部分属性进行模糊查询
     * @param roles 当前用户拥有的角色集合，用于权限过滤
     * @return 返回分页查询结果，包含了分页信息和菜单列表
     */
    IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles);

    /**
     * 删除指定的系统菜单
     *
     * @param menuId 要删除的系统菜单的标识
     * @return 返回删除操作的结果，包括是否成功和相关消息
     */
    ReturnResult<Boolean> delete(Long menuId);

    /**
     * 更新系统菜单的信息
     *
     * @param roles 当前用户拥有的角色集合，用于权限校验
     * @param sysMenu 要更新的系统菜单对象，包含更新后的属性值
     * @return 返回更新操作的结果，包括是否成功和相关消息
     */
    ReturnResult<Boolean> update(Set<String> roles, SysMenu sysMenu);

    /**
     * 新增一个系统菜单
     *
     * @param sysMenu 新增的系统菜单对象，包含所有必要属性
     * @param roles 当前用户拥有的角色集合，用于权限校验
     * @return 返回新增操作的结果，包括是否成功和相关消息
     */
    ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles);

    /**
     * 根据条件查询系统菜单列表
     *
     * @param sysMenu 作为查询条件的系统菜单对象，可以指定部分属性进行模糊查询
     * @param roles 当前用户拥有的角色集合，用于权限过滤
     * @param username 当前登录账号，用于识别内置 admin（全量放行以账号为准，ADMIN 角色需按菜单授权配置）
     * @return 返回匹配的系统菜单列表
     */
    ReturnResult<List<SysMenuSettingVO>> listMenu(SysMenu sysMenu, Set<String> roles, String username);

    /**
     * 查询所有系统菜单的列表
     *
     * @param sysMenu 查询条件
     * @param roles 角色集合
     * @return 返回所有系统菜单的列表
     */
    ReturnResult<List<SysMenu>> all(SysMenu sysMenu, Set<String> roles);

    /**
     * 刷新菜单缓存
     *
     * @return 操作结果
     */
    ReturnResult<Boolean> refreshCache();

    /**
     * 批量保存按钮(权限)
     *
     * @param menus 按钮列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchSaveButtons(List<SysMenu> menus);

    /**
     * 批量删除按钮
     *
     * @param menuIds 菜单ID集合
     * @return 操作结果
     */
    ReturnResult<Boolean> batchDeleteButtons(List<Integer> menuIds);
}
