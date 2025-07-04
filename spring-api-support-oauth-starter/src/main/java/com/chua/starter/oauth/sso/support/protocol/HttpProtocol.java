package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.AccessSecret;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.support.advice.AdviceView;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.AuthInformation;
import com.chua.starter.oauth.sso.support.parser.Authorization;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.common.support.lang.code.ReturnCode.OK;
import static com.chua.common.support.lang.code.ReturnCode.RESULT_ACCESS_UNAUTHORIZED;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.OAUTH_KEY;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.OAUTH_VALUE;


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
    @PostMapping("/doLogin")
    public AdviceView<Object> doWebLogin(
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            RedirectAttributes modelMap
    ) {
        String username = authRequest.getUsername(),
                passwd = authRequest.getPasswd(),
                code = authRequest.getCode(),
                type = authRequest.getType(),
                data = authRequest.getData(),
                url = authRequest.getRedirect_url(),
                mode = authRequest.getMode(),
                ifRemember = authRequest.getIfRemember();
        String address = RequestUtils.getIpAddress(request);
        String accept = request.getHeader("accept");
        if (null != accept && !accept.contains("text/html")) {
            ReturnResult<?> result = doLogin(username, passwd, type, data, request, response, null);
            return new AdviceView<>(result, request);
        }

        if (!DigestUtils.sha512Hex(username + passwd + code).equals(data)) {
            modelMap.addFlashAttribute("msg", "登录失败");
            try {
                return new AdviceView<>("redirect:" + contextPath + "/login?redirect_url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new AdviceView<>("redirect:" + contextPath + "/login?redirect_url=" + url);
            }
        }
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute("KAPTCHA_SESSION_KEY")).orElse("").toString();
        if (Strings.isNullOrEmpty(code) || !code.equalsIgnoreCase(sessionKey)) {
            modelMap.addFlashAttribute("msg", "校验码错误");
            try {
                return new AdviceView<>("redirect:" + contextPath + "/login?redirect_url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new AdviceView<>("redirect:" + contextPath + "/login?redirect_url=" + url);
            }
        }

        boolean ifRem = "on".equals(ifRemember);
        ReturnResult<LoginResult> result = loginCheck.doLogin(address, username, passwd, type, null);

        if (result.getCode().equals("500")) {
            redirectAttributes.addFlashAttribute("redirect_url", url);
            return new AdviceView<>("redirect:" + contextPath + "/login");
        }

        if (result.getCode().equals("403")) {
            modelMap.addFlashAttribute("msg", Optional.ofNullable(result.getMsg()).orElse("当前登陆端限制登录, 需要注册密钥"));
            try {
                return new AdviceView<>("redirect:" + contextPath + "/login?redirect_url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new AdviceView<>("redirect:" + contextPath + "/login?redirect_url=" + url);
            }
        }

        LoginResult loginResult = result.getData();
        CookieUtil.set(response, authServerProperties.getCookieName(), loginResult.getToken(), ifRem);
        return new AdviceView<>("redirect:" + url);
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
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest request1,
            HttpServletRequest request,
            HttpServletResponse response) {
        String data = request1.getData();
        AuthInformation authInformation = new AuthInformation(request1.isEncipher(), data, request, authServerProperties, applicationContext, loginCheck);
        Authorization authorization = authInformation.resolve();
        String address = RequestUtils.getIpAddress(request);

        if (!authorization.hasKey()) {
            return ReturnResult.noAuth();
        }

        if (!authorization.hasTokenOrCookie()) {
            return ReturnResult.noAuth();
        }

        ReturnResult<String> authentication = authorization.authentication(request1.isEncipher());
        if (!OK.getCode().equals(authentication.getCode())) {
            return ReturnResult.noAuth();
        }

        return authentication;
    }

    /**
     * 执行升级操作的方法。
     *
     * @param data     包含升级相关数据的字符串，具体格式和内容根据业务逻辑而定。
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
            @Parameter(description = "包含升级相关的加密数据", required = true) @RequestParam("data") String data,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthInformation authInformation = new AuthInformation(true, data, request, authServerProperties, applicationContext, loginCheck);
        Authorization authorization = authInformation.resolve();
        String address = RequestUtils.getIpAddress(request);

        if (!authorization.hasRefreshToken()) {
            return ReturnResult.noAuth();
        }

        if (!authorization.hasKey()) {
            return ReturnResult.noAuth();
        }

        if (!authorization.hasTokenOrCookie()) {
            return ReturnResult.noAuth();
        }

        ReturnResult<String> authentication = authorization.upgrade(address, authServerProperties.getCookieName());
        if (!OK.getCode().equals(authentication.getCode())) {
            return ReturnResult.noAuth();
        }


        return authentication;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
