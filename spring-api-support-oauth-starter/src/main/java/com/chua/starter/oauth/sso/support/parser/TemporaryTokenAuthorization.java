package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.controller.OAuthAdminController;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 临时令牌授权处理器
 * <p>负责验证源令牌并创建临时令牌</p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-11
 */
@Slf4j
public final class TemporaryTokenAuthorization implements Authorization {

    private final TemporaryTokenAuthInformation authInformation;
    private final ApplicationContext applicationContext;

    /**
     * 构造函数
     *
     * @param authInformation    认证信息
     * @param applicationContext Spring应用上下文
     */
    public TemporaryTokenAuthorization(TemporaryTokenAuthInformation authInformation,
                                       ApplicationContext applicationContext) {
        this.authInformation = authInformation;
        this.applicationContext = applicationContext;
    }

    /**
     * 执行临时令牌创建认证
     *
     * @return 创建结果，成功返回临时令牌
     */
    @Override
    public ReturnResult<String> authentication() {
        long startTime = System.currentTimeMillis();
        String clientIp = authInformation.getClientIp();
        String sourceToken = authInformation.getSourceToken();

        log.debug("========== 临时令牌创建开始 ==========");
        log.debug("客户端IP: {}", clientIp);

        // 验证源令牌是否存在
        if (Strings.isNullOrEmpty(sourceToken)) {
            log.warn("临时令牌创建失败: 源令牌为空, 客户端IP: {}", clientIp);
            recordRequest(clientIp, false, "源令牌为空", startTime);
            return ReturnResult.illegal("源令牌不能为空");
        }

        // 获取 TokenResolver
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        if (tokenResolver == null) {
            log.error("临时令牌创建失败: TokenResolver 不可用, tokenManagement: {}", tokenManagement);
            recordRequest(clientIp, false, "令牌服务不可用", startTime);
            return ReturnResult.error("令牌服务不可用");
        }

        // 验证源令牌
        ReturnResult<UserResume> resolveResult = tokenResolver.resolve(null, sourceToken);
        if (resolveResult.isFailure()) {
            log.warn("临时令牌创建失败: 源令牌无效, 客户端IP: {}, 错误: {}", clientIp, resolveResult.getMsg());
            recordRequest(clientIp, false, "源令牌无效", startTime);
            return ReturnResult.error("源令牌无效");
        }

        UserResume sourceUserResume = resolveResult.getData();
        log.debug("源令牌验证成功, 用户: {}", sourceUserResume.getUsername());

        // 获取过期时间和允许的URL
        long expireTime = authInformation.getExpireTime();
        String allowedUrls = authInformation.getAllowedUrls();

        // 构建临时用户信息
        UserResult temporaryUser = buildTemporaryUser(sourceUserResume, expireTime, allowedUrls);

        // 创建临时令牌
        ReturnResult<LoginResult> createResult = tokenResolver.createToken(clientIp, temporaryUser, "TEMPORARY");
        if (createResult.isFailure()) {
            log.warn("临时令牌创建失败: {}, 客户端IP: {}", createResult.getMsg(), clientIp);
            recordRequest(clientIp, false, createResult.getMsg(), startTime);
            return ReturnResult.error(createResult.getMsg());
        }

        LoginResult loginResult = createResult.getData();
        long cost = System.currentTimeMillis() - startTime;
        log.info("临时令牌创建成功, 用户: {}, 客户端IP: {}, 过期时间: {}秒, 耗时: {}ms",
                sourceUserResume.getUsername(), clientIp, expireTime, cost);
        log.debug("========== 临时令牌创建结束 ==========");

        recordRequest(clientIp, true, "临时令牌创建成功", startTime);

        // 返回加密后的临时令牌
        return ReturnResult.ok(authInformation.encodeHex(loginResult.getToken()));
    }

    /**
     * 构建临时用户信息
     *
     * @param sourceUserResume 源用户信息
     * @param expireTime       过期时间（秒）
     * @param allowedUrls      允许访问的URL
     * @return 临时用户信息
     */
    private UserResult buildTemporaryUser(UserResume sourceUserResume, long expireTime, String allowedUrls) {
        UserResult temporaryUser = new UserResult();
        temporaryUser.setUserId(sourceUserResume.getUserId());
        temporaryUser.setUsername(sourceUserResume.getUsername());
        temporaryUser.setRoles(sourceUserResume.getRoles());
        temporaryUser.setPermission(sourceUserResume.getPermission());
        temporaryUser.setExpireTime(expireTime);
        temporaryUser.setLoginType("TEMPORARY");
        temporaryUser.setTenantId(sourceUserResume.getTenantId());
        temporaryUser.setDeptId(sourceUserResume.getDeptId());

        // 设置临时令牌的扩展属性
        Map<String, Object> options = new HashMap<>();
        options.put("allowedUrls", Strings.isNullOrEmpty(allowedUrls) ? "*" : allowedUrls);
        options.put("expireTime", String.valueOf(expireTime));
        options.put("sourceUserId", sourceUserResume.getUserId());
        temporaryUser.setExt(options);

        return temporaryUser;
    }

    /**
     * 记录请求信息
     *
     * @param clientIp  客户端IP
     * @param success   是否成功
     * @param message   消息
     * @param startTime 开始时间
     */
    private void recordRequest(String clientIp, boolean success, String message, long startTime) {
        long cost = System.currentTimeMillis() - startTime;
        OAuthAdminController.recordRequest("TEMPORARY_TOKEN", clientIp, null, success, message, cost);
    }
}
