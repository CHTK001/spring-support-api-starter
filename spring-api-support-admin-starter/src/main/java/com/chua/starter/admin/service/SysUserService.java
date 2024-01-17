package com.chua.starter.admin.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.pojo.UserInfoVO;
import com.chua.starter.admin.query.SysUserUpdate;
import com.chua.starter.admin.query.UserPageQuery;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * 用户业务接口
 *
 * @author haoxr
 * @since 2022/1/14
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户分页列表
     *
     * @param username    用户名
     * @param queryParams 请求参数
     * @return 用户分页列表
     */
    IPage<SysUser> getUserPage(UserPageQuery queryParams, String username);


    /**
     * 获取token用户信息
     *
     * @param token token
     * @return 获取token用户信息
     */
    UserInfoVO getUserLoginInfo(String token);

    /**
     * 获取登录用户信息
     *
     * @return 获取登录用户信息
     */
    UserInfoVO getUserLoginInfo();

    /**
     * 是否是管理员
     *
     * @return 是否是管理员
     */
    boolean isAdmin();

    /**
     * 更新
     *
     * @param roles1   角色1
     * @param username 用户名
     * @param sysUser  sys用户
     * @return {@link ReturnResult}<{@link SysUser}>
     */
    ReturnResult<SysUser> updateUser(Set<String> roles1, String username, SysUserUpdate sysUser);

    /**
     * 保存使用者
     *
     * @param sysUser sys用户
     * @param request
     * @return {@link ReturnResult}<{@link SysUser}>
     */
    ReturnResult<SysUser> saveUser(SysUser sysUser, HttpServletRequest request);
}
