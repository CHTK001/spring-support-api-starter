package com.chua.starter.oauth.sso.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.service.UserInfoService;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 临时令牌服务
 * <p>基于已有令牌生成短期临时访问令牌，用于特定场景下的短期授权</p>
 *
 * <h2>使用场景</h2>
 * <ul>
 *   <li>需要将当前用户权限临时授予第三方系统访问</li>
 *   <li>执行敏感操作时，要求再次验证身份并使用短期令牌</li>
 *   <li>API网关需要在有限时间内转发身份信息到多个微服务</li>
 *   <li>单次授权访问，令牌使用后立即失效</li>
 * </ul>
 *
 * <h2>实现原理</h2>
 * <ol>
 *   <li>接收已登录用户的有效令牌作为凭证</li>
 *   <li>验证令牌的有效性，获取关联的用户信息</li>
 *   <li>创建一个继承原令牌权限但有效期更短的临时令牌</li>
 *   <li>默认有效期为10秒，可通过ext参数自定义</li>
 * </ol>
 *
 * @author CH
 */
@Slf4j
@Service
@LoginType("TEMPORARY")
@Tag(name = "临时令牌", description = "生成临时访问令牌，默认有效期10秒")
public class TemporaryTokenService implements UserInfoService, Constant {

    /**
     * 默认临时令牌有效期(秒)
     */
    private static final long DEFAULT_EXPIRE_TIME = 10L;

    /**
     * 最大允许的临时令牌有效期(秒)
     */
    private static final long MAX_EXPIRE_TIME = 3600L;

    /**
     * 令牌服务未找到错误信息
     */
    private static final String ERROR_TOKEN_SERVICE_UNAVAILABLE = "令牌服务不可用";

    /**
     * 令牌无效错误信息
     */
    private static final String ERROR_INVALID_TOKEN = "无效的令牌";

    /**
     * 用户信息无效错误信息
     */
    private static final String ERROR_INVALID_USER = "用户信息无效";

    /**
     * 缺少令牌参数错误信息
     */
    private static final String ERROR_MISSING_TOKEN = "缺少令牌参数";

    @Autowired
    private AuthServerProperties authServerProperties;

    /**
     * 验证现有令牌并创建临时令牌
     * <p>
     * 本方法接收用户当前的有效令牌作为principal参数，验证其有效性后，
     * 创建一个短期临时令牌。临时令牌具有与原令牌相同的权限，但有效期更短。
     * </p>
     *
     * @param principal  用户现有的有效令牌
     * @param credential 不使用，可为null
     * @param address    客户端地址
     * @param ext        扩展参数，可指定临时令牌的有效期(expireTime)，单位为秒
     * @return 临时用户令牌信息，若令牌无效则message字段包含错误信息
     */
    @Override
    public UserResult checkLogin(String principal, String credential, String address, Object ext) {
        log.debug("临时令牌请求: IP={}", address);

        // 参数校验 - principal即为用户当前令牌
        if (StringUtils.isEmpty(principal)) {
            log.warn("临时令牌创建失败: 缺少令牌参数");
            return new UserResult().setMessage(ERROR_MISSING_TOKEN);
        }

        // 获取TokenResolver服务
        long startTime = System.currentTimeMillis();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class)
                .getExtension(authServerProperties.getTokenManagement());

        if (null == tokenResolver) {
            log.error("临时令牌创建失败: 令牌解析服务不可用, 配置类型={}", authServerProperties.getTokenManagement());
            return new UserResult().setMessage(ERROR_TOKEN_SERVICE_UNAVAILABLE);
        }

        log.debug("令牌解析器加载耗时: {}ms", System.currentTimeMillis() - startTime);

        // 验证令牌有效性 - 通过principal获取用户信息
        startTime = System.currentTimeMillis();
        ReturnResult<UserResult> resolve = tokenResolver.resolve(null, principal);
        log.debug("令牌解析耗时: {}ms", System.currentTimeMillis() - startTime);

        if (!resolve.isOk() || null == resolve.getData()) {
            log.warn("临时令牌创建失败: 无效的源令牌 {} - {}",
                    principal.substring(0, Math.min(principal.length(), 10)) + "...",
                    resolve.getMessage());
            return new UserResult().setMessage(ERROR_INVALID_TOKEN);
        }

        // 获取原始用户信息
        UserResult userData = resolve.getData();
        if (null == userData) {
            log.warn("临时令牌创建失败: 令牌解析后的用户信息为空");
            return new UserResult().setMessage(ERROR_INVALID_USER);
        }

        // 确定临时令牌过期时间（默认10秒）
        long expireTime = DEFAULT_EXPIRE_TIME;
        if (ext instanceof Map) {
            Map<String, Object> extMap = (Map<String, Object>) ext;
            expireTime = MapUtils.getLong(extMap, "expireTime", DEFAULT_EXPIRE_TIME);

            // 限制最大过期时间
            if (expireTime > MAX_EXPIRE_TIME) {
                log.warn("临时令牌过期时间超过最大限制({}秒)，将使用最大值", MAX_EXPIRE_TIME);
                expireTime = MAX_EXPIRE_TIME;
            }

            // 确保最小过期时间
            if (expireTime < 1) {
                log.warn("临时令牌过期时间小于1秒，将使用默认值{}秒", DEFAULT_EXPIRE_TIME);
                expireTime = DEFAULT_EXPIRE_TIME;
            }
        }

        // 创建一个临时用户结果，包含原始用户的基本信息
        UserResult temporaryResult = createTemporaryResult(userData, expireTime);

        log.info("临时令牌创建成功: 用户={}, IP={}, 过期时间={}秒",
                Optional.ofNullable(userData.getUsername()).orElse(userData.getUserId()),
                address,
                expireTime);

        return temporaryResult;
    }

    /**
     * 创建临时用户结果对象
     *
     * @param userData   原始用户数据
     * @param expireTime 过期时间(秒)
     * @return 临时用户结果对象
     */
    private UserResult createTemporaryResult(UserResult userData, long expireTime) {
        return new UserResult()
                .setId(userData.getId())
                .setUserId(userData.getUserId())
                .setClientId(userData.getClientId())
                .setOpenId(userData.getOpenId())
                .setUnionId(userData.getUnionId())
                .setRoles(userData.getRoles())
                .setRolesByRole(userData.getRolesByRole())
                .setDeptId(userData.getDeptId())
                .setDataPermission(userData.getDataPermission())
                .setDataPermissionRule(userData.getDataPermissionRule())
                .setPermission(userData.getPermission())
                .setPhone(userData.getPhone())
                .setUsername(userData.getUsername())
                .setName(userData.getName())
                .setExpire(expireTime)  // 设置过期时间为临时值
                .setAuthType("TEMPORARY");
    }

    /**
     * 升级/刷新临时令牌信息
     * <p>临时令牌设计为短期使用，不支持升级/刷新操作</p>
     *
     * @param userResult 已有的用户信息
     * @return 原始用户信息，不做任何修改
     */
    @Override
    public UserResult upgrade(UserResult userResult) {
        // 临时令牌不支持升级
        log.debug("临时令牌不支持升级操作: {}",
                Optional.ofNullable(userResult.getUsername()).orElse(userResult.getUserId()));
        return userResult;
    }
} 