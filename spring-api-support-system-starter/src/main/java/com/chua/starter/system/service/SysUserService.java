package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.query.SysUserQuery;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.result.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Set;

/**
 * 系统用户服务接口，定义了与系统用户相关的操作
 * 继承了通用的服务接口IService，为系统用户提供增删查改等操作
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户查询条件和角色集合分页查询用户信息
     *
     * @param sysUserQuery 用户查询条件对象，包含查询的各类参数
     * @param roles 用户角色集合，用于筛选用户
     * @return 分页查询结果，包含用户信息列表和分页数据
     */
    IPage<SysUser> pageForUser(SysUserQuery sysUserQuery, Set<String> roles);

    /**
     * 用户登录方法，根据登录数据进行用户身份验证
     *
     * @param loginData 登录数据对象，包含用户名、密码等信息
     * @param request HTTP请求对象，用于获取请求相关的数据
     * @param response HTTP响应对象，用于设置响应数据
     * @return 结果对象，包含登录结果和可能的附加信息
     */
    Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户的信息
     *
     * @return 用户信息对象，包含用户的基本信息
     */
    UserInfoVO getUserLoginInfo();

    /**
     * 删除指定用户
     *
     * @param userId 用户ID，用于确定要删除的用户
     * @return 操作结果对象，包含操作是否成功和相关提示信息
     */
    ReturnResult<Boolean> delete(Long userId);

    /**
     * 重置指定用户的业主信息
     *
     * @param userId 用户ID，用于确定要重置的用户
     * @return 操作结果对象，包含操作是否成功和相关提示信息
     */
    ReturnResult<SysUser> resetOwner(Long userId);

    /**
     * 重置指定用户的登录密码
     *
     * @param userId 用户ID，用于确定要重置密码的用户
     * @return 操作结果对象，包含操作是否成功和相关提示信息
     */
    ReturnResult<String> reset(Long userId);

    /**
     * 更新用户信息，包括角色等
     *
     * @param roles 用户的新角色集合
     * @param userId 用户ID，用于确定要更新的用户
     * @param sysUser 更新的用户信息对象
     * @return 操作结果对象，包含操作是否成功和相关提示信息
     */
    ReturnResult<SysUser> updateUser(Set<String> roles, Long userId, SysUserUpdate sysUser);

    /**
     * 保存新用户信息
     *
     * @param sysUserUpdate 新用户信息对象，包含用户的基本信息
     * @param request HTTP请求对象，用于获取请求相关的数据
     * @return 操作结果对象，包含操作是否成功和相关提示信息
     */
    ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request);

    /**
     * 获取当前登录用户的菜单列表
     *
     * @param roles 用户的角色集合，用于筛选菜单
     * @return 菜单列表对象，包含当前登录用户可以访问的菜单信息
     */
    List<SysMenuVO> getUserMenu(Set<String> roles);
}
