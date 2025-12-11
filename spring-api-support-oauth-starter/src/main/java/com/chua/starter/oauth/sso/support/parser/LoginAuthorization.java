package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.UserAgent;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.service.SysUserLogService;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.entity.system.SysUserLog;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public final class LoginAuthorization implements Authorization {
    private final String username;
    private final String password;
    private final String loginType;
    private final String appName;
    private final Map<String, String> ext;
    private final HttpServletResponse response;
    private final AuthServerProperties authServerProperties;
    private final SysUserLogService sysUserLogService;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final LoginAuthInformation authInformation;

    public LoginAuthorization(LoginAuthInformation authInformation,
                              String username,
                              String password,
                              String loginType,
                              String appName,
                              Map<String, String> ext,
                              HttpServletResponse response,
                              AuthServerProperties authServerProperties,
                              SysUserLogService sysUserLogService,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.username = username;
        this.password = password;
        this.loginType = loginType;
        this.appName = appName;
        this.ext = ext;
        this.response = response;
        this.authServerProperties = authServerProperties;
        this.sysUserLogService = sysUserLogService;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        long startTime = System.currentTimeMillis();
        String clientAddress = MapUtils.getString(this.ext, "address");
        
        log.debug("========== 登录认证开始 ==========");
        log.debug("用户名: {}, 登录类型: {}, 应用名: {}", username, loginType, appName);
        log.debug("客户端地址: {}, 扩展参数: {}", clientAddress, ext != null ? ext.keySet() : "null");
        
        ReturnResult<LoginResult> returnResult = loginCheck.doLogin(clientAddress,
                username,
                password,
                loginType,
                ext
        );
        
        long authCost = System.currentTimeMillis() - startTime;

        if(!returnResult.isOk()) {
            log.warn("登录认证失败: 用户={}, 类型={}, 原因={}, 耗时={}ms", 
                    username, loginType, returnResult.getMsg(), authCost);
            sysUserLogService.asyncSaveUserLog(createLoginLog(returnResult, this, authCost));
            return ReturnResult.illegal(returnResult.getMsg());
        }
        
        LoginResult returnResultData = returnResult.getData();
        log.debug("登录认证通过, 用户ID: {}, Token长度: {}", 
                returnResultData.getUserResume().getUserId(),
                returnResultData.getToken() != null ? returnResultData.getToken().length() : 0);
        
        CookieUtil.set(response, authServerProperties.getCookieName(), returnResultData.getToken(), true);
        log.debug("Cookie已设置: {}", authServerProperties.getCookieName());
        
        String json = Json.toJson(returnResultData);
        sysUserLogService.asyncSaveUserLog(createLoginLog(returnResult, this, authCost));
        
        log.info("登录认证成功: 用户={}, 类型={}, 耗时={}ms", username, loginType, authCost);
        log.debug("========== 登录认证结束 ==========");
        
        return ReturnResult.ok(authInformation.encodeHex(json));
    }

    /**
     * 创建登录日志对象
     * <p>根据登录结果和请求信息构建完整的用户登录日志记录</p>
     *
     * @param returnResult 登录结果对象，包含登录成功/失败状态及详细信息
     * @param loginAuthorization 当前登录授权对象，用于获取上下文信息
     * @param cost 耗时，单位毫秒，记录登录过程耗时
     * @return 构建好的SysUserLog日志实体对象
     */
    private SysUserLog createLoginLog(ReturnResult<LoginResult> returnResult, LoginAuthorization loginAuthorization, long cost) {
        Map<String, String> stringMap = this.ext;
        SysUserLog sysUserLog = new SysUserLog();
        sysUserLog.setSysLogIp(MapUtils.getString(stringMap, "address"));
        sysUserLog.setSysLogFingerprint(MapUtils.getString(stringMap, "fingerprint"));
        sysUserLog.setSysLogUa(MapUtils.getString(stringMap, "ua"));
        UserAgent userAgent = UserAgent.parseUserAgentString(MapUtils.getString(stringMap, "ua"));
        sysUserLog.setSysLogBrowser(userAgent.getBrowser().toString());
        sysUserLog.setSysLogUsername(loginAuthorization.username);
        sysUserLog.setSysLogSystem(userAgent.getOperatingSystem().getName());
        sysUserLog.setSysLogUrl("/login");
        sysUserLog.setSysLogAppName(appName);
        sysUserLog.setSysLogLoginType(loginAuthorization.loginType);
        sysUserLog.setSysLogCost(BigDecimal.valueOf(cost));
        sysUserLog.setSysLogStatus(returnResult.isOk() ? 1 : 0);
        sysUserLog.setSysLogMessage(returnResult.getMsg());

        return sysUserLog;
    }

}
