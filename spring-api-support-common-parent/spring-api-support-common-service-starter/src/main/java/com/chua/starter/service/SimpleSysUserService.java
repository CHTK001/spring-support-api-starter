package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysUser;
import jakarta.validation.constraints.NotNull;

/**
* 接口 简单sys用户服务 继承了 i服务，专门用于处理 sys用户 类的简单用户服务
*
* @since 2024/12/2
* @author CH
 */
public interface SimpleSysUserService extends IService<SysUser> {
    /**
    * 绑定用户标识到特定服务或功能
    *
    * @param sysUserId 用户标识，表示要绑定的用户
    * @return 返回操作结果，包含一个布尔值表示绑定是否成功
     */
    ReturnResult<Boolean> bind(Integer sysUserId);

    /**
    * 解绑用户标识从特定服务或功能
    *
    * @param sysUserId 用户标识，表示要解绑的用户
    * @return 返回操作结果，包含一个布尔值表示解绑是否成功
     */
    ReturnResult<Boolean> unbind(Integer sysUserId);

    /**
    * 获取用户当前 TOTP 状态
    *
    * @param sysUserId 用户标识
    * @return 状态结果
     */
    ReturnResult<Boolean> status(Integer sysUserId);

    /**
    * 生成一个返回结果，具体结构和内容由 返回主体结果 定义
    *
    * @return 返回一个 返回主体结果 对象，包含生成的结果信息
    * @param sysUserId sys用户ID，不允许为 null
     */
    ReturnResult<byte[]> generate(Integer sysUserId);

    /**
    * 验证用户生成的验证码是否正确
    *
    * @param code     用户生成的验证码
    * @param username 用户名，
    * @return 返回一个布尔值，表示验证是否成功
     */
    ReturnResult<Boolean> verify(String code, @NotNull String username);

    /**
    * 生成一个用于访问特定服务的 URI
    *
    * @param sysUserId 用户标识，表示要生成 URI 的用户
    * @return 返回一个包含生成的 URI 的字符串，用于访问特定服务或功能
     */
    ReturnResult<String> generateUri(Integer sysUserId);
}
