package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.crypto.Base32;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.otp.DefaultTotpGenerator;
import com.chua.common.support.otp.TotpGenerator;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

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

    private static final String ISSUER = "SpringAPI";
    /**
     * TOTP 时间窗口容差（前后各允许多少个时间窗口）
     */
    private static final int TIME_WINDOW_TOLERANCE = 1;
    /**
     * 密钥长度（字节）
     */
    private static final int SECRET_LENGTH = 20;

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
     * 校验 TOTP 验证码
     * <p>
     * 使用标准 TOTP 算法进行校验，支持时间窗口容差。
     * </p>
     *
     * @param code     验证码（6位数字）
     * @param username 用户名
     * @return 校验结果
     */
    @Override
    public ReturnResult<Boolean> verify(String code, String username) {
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(username)) {
            log.warn("[SimpleSysUserService] TOTP验证参数为空，username={}, code={}", username, code);
            return ReturnResult.illegal("验证码或用户名不能为空");
        }

        // 根据用户名查询用户
        SysUser sysUser = lambdaQuery()
                .eq(SysUser::getSysUserUsername, username)
                .one();
        if (sysUser == null) {
            log.warn("[SimpleSysUserService] TOTP验证失败，用户不存在，username={}", username);
            return ReturnResult.illegal("用户不存在");
        }

        String secret = sysUser.getSysUserTotpCode();
        if (StringUtils.isEmpty(secret)) {
            log.info("[SimpleSysUserService] 用户未绑定2FA，跳过验证，username={}", username);
            // 未绑定 TOTP 的用户，返回成功（表示不需要验证）
            return ReturnResult.ok(true);
        }

        // 使用 TotpGenerator 进行验证（支持时间窗口容差）
        boolean verified = verifyWithTolerance(secret, code);
        log.info("[SimpleSysUserService] TOTP验证完成，username={}, verified={}", username, verified);

        if (!verified) {
            return ReturnResult.illegal("验证码错误或已过期");
        }
        return ReturnResult.ok(true);
    }

    /**
     * 带时间窗口容差的 TOTP 验证
     *
     * @param secret Base32 编码的密钥
     * @param code   用户输入的验证码
     * @return 验证是否通过
     */
    private boolean verifyWithTolerance(String secret, String code) {
        try {
            byte[] secretBytes = Base32.decode(secret);
            TotpGenerator totp = new DefaultTotpGenerator(secretBytes);

            // 验证当前时间窗口
            if (totp.verify(code)) {
                return true;
            }

            // 验证前后时间窗口（容差）
            long currentTime = System.currentTimeMillis();
            long interval = 30 * 1000L; // 30 秒一个窗口

            for (int i = 1; i <= TIME_WINDOW_TOLERANCE; i++) {
                // 检查之前的时间窗口
                String pastCode = totp.at(currentTime - i * interval);
                if (pastCode.equals(code)) {
                    return true;
                }
                // 检查之后的时间窗口
                String futureCode = totp.at(currentTime + i * interval);
                if (futureCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("[SimpleSysUserService] TOTP验证异常", e);
            return false;
        }
    }

    /**
     * 生成 2FA URI
     * <p>
     * 使用标准 otpauth:// URI 格式，兼容 Google Authenticator、Microsoft Authenticator 等应用。
     * </p>
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

        try {
            // 使用 TotpGenerator 生成标准 URI
            byte[] secretBytes = Base32.decode(secret);
            TotpGenerator totp = new DefaultTotpGenerator(secretBytes);
            String account = StringUtils.isEmpty(sysUser.getSysUserUsername())
                    ? String.valueOf(sysUserId)
                    : sysUser.getSysUserUsername();
            URI uri = totp.getUri(ISSUER, account);
            return ReturnResult.ok(uri.toString());
        } catch (URISyntaxException e) {
            log.error("[SimpleSysUserService] 生成2FA URI异常，sysUserId={}", sysUserId, e);
            return ReturnResult.error("生成URI失败");
        }
    }

    /**
     * 生成标准 TOTP 密钥
     * <p>
     * 使用 SecureRandom 生成随机字节，然后进行 Base32 编码。
     * </p>
     *
     * @return Base32 编码的密钥
     */
    private String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_LENGTH];
        random.nextBytes(bytes);
        return Base32.encode(bytes);
    }

    /**
     * 检查用户是否已绑定 TOTP
     *
     * @param username 用户名
     * @return true 表示已绑定，false 表示未绑定
     */
    public boolean isTotpEnabled(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        SysUser sysUser = lambdaQuery()
                .eq(SysUser::getSysUserUsername, username)
                .one();
        return sysUser != null && StringUtils.isNotEmpty(sysUser.getSysUserTotpCode());
    }
}
