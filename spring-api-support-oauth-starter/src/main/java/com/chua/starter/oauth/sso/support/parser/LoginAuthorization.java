package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.net.UserAgent;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.IpUtils;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.system.service.SysUserLogService;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.core.AuthCoreService;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.common.support.service.IptablesService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 鉴权信息
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
public final class LoginAuthorization implements Authorization {
    private final String username; // 用户名
    private final String password; // 密码
    private final String loginType; // login类型
    private final String appName; // app名称
    private final Map<String, String> ext; // ext
    private final HttpServletResponse response; // 响应
    private final AuthServerProperties authServerProperties; // 认证服务端属性
    private final SysUserLogService sysUserLogService; // sys用户日志服务
    private final IptablesService iptablesService; // iptables服务
    final ApplicationContext applicationContext; // application上下文
    private final AuthCoreService authCoreService; // 认证核心服务
    private final LoginAuthInformation authInformation; // 认证信息

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
                              AuthCoreService authCoreService) {
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
        this.authCoreService = authCoreService;
 // 可选注入 iptables服务（用于 IP 归属地解析）
        IptablesService iptSvc = null;
        try {
            iptSvc = applicationContext.getBean(IptablesService.class);
        } catch (Exception ignored) {}
        this.iptablesService = iptSvc;
    }

    @Override
    public ReturnResult<String> authentication() {
        long startTime = System.currentTimeMillis();
        String traceId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        // 优先使用传输层/可信代理解析出的地址；ext.address 由调用方自填，仅作兜底
        String clientAddress = authInformation.resolveTrustedClientAddress();
        if (com.chua.common.support.utils.StringUtils.isBlank(clientAddress)) {
            clientAddress = MapUtils.getString(this.ext, "address");
        }

        log.info("[LOGIN][traceId={}] 开始: user={}, type={}, app={}, ip={}",
                traceId, username, loginType, appName, clientAddress);

        ReturnResult<LoginResult> returnResult = authCoreService.login(clientAddress,
                username,
                password,
                loginType,
                ext
        );

        long authCost = System.currentTimeMillis() - startTime;

        if (!returnResult.isOk()) {
            log.warn("[LOGIN][traceId={}] 失败: user={}, type={}, 原因={}, 耗时={}ms",
                    traceId, username, loginType, returnResult.getMsg(), authCost);
            sysUserLogService.asyncSaveUserLog(createLoginLog(returnResult, this, authCost));
            return ReturnResult.illegal(returnResult.getMsg());
        }

        LoginResult returnResultData = returnResult.getData();
        String userId = returnResultData.getUserResume() != null ? returnResultData.getUserResume().getUserId() : "?";

        log.info("[LOGIN][traceId={}] 成功: user={}, userId={}, type={}, 耗时={}ms",
                traceId, username, userId, loginType, authCost);

        CookieUtil.set(response, authServerProperties.getCookieName(), returnResultData.getToken(), true);

        String json = Json.toJson(returnResultData);
        sysUserLogService.asyncSaveUserLog(createLoginLog(returnResult, this, authCost));

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
        String ip = MapUtils.getString(stringMap, "address");
        sysUserLog.setSysLogIp(ip);
        String ua = MapUtils.getString(stringMap, "ua");
        sysUserLog.setSysLogUa(ua);

 // 指纹：优先用客户端上报的，降级 到 UA 生成的伪指纹
        String fingerprint = MapUtils.getString(stringMap, "fingerprint");
        if (fingerprint == null || fingerprint.isBlank()) {
            fingerprint = IpUtils.fingerprintFromUserAgent(ua);
        }
        sysUserLog.setSysLogFingerprint(fingerprint);

        UserAgent userAgent = UserAgent.parseUserAgentString(ua);
        sysUserLog.setSysLogBrowser(userAgent.getBrowser().toString());
        sysUserLog.setSysLogUsername(loginAuthorization.username);
        sysUserLog.setSysLogSystem(userAgent.getOperatingSystem().getName());
        sysUserLog.setSysLogUrl("/login");
        sysUserLog.setSysLogFrom(returnResult.isOk() ? "LOGIN" : "LOGIN_FAIL");
        sysUserLog.setSysLogAppName(appName);
        sysUserLog.setSysLogLoginType(loginAuthorization.loginType);
        sysUserLog.setSysLogCost(BigDecimal.valueOf(cost));
        sysUserLog.setSysLogStatus(returnResult.isOk() ? 1 : 0);
        sysUserLog.setSysLogMessage(returnResult.getMsg());

        if (returnResult.isOk() && returnResult.getData() != null && returnResult.getData().getUserResume() != null) {
            String userId = returnResult.getData().getUserResume().getUserId();
            if (userId != null) {
                try {
                    sysUserLog.setCreateBy(Integer.parseInt(userId));
                } catch (NumberFormatException ignored) {
                }
            }
            Map<String, Object> userExt = returnResult.getData().getUserResume().getExt();
            if (userExt != null) {
                Object createBy = userExt.get("sysUserId");
                if (createBy != null) {
                    try {
                        sysUserLog.setCreateBy(Integer.parseInt(createBy.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Object sysDeptId = userExt.get("sysDeptId");
                if (sysDeptId != null) {
                    try {
                        sysUserLog.setSysDeptId(Integer.parseInt(sysDeptId.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // IP 归属地解析
        if (iptablesService != null && ip != null && !ip.isBlank()) {
            try {
                com.chua.common.support.lang.code.ReturnResult<com.chua.common.support.network.ip.GeoCity> geoResult =
                        iptablesService.transferAddress(ip);
                if (geoResult != null && geoResult.isOk() && geoResult.getData() != null) {
                    com.chua.common.support.network.ip.GeoCity city = geoResult.getData();
                    sysUserLog.setSysLogAddress(city.toString());
                    sysUserLog.setSysLogIsp(city.getIsp());
                }
            } catch (Exception ignored) {}
        }

        return sysUserLog;
    }

}
