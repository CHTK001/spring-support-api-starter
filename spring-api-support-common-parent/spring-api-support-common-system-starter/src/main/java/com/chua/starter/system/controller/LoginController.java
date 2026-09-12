package com.chua.starter.system.controller;

import com.chua.common.support.core.constant.Action;
import com.chua.task.support.task.debounce.Debounce;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.UserLog;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.protocol.AbstractProtocol;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.pojo.RefreshToken;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.UserInfoVO;
import com.chua.starter.system.pojo.menu.SysMiniMenuVO;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.service.SysUserService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.starter.common.support.logger.AbstractLoggerPointcutAdvisor.X_REQ_FINGERPRINT;

/**
* 登录
*
* @author CH
* @since 2024/9/12
 */
@RestController
@Slf4j
@Api(tags = "登录接口")
@Tag(name = "登录接口")
@Primary
@RequestMapping("/v2/user")
@RequiredArgsConstructor
public class LoginController {

    final SysLoginService sysLoginService; // syslogin服务
    final SysUserService sysUserService; // sys用户服务

    /**
    * 登录
    *
    * @param loginData     登录信息
    * @param request       请求
    * @param response      响应
    * @param bindingResult 参数处理
    * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    @SysLog
    @UserLog(module = "LOGIN",
            name = "用户登录",
            action = Action.LOGIN,
            loginType = "WEB",
            content = "#{#now} #{#args[0].username} 登录系统, 登录返回[状态:#{#result.code}(#{#result.msg})]")
    @Debounce(key = "#loginData.username #loginData.loginType")
    public ReturnResult<LoginResult> login(@Valid @RequestBody LoginData loginData,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestHeader(name = X_REQ_FINGERPRINT, required = false) String fingerprint,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.login(loginData, request, response, fingerprint);
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh-token")
    @SysLog
    @TokenForIgnore
    public ReturnResult<LoginResult> refreshToken(@Valid @RequestBody RefreshToken refreshToken,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.refresh(refreshToken, request, response);
    }

    @Operation(summary = "注销")
    @UserLog(module = "LOGIN", name = "用户登出", action = Action.LOGOUT, content = "#{#now} #{#args[0].username} 退出系统, 登录返回[状态:#{#result.code}(#{#result.msg})]")
    @DeleteMapping("/logout")
    @SysLog
    public ReturnResult<String> logout(@UserValue("token") String uid, String loginType,
                                       @RequestHeader(name = "x-oauth-logout-type", required = false) String logoutTypeHeader,
                                       HttpServletRequest request, HttpServletResponse response) {
        // @UserValue("token") 在白名单请求中无法从用户上下文获取，fallback 到 x-oauth-token header
        if (StringUtils.isEmpty(uid)) {
            uid = request.getHeader("x-oauth-token");
        }
 // 降级 到 授权 头部
        if (StringUtils.isEmpty(uid)) {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                uid = authHeader.substring(7).trim();
            }
        }
 // 降级 到 Cookie
        if (StringUtils.isEmpty(uid)) {
            uid = CookieUtil.getValue(request, "x-oauth-cookie");
        }
        if (StringUtils.isEmpty(uid)) {
            return ReturnResult.ok("注销成功");
        }

 // 支持通过 x-OAuth-logout-类型 头指定登出类型（默认单设备登出）
        LogoutType logoutType = LogoutType.LOGOUT;
        if (StringUtils.isNotBlank(logoutTypeHeader)) {
            try {
                logoutType = LogoutType.valueOf(logoutTypeHeader);
            } catch (IllegalArgumentException ignored) {
            }
        }

        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult accessToken = clientExecute.logout(uid, loginType, logoutType);
        if (null == accessToken) {
            return ReturnResult.error("退出失败请稍后重试");
        }

        if (accessToken.getCode() != 200) {
            return ReturnResult.error(accessToken.getMessage());
        }

 // 清除本地 Guava 缓存，防止登出后旧 令牌 仍命中缓存
        AbstractProtocol.invalidateCache(uid);
        if (logoutType == LogoutType.LOGOUT_ALL) {
            AbstractProtocol.invalidateAllCache();
        }
        CookieUtil.remove(request, response, "x-oauth-cookie");
        return ReturnResult.ok("注销成功");
    }

    /**
    * 获取当前登录用户信息
    *
    * @return 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public ReturnResult<UserInfoVO> getUserLoginInfo() {
        return ReturnResult.ok(sysLoginService.getUserLoginInfo());
    }

    /**
    * 获取当前登录用户菜单
    *
    * @param roles 用户角色集合
    * @return 菜单列表
    * @author CH
    * @since 2024/12/10
    * @版本 1.0
     */
    @Operation(summary = "获取当前登录用户菜单")
    @GetMapping("/menu")
    public ReturnResult<List<SysMenuVO>> getUserMenu(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @RequestParam(required = false, defaultValue = "0") Integer sysMenuEngine) {
        return sysUserService.getUserMenu(roles, sysMenuEngine);
    }

