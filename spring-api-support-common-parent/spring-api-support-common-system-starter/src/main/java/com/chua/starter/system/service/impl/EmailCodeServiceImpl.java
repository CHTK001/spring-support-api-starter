package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.pojo.EmailCodeRequest;
import com.chua.starter.system.pojo.EmailLoginRequest;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.EmailCodeService;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.service.VerifyCodeService;
import static com.chua.starter.system.constant.SystemConstant.USER_STATUS_ENABLED;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮箱验证码服务实现
 *
 * <p>验证码生成、Redis 存储与投递复用通用 {@link VerifyCodeService} 的 EMAIL 渠道；
 * 登录场景额外校验邮箱已注册且账号启用，随后按协议签发令牌。</p>
 *
 * @since 4.0.0.50
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    /**
     * 验证码渠道标识，与 EmailVerifyCodeChannel 的 SPI 键一致
     */
    private static final String CHANNEL_EMAIL = "EMAIL";

    /**
     * 登录业务场景
     */
    private static final String BIZ_LOGIN = "login";

    /**
     * 通用验证码服务
     */
    private final VerifyCodeService verifyCodeService;
    /**
     * 用户映射器
     */
    private final SysUserMapper sysUserMapper;
    /**
     * 登录服务
     */
    private final SysLoginService sysLoginService;
    /**
     * 认证客户端配置（判断 static / 远程协议）
     */
    private final AuthClientProperties authClientProperties;

    @Override
    public ReturnResult<Boolean> sendCode(EmailCodeRequest request) {
        String email = request.email();
        String bizType = request.bizType();
        log.debug("发送邮箱验证码: email={}, bizType={}", email, bizType);

        // 登录业务需要验证邮箱是否已注册且启用
        if (BIZ_LOGIN.equals(bizType)) {
            SysUser user = findEnabledByEmail(email);
            if (user == null) {
                log.warn("邮箱未注册或账号已禁用: email={}", email);
                return ReturnResult.error("该邮箱未注册或账号已被禁用");
            }
        }

        ReturnResult<com.chua.starter.system.pojo.verify.VerifyCodeSentVO> sent =
                verifyCodeService.send(CHANNEL_EMAIL, bizType, email);
        if (null == sent || sent.getData() == null) {
            return ReturnResult.error(null == sent ? "验证码发送失败" : sent.getMsg());
        }
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<LoginResult> login(EmailLoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String email = request.email();
        String code = request.code();
        log.debug("邮箱验证码登录: email={}", email);

        // 校验并消费验证码
        if (!verifyCodeService.verify(CHANNEL_EMAIL, BIZ_LOGIN, email, code)) {
            return ReturnResult.error("验证码错误或已过期");
        }

        // 根据邮箱查询用户
        SysUser user = findEnabledByEmail(email);
        if (user == null) {
            log.warn("邮箱未注册或账号已禁用: email={}", email);
            return ReturnResult.error("该邮箱未注册或账号已被禁用");
        }

        // 签发令牌：static 复用本地令牌构建；远程协议经 OAuth 按账号签发
        LoginAuthResult accessToken;
        if (authClientProperties != null && "static".equalsIgnoreCase(authClientProperties.getProtocol())) {
            accessToken = sysLoginService.issueLocalToken(user);
        } else {
            Map<String, Object> ext = new HashMap<>();
            ext.put("loginType", "email");
            ext.put("email", email);
            ext.put("address", RequestUtils.getIpAddress(httpRequest));
            accessToken = AuthClientExecute.getInstance().emailLogin(user.getSysUserUsername(), ext);
        }

        if (accessToken == null || StringUtils.isEmpty(accessToken.getToken())) {
            log.error("获取 Token 失败: email={}", email);
            return ReturnResult.error("登录失败，请重试");
        }

        // 构建登录结果
        LoginResult loginResult = LoginResult.builder()
                .userInfo(accessToken.getUserResume() != null
                        ? sysLoginService.buildUserInfo(accessToken.getUserResume())
                        : sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .refreshToken(accessToken.getRefreshToken())
                .build();

        // 设置 Cookie
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);

        log.info("邮箱验证码登录成功: email={}, userId={}", email, user.getSysUserId());
        return ReturnResult.ok(loginResult);
    }

    /**
     * 按邮箱查询启用状态的用户
     *
     * @param email 邮箱
     * @return 用户，不存在或已禁用时返回 null
     */
    private SysUser findEnabledByEmail(String email) {
        return sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getSysUserEmail, email)
                        .eq(SysUser::getSysUserStatus, USER_STATUS_ENABLED)
        );
    }
}
