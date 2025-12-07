package com.chua.starter.oauth.sso.support.check;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.chua.starter.oauth.sso.login.UserInfoService;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Map;

import static com.chua.common.support.constant.CommonConstant.EMPTY_STRING;
import static com.chua.common.support.lang.code.ReturnCode.OK;
import static com.chua.starter.oauth.client.support.execute.AuthClientExecute.createUid;

/**
 * 登录检测
 *
 * @author CH
 */
@RequiredArgsConstructor
@Slf4j
public class LoginCheck {

    final AuthServerProperties authServerProperties;

    final ApplicationContext applicationContext;

    /**
     * 是否匹配类型
     *
     * @param userInfoService 服务
     * @param authType        类型
     * @return 是否匹配类型
     */
    private boolean isMatch(UserInfoService userInfoService, String authType) {
        Class<?> aClass = ClassUtils.getUserClass(userInfoService.getClass());
        LoginType userLoginType = aClass.getDeclaredAnnotation(LoginType.class);
        if (null == userLoginType) {
            return false;
        }
        String value = userLoginType.value();
        if (null == value) {
            AuthType type = userLoginType.group();
            if (null == type) {
                return false;
            }
            value = type.name();
        }
        return value.equalsIgnoreCase(authType);
    }


    /**
     * 登录验证
     * <p>
     * 根据登录类型选择合适的UserInfoService实现，验证用户凭证，并生成登录令牌。
     * 不同登录类型的参数含义可能不同，如用户名密码登录、令牌登录或第三方登录。
     * </p>
     *
     * @param address    客户端地址
     * @param principal  用户标识（用户名/手机号/邮箱/令牌等）
     * @param credential 用户凭证（密码/令牌/证书等）
     * @param authType   认证类型，与UserInfoService实现的@LoginType注解值匹配
     * @param ext        扩展参数，传递给UserInfoService实现
     * @return 登录结果，包含令牌和用户信息
     */
    public ReturnResult<LoginResult> doLogin(String address, String principal, String credential, String authType, Object ext) {
        long startTime = System.currentTimeMillis();
        log.debug("========== 登录检测开始 ==========");
        log.debug("用户标识: {}, 认证类型: {}, 客户端地址: {}", principal, authType, address);
        
        UserResume userResume = null;
        Map<String, UserInfoService> beansOfType = applicationContext.getBeansOfType(UserInfoService.class);
        log.debug("已注册的UserInfoService实现数量: {}", beansOfType.size());
        
        for (Map.Entry<String, UserInfoService> entry : beansOfType.entrySet()) {
            UserInfoService userInfoService = entry.getValue();
            if (!isMatch(userInfoService, authType)) {
                log.debug("UserInfoService [{}] 不匹配认证类型: {}", entry.getKey(), authType);
                continue;
            }
            
            log.debug("匹配到UserInfoService: {}, 开始验证用户凭证", entry.getKey());
            long checkStartTime = System.currentTimeMillis();

            try {
                userResume = userInfoService.checkLogin(principal, credential, address, ext);
                log.debug("用户凭证验证完成, 耗时: {}ms, 结果: {}", 
                        System.currentTimeMillis() - checkStartTime,
                        userResume != null ? (Strings.isNullOrEmpty(userResume.getMessage()) ? "成功" : "失败:" + userResume.getMessage()) : "null");
            } catch (Exception e) {
                log.error("用户认证异常: {}, 类型: {}, 异常信息: {}", principal, authType, e.getMessage());
                log.debug("认证异常详情", e);
                continue;
            }
            if (null != userResume && Strings.isNullOrEmpty(userResume.getMessage())) {
                userResume.setLoginType(authType);
                userResume.setUid(createUid(principal, authType));
                // 优化 userCode 生成：简化结构，只保留必要信息
                // 格式：{uid}:{加密的userId}
                userResume.setUserCode(buildUserCode(userResume));
                log.debug("用户验证成功, userId: {}, uid: {}, userCode长度: {}", 
                        userResume.getUserId(), userResume.getUid(), 
                        userResume.getUserCode() != null ? userResume.getUserCode().length() : 0);
                break;
            }
        }

        if (null == userResume) {
            log.warn("登录失败: 未找到匹配的认证服务或用户不存在, 用户: {}, 类型: {}, 耗时: {}ms", 
                    principal, authType, System.currentTimeMillis() - startTime);
            return ReturnResult.noAuth();
        }

        if (!Strings.isNullOrEmpty(userResume.getMessage())) {
            log.warn("登录失败: {}, 用户: {}, 类型: {}, 耗时: {}ms", 
                    userResume.getMessage(), principal, authType, System.currentTimeMillis() - startTime);
            return ReturnResult.noAuth(userResume.getMessage());
        }

        log.debug("开始创建Token, tokenManagement: {}", authServerProperties.getTokenManagement());
        TokenResolver tokenManagement = ServiceProvider.of(TokenResolver.class).getExtension(authServerProperties.getTokenManagement());
        UserResult userResult = new UserResult();
        BeanUtils.copyProperties(userResume, userResult);
        
        long tokenStartTime = System.currentTimeMillis();
        ReturnResult<LoginResult> token = tokenManagement.createToken(address, userResult, authType);
        log.debug("Token创建完成, 耗时: {}ms, 结果: {}", 
                System.currentTimeMillis() - tokenStartTime, token.isOk() ? "成功" : "失败:" + token.getMsg());
        
        String code = token.getCode();
        if (OK.getCode().equals(code)) {
            LoginResult loginResult = token.getData();
            loginResult.setUserResume(userResume);
            log.info("登录成功: 用户={}, 类型={}, 总耗时={}ms", principal, authType, System.currentTimeMillis() - startTime);
        }
        
        log.debug("========== 登录检测结束 ==========");
        return token;
    }

    /**
     * 构建用户编码
     * <p>
     * 优化后的 userCode 格式：{uid}:{加密的userId}
     * </p>
     * <ul>
     *   <li>uid - 用户唯一标识（MD5）</li>
     *   <li>加密的userId - 使用 AES 加密的用户ID</li>
     * </ul>
     *
     * @param userResume 用户信息
     * @return 用户编码
     */
    private String buildUserCode(UserResume userResume) {
        if (userResume == null || userResume.getUid() == null || userResume.getUserId() == null) {
            return null;
        }
        
        String encryptedUserId = DigestUtils.aesEncrypt(userResume.getUserId(), authServerProperties.getServiceKey());
        return userResume.getUid() + ":" + encryptedUserId;
    }

    /**
     * 获取用户信息
     *
     * @param userResult 用户信息
     * @return 用户信息
     */
    public UserResume getUserInfo(UserResult userResult) {
        try {
            Map<String, UserInfoService> beansOfType = applicationContext.getBeansOfType(UserInfoService.class);
            UserInfoService userInfoService = null;
            for (Map.Entry<String, UserInfoService> entry : beansOfType.entrySet()) {
                if (isMatch(entry.getValue(), userResult.getLoginType())) {
                    userInfoService = entry.getValue();
                    break;
                }
            }
            if (null == userInfoService) {
                return null;
            }

            return userInfoService.upgrade(userResult);
        } catch (Exception e) {
            return null;
        }
    }
}
