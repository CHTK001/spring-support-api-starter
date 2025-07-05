package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.advice.AdviceView;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
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

import static com.chua.common.support.constant.CommonConstant.EMPTY;


/**
 * http
 *
 * @author CH
 */
@Controller
@Extension("http")
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
    public ReturnResult<String> login(
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            RedirectAttributes modelMap
    ) {
        LoginAuthInformation authInformation = new LoginAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        if(!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        LoginAuthorization authorization = authInformation.authorization();
        ReturnResult<String> authentication = authorization.authentication();
        if(authentication.isFailure()) {
            return ReturnResult.error(authentication.getMsg());
        }
        return authentication;
    }

    /**
     * 高性能登录方法
     */
    public ReturnResult<?> doLogin(String username, String passwd, String authType, String data, HttpServletRequest request, HttpServletResponse response, Map<String, Object> extParams) {
        // 快速获取客户端地址
        String address = RequestUtils.getIpAddress(request);

        // 执行登录检查
        if (log.isInfoEnabled()) {
            log.info("登录请求 - 地址: {}, 账号: {}, 方式: {}", address, username, authType);
        }

        ReturnResult<LoginResult> result = loginCheck.doLogin(address, username, passwd, authType, null);

        if (log.isInfoEnabled()) {
            log.info("登录结果: {} => {}", result.getCode(), result.getMsg());
        }

        return result;
    }


    /**
     * 登出
     *
     * @return 登出
     */
    @PostMapping("/logout")
    @ResponseBody
    @Operation(summary = "登出")
    public AdviceView<Object> logoutWeb(@RequestBody AuthRequest request1,
                                        HttpServletRequest request, HttpServletResponse response) {
        String data = request1.getData();
        LogoutType type = LogoutType.valueOf(request1.getType().toUpperCase());
        String address = RequestUtils.getIpAddress(request);
        String accept = request.getHeader("accept");

        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(authServerProperties.getTokenManagement());
        Cookie cookie = CookieUtil.get(request, authServerProperties.getCookieName());
        if (null != cookie) {
            tokenResolver.logout(cookie.getValue());
            CookieUtil.remove(request, response, authServerProperties.getCookieName());
        }

        String headerValue = request.getHeader(authServerProperties.getTokenName());

        if (!Strings.isNullOrEmpty(headerValue)) {
            tokenResolver.logout(headerValue);
        }


        Object attributeValue = request.getAttribute(authServerProperties.getTokenName());

        if (null != attributeValue) {
            tokenResolver.logout(attributeValue.toString());
        }

        Object uid = request.getAttribute("uid");
        if (null != uid) {
            tokenResolver.logout(uid.toString(), (LogoutType) request.getAttribute("type"));
            return new AdviceView<>(EMPTY, request);
        }
        if (null == data) {
            return new AdviceView<>(EMPTY, request);
        }
        if (!Strings.isNullOrEmpty(accept) && !accept.contains("text/html")) {
            return new AdviceView<>(logoutApi(data, type, request, response), request);
        }
        return new AdviceView<>("redirect:/login");
    }

    /**
     * 登出api
     *
     * @param data     数据
     * @param type     类型
     * @param request  请求
     * @param response 响应
     * @return 结果
     */
    private Object logoutApi(String data, LogoutType type, HttpServletRequest request, HttpServletResponse response) {
        if (Strings.isNullOrEmpty(data)) {
            return ReturnResult.ok();
        }

//        JsonObject jsonObject = Json.getJsonObject(Codec.build(encryption, serviceKey).decodeHex(data));
//        String key = jsonObject.getString(OAUTH_KEY);
//        JsonObject request1 = Json.getJsonObject(Codec.build(encryption, DigestUtils.md5Hex(key)).decodeHex(jsonObject.getString(OAUTH_VALUE)));
//        String uid = request1.getString("uid");
//        request.setAttribute("uid", uid);
//        request.setAttribute("type", type);
//        AuthRequest authRequest = new AuthRequest();
//        authRequest.setType(type.name());
//        return logoutWeb(authRequest, request, response);
        return null;
    }

    /**
     * 鉴权
     *
     * @return 鉴权
     */
    @PostMapping("/oauth")
    @ResponseBody
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
        OauthAuthInformation authInformation = new OauthAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        if(!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        OauthAuthorization authorization = authInformation.authorization();
        return authorization.authentication();
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
        UpgradeAuthInformation authInformation = new UpgradeAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);
        if(!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }
        UpgradeAuthorization authorization = authInformation.authorization();
        return authorization.authentication();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
