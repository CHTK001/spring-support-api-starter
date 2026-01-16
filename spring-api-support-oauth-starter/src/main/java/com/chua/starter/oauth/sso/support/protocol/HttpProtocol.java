package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.task.limit.RateLimiter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.advice.AdviceView;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.controller.OAuthAdminController;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.parser.TemporaryTokenAuthorization;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.parser.LogoutAuthorization;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.parser.UpgradeAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * http
 *
 * @author CH
 */
@Controller
@Extension("http")
@com.chua.common.support.annotations.Spi("http")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "提供HTTP协议的认证服务接口")
public class HttpProtocol implements Protocol, InitializingBean {

    final AuthServerProperties authServerProperties;

    final LoginCheck loginCheck;


    final ApplicationContext applicationContext;

    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;
    /**
     * 登录
     *
     * @return 登录页
     */
    @Operation(
            summary = "用户登录",
            description = "处理用户登录请求，验证用户身份并颁发访问令牌",
            responses = {
                    @ApiResponse(responseCode = "200", description = "登录成功，返回令牌"),
                    @ApiResponse(responseCode = "401", description = "登录失败，用户名或密码错误")
            }
    )
    @ResponseBody
    @PostMapping("/login")
    @RateLimiter(value = 30, mode = "ip", message = "登录请求过于频繁，请30秒后重试")
    public ReturnResult<String> login(
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            RedirectAttributes modelMap
    ) {
        long startTime = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        log.debug("========== 登录请求开始 ==========");
        log.debug("客户端IP: {}, User-Agent: {}", clientIp, userAgent);
        log.debug("请求头 x-oauth-uuid: {}", request.getHeader("x-oauth-uuid"));
        log.debug("请求头 x-oauth-serial: {}", request.getHeader("x-oauth-serial"));
        log.debug("请求头 x-oauth-encode: {}", request.getHeader("x-oauth-encode"));
        log.debug("请求数据长度: {}", authRequest.getData() != null ? authRequest.getData().length() : 0);
        
        LoginAuthInformation authInformation = new LoginAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        
        if(!authInformation.isSuccessful()) {
            log.warn("登录请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                    authInformation.errorMessage(), clientIp, System.currentTimeMillis() - startTime);
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        
        log.debug("登录请求解析成功, 开始认证...");
        LoginAuthorization authorization = authInformation.authorization();
        ReturnResult<String> authentication = authorization.authentication();
        
        if(authentication.isFailure()) {
            long cost = System.currentTimeMillis() - startTime;
            log.warn("登录认证失败: {}, 客户端IP: {}, 耗时: {}ms", 
                    authentication.getMsg(), clientIp, cost);
            // 记录失败请求
            OAuthAdminController.recordRequest("LOGIN", clientIp, null, false, authentication.getMsg(), cost);
            return ReturnResult.error(authentication.getMsg());
        }
        
        long cost = System.currentTimeMillis() - startTime;
        log.info("登录认证成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
        log.debug("========== 登录请求结束 ==========");
        // 记录成功请求
        OAuthAdminController.recordRequest("LOGIN", clientIp, null, true, "登录成功", cost);
        return authentication;
    }


    /**
     * 登出
     *
     * @return 登出
     */
    @PostMapping("/logout")
    @ResponseBody
    @Operation(summary = "登出")
    @RateLimiter(value = 60, mode = "ip", message = "登出请求过于频繁，请稍后重试")
    public ReturnResult<String> logout(@RequestBody AuthRequest authRequest,
                                        HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();
        
        log.debug("========== 登出请求开始 ==========");
        log.debug("客户端IP: {}", clientIp);
        
        LogoutAuthInformation authInformation = new LogoutAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        if(!authInformation.isSuccessful()) {
            log.warn("登出请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                    authInformation.errorMessage(), clientIp, System.currentTimeMillis() - startTime);
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        LogoutAuthorization authorization = authInformation.authorization();
        ReturnResult<String> authentication = authorization.authentication();
        if(authentication.isFailure()) {
            long cost = System.currentTimeMillis() - startTime;
            log.warn("登出失败: {}, 客户端IP: {}, 耗时: {}ms", 
                    authentication.getMsg(), clientIp, cost);
            OAuthAdminController.recordRequest("LOGOUT", clientIp, null, false, authentication.getMsg(), cost);
            return ReturnResult.error(authentication.getMsg());
        }
        
        long cost = System.currentTimeMillis() - startTime;
        log.info("登出成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
        log.debug("========== 登出请求结束 ==========");
        OAuthAdminController.recordRequest("LOGOUT", clientIp, null, true, "登出成功", cost);
        return authentication;
    }

    /**
     * 登出API（内部调用）
     * <p>
     * 用于处理API方式的用户登出请求，支持不同的登出类型：
     * - SINGLE: 单点登出，仅登出当前会话
     * - ALL: 全局登出，登出用户所有会话
     * </p>
     *
     * @param data     加密的登出数据
     * @param type     登出类型
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    private ReturnResult<String> logoutApi(String data, LogoutType type, HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();
        
        log.debug("========== API登出请求开始 ==========");
        log.debug("客户端IP: {}, 登出类型: {}", clientIp, type);
        
        if (Strings.isNullOrEmpty(data)) {
            log.info("API登出成功(无数据) - 客户端IP: {}", clientIp);
            return ReturnResult.ok();
        }

        try {
            // 构建登出请求
            AuthRequest authRequest = new AuthRequest();
            authRequest.setData(data);
            authRequest.setType(type.name());
            
            // 委托给标准登出流程处理
            LogoutAuthInformation authInformation = new LogoutAuthInformation(
                    authServerProperties,
                    data,
                    request,
                    response,
                    applicationContext,
                    loginCheck);
            
            if (!authInformation.isSuccessful()) {
                log.warn("API登出请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                        authInformation.errorMessage(), clientIp, System.currentTimeMillis() - startTime);
                return ReturnResult.illegal(authInformation.errorMessage());
            }
            
            LogoutAuthorization authorization = authInformation.authorization();
            ReturnResult<String> result = authorization.authentication();
            
            if (result.isFailure()) {
                log.warn("API登出失败: {}, 客户端IP: {}, 耗时: {}ms", 
                        result.getMsg(), clientIp, System.currentTimeMillis() - startTime);
            } else {
                log.info("API登出成功 - 客户端IP: {}, 登出类型: {}, 耗时: {}ms", 
                        clientIp, type, System.currentTimeMillis() - startTime);
            }
            
            log.debug("========== API登出请求结束 ==========");
            return result;
        } catch (Exception e) {
            log.error("API登出异常 - 客户端IP: {}, 异常: {}", clientIp, e.getMessage(), e);
            return ReturnResult.error("登出失败: " + e.getMessage());
        }
    }

    /**
     * 鉴权
     *
     * @return 鉴权
     */
    @PostMapping("/oauth")
    @ResponseBody
    @RateLimiter(value = 200, mode = "ip", message = "鉴权请求过于频繁，请稍后重试")
    @Operation(
            summary = "用户鉴权",
            description = "验证用户权限，确认用户身份和访问权限",
            responses = {
                    @ApiResponse(responseCode = "200", description = "鉴权成功"),
                    @ApiResponse(responseCode = "401", description = "鉴权失败")
            }
    )
    public ReturnResult<String> oauth(
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();
        
        log.debug("========== 鉴权请求开始 ==========");
        log.debug("客户端IP: {}", clientIp);
        
        OauthAuthInformation authInformation = new OauthAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        if(!authInformation.isSuccessful()) {
            log.warn("鉴权请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                    authInformation.errorMessage(), clientIp, System.currentTimeMillis() - startTime);
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        OauthAuthorization authorization = authInformation.authorization();
        ReturnResult<String> result = authorization.authentication();
        
        long cost = System.currentTimeMillis() - startTime;
        if(result.isFailure()) {
            log.debug("鉴权失败: {}, 客户端IP: {}, 耗时: {}ms", result.getMsg(), clientIp, cost);
            OAuthAdminController.recordRequest("OAUTH", clientIp, null, false, result.getMsg(), cost);
        } else {
            log.debug("鉴权成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
            OAuthAdminController.recordRequest("OAUTH", clientIp, null, true, "鉴权成功", cost);
        }
        log.debug("========== 鉴权请求结束 ==========");
        return result;
    }

    /**
     * 执行升级操作的方法。
     *
     * @param request  HttpServletRequest对象，用于获取客户端请求的相关信息。
     * @param response HttpServletResponse对象，用于向客户端发送响应。
     * @return ReturnResult<String> 包含升级操作结果的返回对象，其中可能包含成功与否、错误信息等。
     */
    @PostMapping("/upgrade")
    @ResponseBody
    @RateLimiter(value = 60, mode = "ip", message = "令牌刷新请求过于频繁，请稍后重试")
    @Operation(
            summary = "令牌升级",
            description = "升级用户令牌，刷新令牌有效期或权限",
            responses = {
                    @ApiResponse(responseCode = "200", description = "升级成功"),
                    @ApiResponse(responseCode = "401", description = "升级失败，无效的令牌")
            }
    )
    public ReturnResult<String> upgrade(
            @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();
        
        log.debug("========== 令牌升级请求开始 ==========");
        log.debug("客户端IP: {}", clientIp);
        
        UpgradeAuthInformation authInformation = new UpgradeAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        if(!authInformation.isSuccessful()) {
            log.warn("令牌升级请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                    authInformation.errorMessage(), clientIp, System.currentTimeMillis() - startTime);
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        UpgradeAuthorization authorization = authInformation.authorization();
        ReturnResult<String> result = authorization.authentication();
        
        long cost = System.currentTimeMillis() - startTime;
        if(result.isFailure()) {
            log.warn("令牌升级失败: {}, 客户端IP: {}, 耗时: {}ms", result.getMsg(), clientIp, cost);
            OAuthAdminController.recordRequest("UPGRADE", clientIp, null, false, result.getMsg(), cost);
        } else {
            log.info("令牌升级成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
            OAuthAdminController.recordRequest("UPGRADE", clientIp, null, true, "令牌升级成功", cost);
        }
        log.debug("========== 令牌升级请求结束 ==========");
        return result;
    }


    /**
     * 创建临时令牌
     * <p>基于源令牌创建一个短期有效的临时令牌</p>
     *
     * @param authRequest 认证请求
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @return 临时令牌结果
     */
    @PostMapping("/temporary-token")
    @ResponseBody
    @RateLimiter(value = 30, mode = "ip", message = "临时令牌创建请求过于频繁，请稍后重试")
    @Operation(
            summary = "创建临时令牌",
            description = "基于已有令牌创建短期有效的临时令牌，支持指定过期时间和允许访问的URL",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建成功"),
                    @ApiResponse(responseCode = "401", description = "源令牌无效")
            }
    )
    public ReturnResult<String> createTemporaryToken(
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 使用 TemporaryTokenAuthInformation 解析请求
        TemporaryTokenAuthInformation authInformation = new TemporaryTokenAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);

        if (!authInformation.isSuccessful()) {
            log.warn("临时令牌请求解析失败: {}, 客户端IP: {}",
                    authInformation.errorMessage(), authInformation.getClientIp());
            return ReturnResult.illegal(authInformation.errorMessage());
        }

        // 使用 TemporaryTokenAuthorization 执行创建逻辑
        TemporaryTokenAuthorization authorization = authInformation.authorization();
        return authorization.authentication();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