    @Operation(summary = "获取当前登录用户小程序菜单")
    @GetMapping("/mini-menu")
    public ReturnResult<List<SysMiniMenuVO>> getUserMiniMenu(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysUserService.getUserMiniMenu(roles);
    }

    /**
    * 创建临时令牌
    *
    * @param body 请求体，可包含 expire时间（秒，默认10，最大3600）和 allowedurls（逗号分隔的URL列表）
    * @return 临时令牌
     */
    @Operation(summary = "创建临时令牌")
    @PostMapping("/temporary-token")
    @SysLog
    public ReturnResult<String> createTemporaryToken(
            @RequestBody(required = false) Map<String, Object> body) {
        int expireTime = 10;
        List<String> allowedUrls = null;

        if (body != null) {
            Object expireObj = body.get("expireTime");
            if (expireObj instanceof Number n) {
                expireTime = n.intValue();
            } else if (expireObj instanceof String s) {
                try { expireTime = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
            }
            Object urlsObj = body.get("allowedUrls");
            if (urlsObj instanceof List<?> list) {
                allowedUrls = list.stream().map(Object::toString).toList();
            } else if (urlsObj instanceof String s && StringUtils.isNotBlank(s)) {
                allowedUrls = List.of(s.split(","));
            }
        }

        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult result = clientExecute.createTemporaryToken(expireTime, allowedUrls);
        if (result == null) {
            return ReturnResult.error("创建临时令牌失败");
        }
        if (result.getCode() != 200) {
            return ReturnResult.error(result.getMessage());
        }
        return ReturnResult.ok(result.getToken());
    }

    /**
    * 令牌 升级（版本 刷新用户信息 / 时间戳 续期）
    *
    * @param upgradeType 升级类型：版本（刷新用户信息）/ 时间戳（续期）
    * @param token       当前 access令牌（从 头部 获取）
    * @return 升级结果，包含新 令牌 信息
     */
    @Operation(summary = "Token升级（VERSION刷新用户信息/TIMESTAMP续期）")
    @PostMapping("/upgrade")
    @SysLog
    public ReturnResult<LoginResult> upgrade(
            @RequestHeader(name = "x-oauth-upgrade-type", defaultValue = "VERSION") String upgradeType,
            @UserValue("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (StringUtils.isEmpty(token)) {
            token = request.getHeader("x-oauth-token");
        }
        if (StringUtils.isEmpty(token)) {
            return ReturnResult.error("未登录");
        }

        UpgradeType type;
        try {
            type = UpgradeType.valueOf(upgradeType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ReturnResult.error("不支持的升级类型: " + upgradeType);
        }

        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        com.chua.starter.oauth.client.support.user.LoginResult upgrade = clientExecute.upgrade(token, type);
        if (upgrade == null) {
            return ReturnResult.error("升级失败");
        }

        // 清除本地缓存，确保下次请求重新从服务端获取最新用户信息
        AbstractProtocol.invalidateCache(token);

        LoginResult loginResult = LoginResult.builder()
                .userInfo(upgrade.getUserResume() != null
                        ? sysLoginService.buildUserInfo(upgrade.getUserResume())
                        : sysLoginService.getUserLoginInfo(upgrade.getToken()))
                .tokenType("Bearer")
                .accessToken(upgrade.getToken())
                .refreshToken(upgrade.getRefreshToken())
                .build();
        return ReturnResult.ok(loginResult);
    }
}
