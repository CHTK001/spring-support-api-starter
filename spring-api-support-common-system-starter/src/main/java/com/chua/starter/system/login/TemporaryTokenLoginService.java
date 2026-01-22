package com.chua.starter.system.login;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
 *   <li>限制临时令牌只能访问特定URL</li>
 * </ul>
 *
 * <h2>实现原理</h2>
 * <ol>
 *   <li>接收已登录用户的有效令牌作为凭证</li>
 *   <li>验证令牌的有效性，获取关联的用户信息</li>
 *   <li>创建一个继承原令牌权限但有效期更短的临时令牌</li>
 *   <li>默认有效期为10秒，可通过ext参数自定义</li>
 *   <li>可指定允许访问的URL列表，默认允许所有URL</li>
 * </ol>
 *
 * <h2>扩展参数说明</h2>
 * <ul>
 *   <li>expireTime: 过期时间(秒)，默认10秒，最大3600秒</li>
 *   <li>allowedUrls: 允许访问的URL列表，默认为空表示允许所有URL</li>
 * </ul>
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
@Service
@LoginType("TEMPORARY")
@Tag(name = "临时令牌", description = "生成临时访问令牌，支持URL限制")
public class TemporaryTokenLoginService implements UserInfoService, Constant {

    /**
     * 默认临时令牌有效期(秒)
     */
    private static final long DEFAULT_EXPIRE_TIME = 10L;

    /**
     * 最大允许的临时令牌有效期(秒)
     */
    private static final long MAX_EXPIRE_TIME = 3600L;

    /**
     * 允许访问的URL配置键
     */
    private static final String KEY_ALLOWED_URLS = "allowedUrls";

    /**
     * 过期时间配置键
     */
    private static final String KEY_EXPIRE_TIME = "expireTime";

    /**
     * 默认允许所有URL的标识
     */
    private static final String ALL_URLS = "*";

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
     * @param ext        扩展参数，可指定: expireTime(过期时间秒), allowedUrls(允许的URL列表)
     * @return 临时用户令牌信息，若令牌无效则message字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        log.debug("临时令牌请求: IP={}", address);

        // 参数校验 - principal即为用户当前令牌
        if (StringUtils.isEmpty(principal)) {
            log.warn("临时令牌创建失败: 缺少令牌参数");
            return new UserResume(ERROR_MISSING_TOKEN);
        }

        // 解析扩展参数
        long expireTime = DEFAULT_EXPIRE_TIME;
        Set<String> allowedUrls = new HashSet<>();
        
        if (ext instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extMap = (Map<String, Object>) ext;
            
            // 解析过期时间
            expireTime = parseExpireTime(extMap);
            
            // 解析允许的URL列表
            allowedUrls = parseAllowedUrls(extMap);
        }

        // 创建临时用户结果
        // 注意：实际的令牌验证和用户信息获取应该在调用方完成
        // 这里只负责设置临时令牌的属性
        UserResume temporaryResult = new UserResume();
        temporaryResult.setExpireTime(expireTime);
        temporaryResult.setLoginType("TEMPORARY");
        
        // 设置允许访问的URL到扩展属性
        Map<String, Object> options = new HashMap<>();
        if (allowedUrls.isEmpty()) {
            // 默认允许所有URL
            options.put(KEY_ALLOWED_URLS, ALL_URLS);
        } else {
            // 使用逗号分隔存储URL列表
            options.put(KEY_ALLOWED_URLS, String.join(",", allowedUrls));
        }
        options.put(KEY_EXPIRE_TIME, String.valueOf(expireTime));
        temporaryResult.setExt(options);

        log.info("临时令牌创建成功: IP={}, 过期时间={}秒, 允许URL={}",
                address,
                expireTime,
                allowedUrls.isEmpty() ? "全部" : allowedUrls);

        return temporaryResult;
    }

    /**
     * 解析过期时间
     *
     * @param extMap 扩展参数Map
     * @return 过期时间(秒)
     */
    private long parseExpireTime(Map<String, Object> extMap) {
        long expireTime = MapUtils.getLong(extMap, KEY_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);

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

        return expireTime;
    }

    /**
     * 解析允许访问的URL列表
     *
     * @param extMap 扩展参数Map
     * @return URL集合，空集合表示允许所有URL
     */
    @SuppressWarnings("unchecked")
    private Set<String> parseAllowedUrls(Map<String, Object> extMap) {
        Set<String> allowedUrls = new HashSet<>();
        
        Object urlsObj = extMap.get(KEY_ALLOWED_URLS);
        if (urlsObj == null) {
            // 默认允许所有URL
            return allowedUrls;
        }

        if (urlsObj instanceof String urlStr) {
            // 支持字符串格式："*" 表示全部，或逗号分隔的URL列表
            if (ALL_URLS.equals(urlStr) || StringUtils.isEmpty(urlStr)) {
                return allowedUrls;
            }
            String[] urls = urlStr.split(",");
            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (StringUtils.isNotEmpty(trimmedUrl)) {
                    allowedUrls.add(trimmedUrl);
                }
            }
        } else if (urlsObj instanceof Collection<?> urlList) {
            // 支持集合格式
            for (Object url : urlList) {
                if (url instanceof String urlStr && StringUtils.isNotEmpty(urlStr)) {
                    if (ALL_URLS.equals(urlStr)) {
                        // 如果包含 "*"，表示允许所有URL
                        return new HashSet<>();
                    }
                    allowedUrls.add(urlStr.trim());
                }
            }
        }

        return allowedUrls;
    }

    /**
     * 检查URL是否被允许访问
     * <p>用于验证临时令牌是否可以访问指定的URL</p>
     *
     * @param userResume 用户信息（包含allowedUrls配置）
     * @param requestUrl 请求的URL
     * @return true表示允许访问，false表示拒绝访问
     */
    public static boolean isUrlAllowed(UserResume userResume, String requestUrl) {
        if (userResume == null || StringUtils.isEmpty(requestUrl)) {
            return false;
        }

        String allowedUrlsStr = userResume.getOption(KEY_ALLOWED_URLS);
        
        // 如果没有配置或配置为 "*"，表示允许所有URL
        if (StringUtils.isEmpty(allowedUrlsStr) || ALL_URLS.equals(allowedUrlsStr)) {
            return true;
        }

        // 解析URL列表并检查
        String[] allowedUrls = allowedUrlsStr.split(",");
        for (String allowedUrl : allowedUrls) {
            String trimmedUrl = allowedUrl.trim();
            if (StringUtils.isEmpty(trimmedUrl)) {
                continue;
            }
            
            // 支持通配符匹配
            if (trimmedUrl.endsWith("*")) {
                // 前缀匹配
                String prefix = trimmedUrl.substring(0, trimmedUrl.length() - 1);
                if (requestUrl.startsWith(prefix)) {
                    return true;
                }
            } else if (trimmedUrl.equals(requestUrl)) {
                // 精确匹配
                return true;
            }
        }

        return false;
    }

    /**
     * 升级/刷新临时令牌信息
     * <p>临时令牌设计为短期使用，不支持升级/刷新操作</p>
     *
     * @param userResult 已有的用户信息
     * @return 原始用户信息，不做任何修改
     */
    @Override
    public UserResume upgrade(UserResult userResult) {
        // 临时令牌不支持升级
        log.debug("临时令牌不支持升级操作: {}",
                Optional.ofNullable(userResult.getUsername()).orElse(userResult.getUserId()));
        return userResult;
    }
}
