package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysUser;
import jakarta.validation.constraints.NotNull;

/**
 * 接口 SimpleSysUserService 继承了IService，专门用于处理SysUser类的简单用户服务
 *
 * @author CH
 * @since 2024/12/2
 */
public interface SimpleSysUserService extends IService<SysUser> {
    /**
     * 绑定用户ID到2FA服务
     *
     * @param sysUserId 用户ID，表示要绑定的用户
     * @return 返回操作结果，包含一个布尔值表示绑定是否成功
     */
    ReturnResult<Boolean> bind(Integer sysUserId);

    /**
     * 解绑用户ID从2FA服务
     *
     * @param sysUserId 用户ID，表示要解绑的用户
     * @return 返回操作结果，包含一个布尔值表示解绑是否成功
     */
    ReturnResult<Boolean> unbind(Integer sysUserId);

    /**
     * 生成2FA密钥二维码
     *
     * @param sysUserId 用户ID
     * @return 返回二维码图片字节数组
     */
    ReturnResult<byte[]> generate(Integer sysUserId);

    /**
     * 验证用户生成的2FA验证码是否正确
     *
     * @param code     用户生成的2FA验证码
     * @param username 用户名
     * @return 返回一个布尔值，表示验证是否成功
     */
    ReturnResult<Boolean> verify(String code, @NotNull String username);

    /**
     * 生成2FA密钥URI
     *
     * @param sysUserId 用户ID，表示要生成URI的用户
     * @return 返回一个包含生成的2FA URI的字符串
     */
    ReturnResult<String> generateUri(Integer sysUserId);
}
