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
 */
@Slf4j
public final class OauthAuthorization implements Authorization {
    private final String token;
    private final String cookie;
    private final String subProtocol;
    private final String userCode;
    private final String requestFingerprint;
    private final boolean verifyFingerprint;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final OauthAuthInformation authInformation;

    public OauthAuthorization(OauthAuthInformation authInformation,
                              String token,
                              String cookie,
                              String subProtocol,
                              String userCode,
                              String requestFingerprint,
                              boolean verifyFingerprint,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.subProtocol = subProtocol;
        this.userCode = userCode;
        this.requestFingerprint = requestFingerprint;
        this.verifyFingerprint = verifyFingerprint;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        log.info("开始认证: subProtocol={}, hasToken={}, hasUserCode={}", 
                subProtocol, StringUtils.isNotBlank(token), StringUtils.isNotBlank(userCode));
        
        // 获取追踪上下文
        ServerTraceContext traceContext = ServerTraceContext.current();
        
        // Span: 验证Token
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
     * 1. 根据subProtocol精确匹配
     * 2. 根据参数类型自动选择（userCode→AppKey, token→JWT）
     * 3. 使用默认处理器
     * </p>
     *
     * @return 认证处理器
     */
    private AuthorizationHandler selectAuthorizationHandler() {
        // 1. 如果指定了subProtocol，尝试精确匹配
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
