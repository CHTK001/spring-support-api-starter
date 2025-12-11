package com.chua.starter.oauth.sso.support.check;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
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

    /**
     * 登录验证
     * <p>
     * 根据登录类型通过SPI选择合适的UserInfoService实现，验证用户凭证，并生成登录令牌。
     * 不同登录类型的参数含义可能不同，如用户名密码登录、令牌登录或第三方登录。
     * </p>
     *
     * @param address    客户端地址
     * @param principal  用户标识（用户名/手机号/邮箱/令牌等）
     * @param credential 用户凭证（密码/令牌/证书等）
     * @param authType   认证类型，与UserInfoService实现的@Extension注解值匹配
     * @param ext        扩展参数，传递给UserInfoService实现
     * @return 登录结果，包含令牌和用户信息
     */
    public ReturnResult<LoginResult> doLogin(String address, String principal, String credential, String authType, Object ext) {
        long startTime = System.currentTimeMillis();
        log.debug("========== 登录检测开始 ==========");
        log.debug("用户标识: {}, 认证类型: {}, 客户端地址: {}", principal, authType, address);
        
        // 使用 SPI 机制获取 UserInfoService 实现
        UserInfoService userInfoService = ServiceProvider.of(UserInfoService.class).getExtension(authType);
        if (userInfoService == null) {
            log.warn("未找到匹配的认证服务: authType={}", authType);
            return ReturnResult.noAuth("未找到匹配的认证服务");
        }
        
        log.debug("通过SPI获取到UserInfoService: {}", userInfoService.getClass().getSimpleName());
        
        UserResume userResume = null;
        long checkStartTime = System.currentTimeMillis();
        
        try {
            userResume = userInfoService.checkLogin(principal, credential, address, ext);
            log.debug("用户凭证验证完成, 耗时: {}ms, 结果: {}", 
                    System.currentTimeMillis() - checkStartTime,
                    userResume != null ? (Strings.isNullOrEmpty(userResume.getMessage()) ? "成功" : "失败:" + userResume.getMessage()) : "null");
        } catch (Exception e) {
            log.error("用户认证异常: {}, 类型: {}, 异常信息: {}", principal, authType, e.getMessage());
            log.debug("认证异常详情", e);
            return ReturnResult.noAuth("认证异常: " + e.getMessage());
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
     * <p>通过 SPI 机制根据登录类型获取对应的 UserInfoService 实现</p>
     *
     * @param userResult 用户信息
     * @return 用户信息
     */
    public UserResume getUserInfo(UserResult userResult) {
        try {
            String loginType = userResult.getLoginType();
            if (StringUtils.isBlank(loginType)) {
                log.warn("登录类型为空，无法获取用户信息");
                return null;
            }
            
            // 使用 SPI 机制获取 UserInfoService 实现
            UserInfoService userInfoService = ServiceProvider.of(UserInfoService.class).getExtension(loginType);
            if (null == userInfoService) {
                log.warn("未找到匹配的 UserInfoService: loginType={}", loginType);
                return null;
            }

            return userInfoService.upgrade(userResult);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return null;
        }
    }
}
