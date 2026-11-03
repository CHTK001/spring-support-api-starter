// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.parser.handler.AuthorizationHandler;
import com.chua.starter.oauth.sso.support.trace.ServerTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 鉴权信息
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
public final class OauthAuthorization implements Authorization {
    /**
     * 令牌
    */
    private final String token;
    /**
     * Cookie
    */
    private final String cookie;
    /**
     * SUB协议
    */
    private final String subProtocol;
    /**
     * 用户代码
    */
    private final String userCode;
    final ApplicationContext applicationContext; // application上下文
    final LoginCheck loginCheck; // login检查
    /**
     * Authinformation
    */
    private final OauthAuthInformation authInformation;

    /**
     * 创建 oauth授权 实例
     * @param authInformation 认证信息
     * @param token 令牌
     * @param cookie Cookie
     * @param subProtocol sub协议
     * @param userCode 用户编码
     * @param applicationContext application上下文
     * @param loginCheck login检查
     */
    public OauthAuthorization(OauthAuthInformation authInformation,
                              String token,
                              String cookie,
                              String subProtocol,
                              String userCode,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.subProtocol = subProtocol;
        this.userCode = userCode;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    /**
     * 认证
    */
    public ReturnResult<String> authentication() {
        log.info("开始认证: subProtocol={}, hasToken={}, hasUserCode={}", 
                subProtocol, StringUtils.isNotBlank(token), StringUtils.isNotBlank(userCode));
        
        // 获取追踪上下文
        ServerTraceContext traceContext = ServerTraceContext.current();
        
 // Span: 验证令牌
        if (traceContext != null) {
            traceContext.startSpan("server_validate_token");
        }
        
        try {
            // 使用SPI机制选择认证处理器
            AuthorizationHandler handler = selectAuthorizationHandler();
            if (handler == null) {
                log.error("找不到合适的认证处理器: subProtocol={}", subProtocol);
                return ReturnResult.error("不支持的认证方式");
            }
            
            log.info("使用认证处理器: {}", handler.getClass().getSimpleName());
            return handler.authenticate(authInformation, token, cookie, userCode);
        } finally {
            if (traceContext != null) {
                traceContext.endSpan();
            }
        }
    }
    
    /**
     * 选择认证处理器
     * <p>
     * 优先级：
     * 1. 根据sub协议精确匹配
     * 2. 根据参数类型自动选择（用户编码→app键, 令牌→JWT）
     * 3. 使用默认处理器
     * </p>
     *
     * @return 认证处理器
     */
    private AuthorizationHandler selectAuthorizationHandler() {
 // 1. 如果指定了sub协议，尝试精确匹配
        if (StringUtils.isNotBlank(subProtocol)) {
            AuthorizationHandler handler = ServiceProvider.of(AuthorizationHandler.class)
                    .getExtension(subProtocol);
            if (handler != null) {
                log.debug("根据subProtocol选择处理器: {}", subProtocol);
                return handler;
            }
        }
        
        // 2. 根据参数类型自动选择
        ServiceProvider<AuthorizationHandler> provider = ServiceProvider.of(AuthorizationHandler.class);
        for (String name : provider.getExtensions()) {
            AuthorizationHandler handler = provider.getExtension(name);
            if (handler != null && handler.supports(subProtocol, token, userCode)) {
                log.debug("根据参数类型选择处理器: {}", handler.getClass().getSimpleName());
                return handler;
            }
        }
        
        // 3. 使用默认处理器
        log.debug("使用默认认证处理器");
        return provider.getNewExtension("default");
    }

}
