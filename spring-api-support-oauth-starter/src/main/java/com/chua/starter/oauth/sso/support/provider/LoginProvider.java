package com.chua.starter.oauth.sso.support.provider;

import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.AccessSecret;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.advice.AdviceView;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
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
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.common.support.lang.code.ReturnCode.OK;
import static com.chua.common.support.lang.code.ReturnCode.RESULT_ACCESS_UNAUTHORIZED;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.*;

/**
 * 登录
 *
 * @author CH
 */
@Controller
@Conditional(OnBeanCondition.class)
@SuppressWarnings("ALL")
@Slf4j
@Tag(name = "登录")
@RequiredArgsConstructor
@RequestMapping("${plugin.auth.server.context-path:}")
public class LoginProvider implements InitializingBean {
    final AuthServerProperties authServerProperties;
    final LoginCheck loginCheck;
    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;
    private String encryption;
    private String serviceKey;

    /**
     * 无权限页面
     *
     * @param url      原始地址
     * @param modelMap 参数
     * @return 登录页
     */
    @GetMapping("/oauth-page")
    @Operation(
            summary = "无权限页面",
            description = "当用户没有权限访问某个资源时，重定向到此页面",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功返回无权限页面")
            }
    )
    public String oauthPage(
            @Parameter(description = "重定向URL", required = false) @RequestParam(value = "redirect_url", required = false) String url,
            Model modelMap,
            HttpServletRequest request) {
        return "oauth/oauth-page";
    }

    /**
     * 登出
     *
     * @return 登出
     */
    @Operation(
            summary = "登出",
            description = "退出用户登录状态，清除认证信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "登出成功")
            }
    )
    public ReturnResult<UserResult> logout(
            HttpServletRequest request, HttpServletResponse response) {
        AuthRequest request1 = new AuthRequest();
        request1.setType(LogoutType.NONE.name());
        logoutWeb(request1, request, response);
        return ReturnResult.ok();
    }


    /**
     * 登出
     *
     * @return 登出
     */
    @PostMapping("/logout")
    @ResponseBody
    @Operation(summary = "登出")
    public AdviceView logoutWeb(@RequestBody AuthRequest request1,
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
            return new AdviceView(EMPTY, request);
        }
        if (null == data) {
            return new AdviceView(EMPTY, request);
        }
        if (!Strings.isNullOrEmpty(accept) && !accept.contains("text/html")) {
            return new AdviceView(logoutApi(data, type, request, response), request);
        }
        return new AdviceView("redirect:/login");
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

        JsonObject jsonObject = Json.getJsonObject(Codec.build(encryption, serviceKey).decodeHex(data));
        String key = jsonObject.getString(OAUTH_KEY);
        JsonObject request1 = Json.getJsonObject(Codec.build(encryption, DigestUtils.md5Hex(key)).decodeHex(jsonObject.getString(OAUTH_VALUE)));
        String uid = request1.getString("uid");
        request.setAttribute("uid", uid);
        request.setAttribute("type", type);
        AuthRequest authRequest = new AuthRequest();
        authRequest.setType(type.name());
        return logoutWeb(authRequest, request, response);
    }

    /**
     * 登录
     *
     * @param username 账号
     * @param passwd   密码
     * @param authType 类型
     * @param data     请求数据
     * @param request  请求
     * @param response
     * @return 登录页
     */
    public ReturnResult<?> doLogin(String username, String passwd, String authType, String data, HttpServletRequest request, HttpServletResponse response, Map<String, Object> extParams) {
        String address = RequestUtils.getIpAddress(request);
        AccessSecret accessSecret = getAkSk(data);
        if (StringUtils.isEmpty(passwd)) {
            passwd = accessSecret.getPassword();
        }

        // 如果传入了额外参数，优先使用传入的
        Object ext = (extParams != null) ? extParams : ((accessSecret != null) ? accessSecret.getExt() : null);

        if (null != accessSecret && null != accessSecret.getExt()) {
            address = MapUtils.getString(accessSecret.getExt(), "address", address);
        }

        log.info("登录请求地址: {}, 账号:{}, 登陆方式:{}", address, username, authType);
        ReturnResult result = loginCheck.doLogin(address, username, passwd, authType, ext);
        log.info("登录结果: {} => {}", result.getCode(), result.getMsg());
        if (!result.getCode().equals(OK.getCode())) {
            Object data1 = result.getData();
            if (null != data1) {
                request.setAttribute(authServerProperties.getTokenName(), ((LoginResult) data1).getToken());
            }
            logout(request, response);
            return ReturnResult.newBuilder().code(result.getCode()).data(result.getData()).msg(result.getMsg()).build();
        }

        if (!checkAkSk(result, data, accessSecret)) {
            Object data1 = result.getData();
            if (null != data1) {
                request.setAttribute(authServerProperties.getTokenName(), ((LoginResult) data1).getToken());
            }
            logout(request, response);
            return ReturnResult.newBuilder().code(RESULT_ACCESS_UNAUTHORIZED.getCode()).msg("ak/sk无效").build();
        }
        if (authServerProperties.isOpenReturnCheckAkSk()) {
            doEncodeReturnValue(result, accessSecret);
        }

        return result;

    }

    /**
     * 加密返回结果
     *
     * @param resultReturnResult 结果
     * @param accessSecret       AK/SK
     */
    private void doEncodeReturnValue(ReturnResult resultReturnResult, AccessSecret accessSecret) {
        if (!authServerProperties.isOpenReturnCheckAkSk()) {
            return;
        }

        LoginResult loginResult = (LoginResult) resultReturnResult.getData();
        loginResult.getUserResult().setAccessSecret(new AccessSecret());

        String json = Json.toJson(loginResult);
        resultReturnResult.setData(Codec.build(encryption, accessSecret.getUKey()).encodeHex(json));
    }

    private AccessSecret getAkSk(String data) {
        return null != data ? createAccessSecret(data) : null;
    }

    /**
     * 检验ak/sk合法
     *
     * @param resultReturnResult 数据
     * @param data               请求数据
     * @param accessSecret1      AK/SK
     * @return 合法性
     */
    private boolean checkAkSk(ReturnResult<Object> resultReturnResult, String data, AccessSecret accessSecret1) {
        if (!authServerProperties.isOpenCheckAkSk()) {
            return doEncode(true, resultReturnResult, null);
        }

        LoginResult loginResult = (LoginResult) resultReturnResult.getData();

        AccessSecret accessSecret = loginResult.getUserResult().getAccessSecret();
        if (null == accessSecret) {
            return false;
        }

        try {
            AccessSecret requestAccessSecret = accessSecret1;
            return doEncode(accessSecret.getAccessKey().equals(requestAccessSecret.getAccessKey())
                    && accessSecret.getSecretKey().equals(requestAccessSecret.getSecretKey()), resultReturnResult, requestAccessSecret.getUKey());
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * 数据加密
     *
     * @param status             状态
     * @param resultReturnResult 结果
     * @param uKey               ukey
     * @return 加密
     */
    private boolean doEncode(boolean status, ReturnResult<Object> resultReturnResult, String uKey) {
        if (!authServerProperties.isOpenCheckAkSk()) {
            return true;
        }

        return status;
    }

    /**
     * 解析请求
     *
     * @param data 数据
     * @return 请求
     */
    private AccessSecret createAccessSecret(String data) {
        String decode1 = Codec.build(encryption, serviceKey).decodeHex(data);
        JsonObject jsonObject = Json.getJsonObject(decode1);
        String key = jsonObject.getString(OAUTH_KEY);
        String authKey = jsonObject.getString("x-oauth-uid");
        String value = jsonObject.getString(OAUTH_VALUE);
        String request = Codec.build(encryption, DigestUtils.md5Hex(key)).decodeHex(value);
        JsonObject jsonObject2 = Json.getJsonObject(request);

        return new AccessSecret(jsonObject2.getString(ACCESS_KEY),
                jsonObject2.getString(SECRET_KEY),
                authKey,
                jsonObject.getString("password"),
                jsonObject.getObject("ext", Map.class)
        );
    }

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
    public AdviceView doWebLogin(
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
            return new AdviceView(result, request);
        }

        if (!DigestUtils.sha512Hex(username + passwd + code).equals(data)) {
            modelMap.addFlashAttribute("msg", "登录失败");
            try {
                return new AdviceView("redirect:" + contextPath + "/login?redirect_url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new AdviceView("redirect:" + contextPath + "/login?redirect_url=" + url);
            }
        }
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute("KAPTCHA_SESSION_KEY")).orElse("").toString();
        if (Strings.isNullOrEmpty(code) || !code.equalsIgnoreCase(sessionKey)) {
            modelMap.addFlashAttribute("msg", "校验码错误");
            try {
                return new AdviceView("redirect:" + contextPath + "/login?redirect_url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new AdviceView("redirect:" + contextPath + "/login?redirect_url=" + url);
            }
        }

        boolean ifRem = "on".equals(ifRemember);
        ReturnResult<LoginResult> result = loginCheck.doLogin(address, username, passwd, type, null);

        if (result.getCode().equals(500)) {
            redirectAttributes.addFlashAttribute("redirect_url", url);
            return new AdviceView("redirect:" + contextPath + "/login");
        }

        if (result.getCode().equals(403)) {
            modelMap.addFlashAttribute("msg", Optional.ofNullable(result.getMsg()).orElse("当前登陆端限制登录, 需要注册密钥"));
            try {
                return new AdviceView("redirect:" + contextPath + "/login?redirect_url=" + URLEncoder.encode(url, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new AdviceView("redirect:" + contextPath + "/login?redirect_url=" + url);
            }
        }

        LoginResult loginResult = result.getData();
        CookieUtil.set(response, authServerProperties.getCookieName(), loginResult.getToken(), ifRem);
        return new AdviceView("redirect:" + url);
    }


    /**
     * 登录
     *
     * @param url      原始地址
     * @param modelMap 参数
     * @return 登录页
     */
    @GetMapping("/login")
    @Operation(summary = "登录")
    public String login(@RequestParam(value = "redirect_url", required = false) String url, Model modelMap, HttpServletRequest request) {
        String address = RequestUtils.getIpAddress(request);

        if (null == url) {
            url = request.getParameter("redirect_url");
        }

        if (null == url) {
            return "oauth/login";
        }

        try {
            modelMap.addAttribute("redirect_url", URLDecoder.decode(URLDecoder.decode(url, "UTF-8"), "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
        }
        Cookie[] cookies = request.getCookies();
        String token = request.getHeader(authServerProperties.getTokenName());
        String key = UUID.randomUUID().toString();
        if (null != cookies || Strings.isNullOrEmpty(token)) {
            ReturnResult<String> returnResult = oauthResult(cookies, token, key, address);
            if (returnResult.getCode().equals(200)) {
                return "redirect:" + url;
            }
        }
        return "oauth/login";
    }


    /**
     * 认证
     *
     * @param cookies cookie
     * @param token   token
     * @param key     key
     * @param address 地址
     * @return ReturnResult
     */
    private ReturnResult<String> oauthResult(Cookie[] cookies, String token, String key, String address) {

        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(authServerProperties.getTokenManagement());
        ReturnResult<UserResult> oauth = tokenResolver.resolve(cookies, token);
        UserResult data1 = oauth.getData();

        String rs = null;
        if (null != data1) {
            data1.setAddress(null);
            Codec codec = Codec.build(encryption, key);
            rs = codec.encodeHex(Json.toJson(data1));
        }

        return ReturnResult.of(oauth.getCode(), rs, oauth.getMsg());
    }

    /**
     * 创建临时令牌
     * <p>
     * 基于当前用户的有效令牌创建一个短期临时令牌，用于特定场景下的短期授权访问。
     * 临时令牌继承原令牌的所有权限，但有效期更短，适用于临时授权和敏感操作场景。
     * </p>
     *
     * @param token      当前用户的有效令牌，作为principal参数传递给临时令牌服务
     * @param expireTime 临时令牌的过期时间（秒），默认10秒
     * @param request    HTTP请求
     * @param response   HTTP响应
     * @return 包含临时令牌的登录结果
     */
    @PostMapping("/temporary-token")
    @ResponseBody
    @Operation(
            summary = "创建临时令牌",
            description = "基于当前用户令牌创建一个短期临时令牌，默认有效期10秒",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建成功，返回临时令牌"),
                    @ApiResponse(responseCode = "401", description = "创建失败，无效的令牌")
            }
    )
    public ReturnResult<?> createTemporaryToken(
            @Parameter(description = "当前用户的有效令牌", required = true) @RequestParam("token") String token,
            @Parameter(description = "临时令牌的过期时间（秒）", required = false) @RequestParam(value = "expireTime", required = false, defaultValue = "10") Long expireTime,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 获取客户端IP地址
        String address = RequestUtils.getIpAddress(request);

        // 准备扩展参数，包含过期时间
        Map<String, Object> ext = new HashMap<>();
        ext.put("expireTime", expireTime);

        // 调用登录处理方法，其中token作为principal参数传递给TemporaryTokenService
        ReturnResult<?> result = doLogin(token, null, "TEMPORARY", null, request, response, ext);

        // 记录临时令牌创建结果
        log.info("临时令牌创建: 地址={}, 过期时间={}秒, 结果={}", address, expireTime, result.getCode());

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.encryption = authServerProperties.getEncryption();
        this.serviceKey = authServerProperties.getServiceKey();
    }
}
