package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.OpenIdGenerator;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.service.SysUserThirdPartyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

/**
 * 第三方登录令牌签发服务（OIDC / Gitee / Github / Google / 微信 等统一入口）。
 *
 * <p><b>为什么需要这个实现</b>：所有第三方回调控制器（
 * {@code OidcCallbackController}、{@code GiteeCallbackController}、
 * {@code GithubCallbackController}、{@code GoogleCallbackController}、
 * {@code WechatCallbackController}、{@code ThirdPartyPlatformCallbackController}）
 * 在完成「第三方身份 → 本地账号」绑定校验后，统一调用
 * {@code AuthClientExecute#getAccessToken(sysUserId, null, AuthType.THIRD_PARTY, ext)}
 * 换取系统令牌，请求里的 {@code x-oauth-auth-type} 即 {@code THIRD_PARTY}。
 * 认证端 {@code LoginCheck#resolveUserInfoService} 按该值在 Spring 容器中查找
 * 带 {@code @Spi} 的 {@link com.chua.starter.oauth.client.support.user.UserInfoService}
 * 实现；此前容器里只有 WEB / tenant / TEMPORARY / gitee / github / google /
 * WECHAT / WECHAT_ENTERPRISE / DINGTALK / ALIPAY / WECHAT_MINI，
 * <b>没有任何实现注册 {@code THIRD_PARTY}</b>，于是每次第三方登录都止步于
 * 「不支持该登录/鉴权方式: THIRD_PARTY」，回调只能重定向回
 * {@code ?thirdMsg=xxx登录失败，请重试}。本类补齐这一环。</p>
 *
 * <p><b>principal 的语义</b>：这里是已通过第三方渠道校验的<b>本地用户ID</b>
 * （{@code sys_user_third_party.sys_user_id}），不是可被外部任意指定的用户名。
 * 为防止该入口被当成「按用户ID直接登录」的后门，本类额外做一次绑定关系校验：
 * {@code ext.loginType}（oidc / gitee / github / google / wechat ...）必须能在
 * {@code sys_user_third_party} 中命中该用户的一条绑定记录，否则拒绝签发。</p>
 *
 * <p><b>令牌签发不在这里</b>：{@code LoginCheck#doLogin} 拿到本类返回的
 * {@link UserResume} 后统一走 {@code TokenResolver} 签发，本类只负责
 * 「验明身份 + 如实描述这个人」。角色 / 权限 / 数据权限由
 * {@link AbstractUserLoginService#loginInfo(String, SysUser, String)} 统一装配。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Service
@Spi("THIRD_PARTY")
@LoginType("THIRD_PARTY")
@Tag(name = "第三方登录服务", description = "OIDC / Gitee / Github / Google / 微信等第三方登录的令牌签发")
public class ThirdPartyLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀（用于 Redis 中登录相关键的命名）
     */
    private static final String LOGIN_TYPE_PREFIX = "third_party";

    /**
     * 第三方登录类型在 ext 中的键（回调控制器写入 loginType 与 thirdPartyType 两个同值键）
     */
    private static final String EXT_LOGIN_TYPE = "loginType";

    /**
     * 第三方登录类型的备用键
     */
    private static final String EXT_THIRD_PARTY_TYPE = "thirdPartyType";

    /**
     * 回调侧携带的客户端地址键
     */
    private static final String EXT_ADDRESS = "address";

    @Autowired
    /**
     * 用户第三方账号绑定服务
     */
    private SysUserThirdPartyService sysUserThirdPartyService;

    @Override
    /**
     * 获取login类型前缀
     */
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    /**
     * generate用户标识
     */
    protected String[] generateUserIds(SysUser user) {
        return OpenIdGenerator.generateSystemIds("third_party", String.valueOf(user.getSysUserId()));
    }

    @Override
    /**
     * 校验Login
     *
     * @param principal  已完成第三方校验的本地用户ID
     * @param credential 不使用，可为空
     * @param address    客户端地址
     * @param ext        扩展参数，含 loginType / thirdPartyType 与回调侧地址
     * @return 用户resume的结果
     */
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        Integer userId = Converter.convertIfNecessary(principal, Integer.class);
        if (null == userId) {
            log.warn("第三方登录失败: 本地用户ID无效 principal={}", principal);
            return new UserResume("账号不存在");
        }

        String loginType = resolveLoginType(ext);
        if (StringUtils.isEmpty(loginType)) {
            // 没有渠道类型的 THIRD_PARTY 请求无从校验绑定关系，一律拒绝
            log.warn("第三方登录失败: 缺少第三方登录类型 ext={}", ext);
            return new UserResume("第三方登录类型无效");
        }

        long bound = sysUserThirdPartyService.count(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserId, userId)
                .apply("UPPER(sys_user_third_party_type) = {0}", loginType.toUpperCase(Locale.ROOT)));
        if (bound <= 0) {
            log.warn("第三方登录失败: 用户未绑定该渠道 userId={}, loginType={}", userId, loginType);
            return new UserResume("请先绑定第三方账号后再登录");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (null == user) {
            log.warn("第三方登录失败: 账号不存在 userId={}", userId);
            return new UserResume("账号不存在");
        }
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("第三方登录失败: 账户已禁用 userId={}", userId);
            return new UserResume("账号已被禁用");
        }

        String ip = resolveAddress(address, ext);
        log.info("第三方登录成功: {} (ID:{}), 渠道: {}, IP: {}",
                user.getSysUserUsername(), userId, loginType, ip);
        return loginInfo(user.getSysUserUsername(), user, ip);
    }

    @Override
    /**
     * Upgrade
     */
    public UserResume upgrade(UserResult userResult) {
        Integer userId = Converter.convertIfNecessary(userResult.getUserId(), Integer.class);
        SysUser user = null == userId ? null : sysUserMapper.selectById(userId);
        if (null == user) {
            return new UserResume("账号不存在");
        }
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            return new UserResume("账号已被禁用");
        }
        UserResume result = loginInfo(userResult.getUsername(), user, null);
        result.setLoginType(userResult.getLoginType());
        return result;
    }

    /**
     * 解析第三方登录渠道类型。
     *
     * @param ext 扩展参数
     * @return 渠道类型（小写原样返回），解析不到返回空串
     */
    private String resolveLoginType(Object ext) {
        if (!(ext instanceof Map<?, ?> extMap)) {
            return "";
        }
        Object value = extMap.get(EXT_LOGIN_TYPE);
        if (null == value) {
            value = extMap.get(EXT_THIRD_PARTY_TYPE);
        }
        return null == value ? "" : StringUtils.trimToEmpty(String.valueOf(value));
    }

    /**
     * 解析登录地址。
     *
     * <p>协议服务器拿不到浏览器对端地址，回调控制器已把真实地址放进 ext，
     * 这里优先取 ext，其次才用协议层地址，避免登录日志与 lastIp 记为内网回环。</p>
     *
     * @param address 协议层地址
     * @param ext     扩展参数
     * @return 客户端地址
     */
    private String resolveAddress(String address, Object ext) {
        if (ext instanceof Map<?, ?> extMap) {
            Object value = extMap.get(EXT_ADDRESS);
            if (null != value && StringUtils.isNotEmpty(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return address;
    }
}
