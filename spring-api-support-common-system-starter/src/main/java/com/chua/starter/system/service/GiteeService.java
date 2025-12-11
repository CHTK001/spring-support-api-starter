package com.chua.starter.system.service;

import com.chua.starter.oauth.client.support.user.UserResume;
import me.zhyd.oauth.model.AuthUser;

/**
 * Gitee第三方登录服务
 * <p>处理Gitee账号与系统账号的绑定</p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/29
 */
public interface GiteeService {

    /**
     * 绑定Gitee账号
     * <p>将Gitee认证用户信息与系统用户进行绑定</p>
     *
     * @param data       Gitee认证用户信息
     * @param userResume 系统用户信息
     */
    void binder(AuthUser data, UserResume userResume);

}
