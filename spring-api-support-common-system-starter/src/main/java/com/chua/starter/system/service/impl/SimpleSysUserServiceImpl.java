package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 简单用户服务实现类
 * <p>
 * 主要用于登录回调、2FA(TOTP) 等轻量能力。
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleSysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SimpleSysUserService {

    private static final String OTP_AUTH_URI_PREFIX = "otpauth://totp/";

    /**
     * 绑定 2FA 密钥
     *
     * @param sysUserId 用户ID
     * @return 绑定结果
     */
    @Override
    public ReturnResult<Boolean> bind(Integer sysUserId) {
        SysUser sysUser = getById(sysUserId);
        if (sysUser == null) {
            log.warn("[SimpleSysUserService] 绑定2FA失败，用户不存在，sysUserId={}", sysUserId);
            return ReturnResult.illegal("用户信息不存在");
        }

        String secret = generateSecret();
        sysUser.setSysUserTotpCode(secret);
        boolean updated = updateById(sysUser);

        log.info("[SimpleSysUserService] 绑定2FA完成，sysUserId={}, updated={}", sysUserId, updated);
        return ReturnResult.of(updated);
    }

    /**
     * 解绑 2FA 密钥
     *
     * @param sysUserId 用户ID
     * @return 解绑结果
     */
    @Override
    public ReturnResult<Boolean> unbind(Integer sysUserId) {
        SysUser sysUser = getById(sysUserId);
        if (sysUser == null) {
            log.warn("[SimpleSysUserService] 解绑2FA失败，用户不存在，sysUserId={}", sysUserId);
            return ReturnResult.illegal("用户信息不存在");
        }

        sysUser.setSysUserTotpCode(null);
        boolean updated = updateById(sysUser);

        log.info("[SimpleSysUserService] 解绑2FA完成，sysUserId={}, updated={}", sysUserId, updated);
        return ReturnResult.of(updated);
    }

    /**
     * 生成 2FA 密钥内容
     * <p>
     * 当前实现返回密钥原文的 UTF-8 字节，便于前端自行生成二维码。
     * </p>
     *
     * @param sysUserId 用户ID
     * @return 密钥内容
     */
    @Override
    public ReturnResult<byte[]> generate(Integer sysUserId) {
        SysUser sysUser = getById(sysUserId);
        if (sysUser == null) {
            log.warn("[SimpleSysUserService] 生成2FA密钥失败，用户不存在，sysUserId={}", sysUserId);
            return ReturnResult.illegal("用户信息不存在");
        }

        String secret = sysUser.getSysUserTotpCode();
        if (StringUtils.isEmpty(secret)) {
            secret = generateSecret();
            sysUser.setSysUserTotpCode(secret);
            updateById(sysUser);
            log.debug("[SimpleSysUserService] 用户未绑定2FA，已自动生成密钥，sysUserId={}", sysUserId);
        }

        return ReturnResult.ok(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验验证码
     * <p>
     * 说明：该方法需要标准 TOTP 算法支持。当前模块未引入额外依赖，暂不做强校验。
     * 建议在后续接入统一的 TOTP 工具类后完善。
     * </p>
     *
     * @param code     验证码
     * @param username 用户名
     * @return 校验结果
     */
    @Override
    public ReturnResult<Boolean> verify(String code, String username) {
        log.warn("[SimpleSysUserService] verify 尚未实现强校验，username={}, codeLength={}", username,
                code == null ? 0 : code.length());
        return ReturnResult.illegal("暂不支持验证码校验");
    }

    /**
     * 生成 2FA URI
     *
     * @param sysUserId 用户ID
     * @return URI
     */
    @Override
    public ReturnResult<String> generateUri(Integer sysUserId) {
        SysUser sysUser = getById(sysUserId);
        if (sysUser == null) {
            log.warn("[SimpleSysUserService] 生成2FA URI失败，用户不存在，sysUserId={}", sysUserId);
            return ReturnResult.illegal("用户信息不存在");
        }

        String secret = sysUser.getSysUserTotpCode();
        if (StringUtils.isEmpty(secret)) {
            secret = generateSecret();
            sysUser.setSysUserTotpCode(secret);
            updateById(sysUser);
            log.debug("[SimpleSysUserService] 用户未绑定2FA，已自动生成密钥用于URI，sysUserId={}", sysUserId);
        }

        String label = StringUtils.isEmpty(sysUser.getSysUserUsername()) ? String.valueOf(sysUserId) : sysUser.getSysUserUsername();
        String uri = OTP_AUTH_URI_PREFIX + label + "?secret=" + secret;
        return ReturnResult.ok(uri);
    }

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    private String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
