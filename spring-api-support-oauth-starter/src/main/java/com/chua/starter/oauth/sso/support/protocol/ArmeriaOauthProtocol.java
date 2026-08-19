package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.base.collection.Options;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.protocol.support.network.protocol.Protocol;
import com.chua.protocol.support.network.protocol.ProtocolSetting;
import com.chua.protocol.support.network.protocol.request.ServletRequest;
import com.chua.protocol.support.network.protocol.request.ServletResponse;
import com.chua.protocol.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.text.json.Json;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.admin.RateLimitConfig;
import com.chua.starter.oauth.sso.support.admin.RateLimitManager;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.ProtocolServerAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Armeria 协议
 *
 * @author CH
 */
@Component
@Extension("armeria")
@Spi("armeria")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "提供Armeria协议的认证服务接口")
public class ArmeriaOauthProtocol implements OauthProtocol, InitializingBean {

    final AuthServerProperties authServerProperties;

    final LoginCheck loginCheck;

    final RateLimitManager rateLimitManager;

    final ApplicationContext applicationContext;

    @Value("${server.servlet.context-path:}")
    /** 上下文路径 */
    private String contextPath;

    /** 协议服务器 */
    private ProtocolServer protocolServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检查是否启用 armeria 协议
        if (!authServerProperties.getProtocols().contains("armeria")) {
            log.info("[OAuth][Armeria] 协议未启用，跳过 ProtocolServer 启动");
            return;
        }
        var protocolSetting = ProtocolSetting.builder()
                .protocol("armeria")
                .port(authServerProperties.getArmeriaPort() != null ? authServerProperties.getArmeriaPort() : 19180)
                // 统一由 Armeria 底层根据 contextPath 处理路由前缀，业务侧仅注册相对路径
                .options(Options.of("contextPath", contextPath))
                .build();
        try {
            var protocol = ServiceProvider.of(Protocol.class).getNewExtension("armeria", protocolSetting);
            if (protocol == null) {
                log.warn("[OAuth][Armeria] 未找到 Armeria Protocol 实现，跳过 ProtocolServer 启动");
                return;
            }


            protocolServer = protocol.createServer(protocolSetting);
            registerProtocolServerRoutes(protocolServer);
            protocolServer.start();

            log.info("[OAuth][Armeria] ProtocolServer 启动成功，端口: {}", protocolSetting.getPort());
        } catch (Exception e) {
            log.error("[OAuth][Armeria] ProtocolServer 启动失败", e);
        }
    }

    /**
     * 注册 ProtocolServer 路由
     *
     * @param server ProtocolServer 实例
     */
    private void registerProtocolServerRoutes(ProtocolServer server) {
        // 登录端点
        server.post("/login", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new LoginAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 登录处理异常", e);
                writeResponse(response, ReturnResult.error("登录失败: " + e.getMessage()));
            }
        });

        // 登出端点
        server.post("/logout", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new LogoutAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 登出处理异常", e);
                writeResponse(response, ReturnResult.error("登出失败: " + e.getMessage()));
            }
        });

        // 鉴权端点，兼容历史 /oauth 客户端
        server.post("/verify", this::handleVerifyRequest);
        server.post("/oauth", this::handleVerifyRequest);

        // 升级端点
        server.post("/upgrade", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new UpgradeAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 令牌升级处理异常", e);
                writeResponse(response, ReturnResult.error("令牌升级失败: " + e.getMessage()));
            }
        });

        // 管理控制台 - GET 返回 HTML
        server.get("/admin", (request, response) -> {
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.setStatus(200);
                response.setBodyString(buildAdminHtml());
            } catch (Exception e) {
                log.error("[OAuth][Admin] 管理页面异常", e);
                writeResponse(response, ReturnResult.error("管理页面加载失败"));
            }
        });

        // 管理控制台 - 获取状态
        server.get("/admin/status", (request, response) -> {
            try {
                java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
                status.put("signVerify", authServerProperties.isSignVerify());
                status.put("online", authServerProperties.getOnline().name());
                status.put("maxOnlineCount", authServerProperties.getMaxOnlineCount());
                status.put("expire", authServerProperties.getExpire());
                status.put("refreshExpire", authServerProperties.getRefreshExpire());
                status.put("renew", authServerProperties.isRenew());
                status.put("refreshTokenRotation", authServerProperties.isRefreshTokenRotation());
                status.put("protocols", authServerProperties.getProtocols());
                status.put("rateLimit", rateLimitManager.getStatus());
                writeResponse(response, ReturnResult.ok(status));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 管理控制台 - 切换签名校验
        server.post("/admin/sign-verify", (request, response) -> {
            try {
                String body = extractBody(request);
                boolean enabled = body.contains("true");
                // 也支持 query param
                String query = request.getQueryString();
                if (query != null && query.contains("enabled=")) {
                    enabled = query.contains("enabled=true");
                }
                authServerProperties.setSignVerify(enabled);
                log.info("[Admin] 签名校验已{}", enabled ? "开启" : "关闭");
                writeResponse(response, ReturnResult.ok(enabled ? "签名校验已开启" : "签名校验已关闭"));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 管理控制台 - 切换在线模式
        server.post("/admin/online-mode", (request, response) -> {
            try {
                String body = extractBody(request);
                String query = request.getQueryString();
                String mode = "MULTIPLE";
                int maxCount = 3;
                if (query != null) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("mode=")) mode = param.substring(5);
                        if (param.startsWith("maxCount=")) maxCount = Integer.parseInt(param.substring(9));
                    }
                }
                com.chua.starter.oauth.sso.support.properties.AuthServerProperties.Online onlineMode =
                    com.chua.starter.oauth.sso.support.properties.AuthServerProperties.Online.valueOf(mode.toUpperCase());
                authServerProperties.setOnline(onlineMode);
                if (onlineMode == com.chua.starter.oauth.sso.support.properties.AuthServerProperties.Online.LIMIT) {
                    authServerProperties.setMaxOnlineCount(maxCount);
                }
                log.info("[Admin] 在线模式已切换为: {}", onlineMode);
                writeResponse(response, ReturnResult.ok("在线模式已切换为: " + onlineMode));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 管理控制台 - 限流配置
        server.post("/admin/rate-limit", (request, response) -> {
            try {
                String body = extractBody(request);
                RateLimitConfig config = Json.fromJson(body, RateLimitConfig.class);
                if (config == null) config = new RateLimitConfig();
                rateLimitManager.update(config);
                log.info("[Admin] 限流配置已更新: enabled={}, qps={}", config.isEnabled(), config.getQps());
                writeResponse(response, ReturnResult.ok("限流配置已更新"));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 管理控制台 - 切换 renew
        server.post("/admin/renew", (request, response) -> {
            try {
                String query = request.getQueryString();
                boolean enabled = query != null && query.contains("enabled=true");
                authServerProperties.setRenew(enabled);
                log.info("[Admin] Token 自动续期已{}", enabled ? "开启" : "关闭");
                writeResponse(response, ReturnResult.ok(enabled ? "Token 自动续期已开启" : "Token 自动续期已关闭"));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 管理控制台 - 切换 refreshToken 轮换
        server.post("/admin/refresh-token-rotation", (request, response) -> {
            try {
                String query = request.getQueryString();
                boolean enabled = query != null && query.contains("enabled=true");
                authServerProperties.setRefreshTokenRotation(enabled);
                log.info("[Admin] RefreshToken 轮换已{}", enabled ? "开启" : "关闭");
                writeResponse(response, ReturnResult.ok(enabled ? "RefreshToken 轮换已开启" : "RefreshToken 轮换已关闭"));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 管理控制台 - 修改过期时间
        server.post("/admin/expire", (request, response) -> {
            try {
                String query = request.getQueryString();
                long expire = 0, refreshExpire = 0;
                if (query != null) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("expire=")) expire = Long.parseLong(param.substring(7));
                        if (param.startsWith("refreshExpire=")) refreshExpire = Long.parseLong(param.substring(14));
                    }
                }
                if (expire <= 0) { writeResponse(response, ReturnResult.illegal("过期时间必须大于 0")); return; }
                authServerProperties.setExpire(expire);
                if (refreshExpire > 0) authServerProperties.setRefreshExpire(refreshExpire);
                log.info("[Admin] Token 过期时间已更新: expire={}s", expire);
                writeResponse(response, ReturnResult.ok("Token 过期时间已更新: " + expire + "s"));
            } catch (Exception e) {
                writeResponse(response, ReturnResult.error(e.getMessage()));
            }
        });

        // 临时令牌端点
        server.post("/temporary-token", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new TemporaryTokenAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 临时令牌处理异常", e);
                writeResponse(response, ReturnResult.error("临时令牌创建失败: " + e.getMessage()));
            }
        });
    }

    private void handleVerifyRequest(ServletRequest request, ServletResponse response) {
        try {
            String body = extractBody(request);
            AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
            if (authRequest == null) {
                authRequest = new AuthRequest();
            }

            var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
            var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

            var authInformation = new OauthAuthInformation(
                    authServerProperties,
                    authRequest.getData(),
                    protocolRequest,
                    protocolResponse,
                    applicationContext,
                    loginCheck);

            if (!authInformation.isSuccessful()) {
                writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                return;
            }

            var result = authInformation.authorization().authentication();
            writeResponse(response, result);
        } catch (Exception e) {
            log.error("[OAuth][Armeria] 鉴权处理异常", e);
            writeResponse(response, ReturnResult.error("鉴权失败: " + e.getMessage()));
        }
    }

    /**
     * 从 ServletRequest 提取请求体
     */
    private String extractBody(ServletRequest request) {
        try {
            byte[] body = request.getBody();
            if (body == null || body.length == 0) {
                return "";
            }
            return new String(body, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[OAuth][Armeria] 提取请求体失败", e);
            return "";
        }
    }

    /**
     * 写入响应
     */
    private void writeResponse(ServletResponse response, ReturnResult<?> result) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.setBodyString(Json.toJson(result));
        } catch (Exception e) {
            log.error("[OAuth][Armeria] 写入响应失败", e);
        }
    }

    /**
     * 构建管理控制台 HTML
     */
    private String buildAdminHtml() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OAuth 管理控制台</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f0f2f5; color: #333; }
  .header { background: #1a1a2e; color: #fff; padding: 16px 24px; display: flex; align-items: center; gap: 12px; }
  .header h1 { font-size: 20px; font-weight: 600; }
  .badge { background: #4ade80; color: #000; font-size: 11px; padding: 2px 8px; border-radius: 12px; font-weight: 700; }
  .container { max-width: 1100px; margin: 24px auto; padding: 0 16px; display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
  .card h2 { font-size: 15px; font-weight: 600; margin-bottom: 16px; color: #1a1a2e; border-bottom: 2px solid #e8f4fd; padding-bottom: 8px; }
  .row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
  .label { font-size: 13px; color: #555; }
  .toggle { position: relative; width: 44px; height: 24px; }
  .toggle input { opacity: 0; width: 0; height: 0; }
  .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background: #ccc; border-radius: 24px; transition: .3s; }
  .slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background: white; border-radius: 50%; transition: .3s; }
  input:checked + .slider { background: #4ade80; }
  input:checked + .slider:before { transform: translateX(20px); }
  select, input[type=number] { border: 1px solid #ddd; border-radius: 6px; padding: 6px 10px; font-size: 13px; outline: none; }
  .btn { background: #1a1a2e; color: #fff; border: none; border-radius: 6px; padding: 7px 16px; font-size: 13px; cursor: pointer; }
  .btn-sm { padding: 5px 12px; font-size: 12px; }
  .status-bar { grid-column: 1 / -1; background: #1a1a2e; color: #4ade80; border-radius: 12px; padding: 12px 20px; font-size: 13px; font-family: monospace; }
  .tag { display: inline-block; background: #d1fae5; color: #065f46; border-radius: 4px; padding: 2px 8px; font-size: 12px; margin: 2px; }
  @media (max-width: 700px) { .container { grid-template-columns: 1fr; } }
</style>
</head>
<body>
<div class="header"><h1>🔐 OAuth 管理控制台</h1><span class="badge">LIVE</span></div>
<div class="container">
  <div class="status-bar" id="statusBar">正在加载配置...</div>
  <div class="card">
    <h2>🔏 签名校验</h2>
    <div class="row"><span class="label">签名校验 (sign-verify)</span>
      <label class="toggle"><input type="checkbox" id="signVerify" onchange="toggleSignVerify(this.checked)"><span class="slider"></span></label></div>
    <div class="row"><span class="label" style="font-size:12px;color:#999">关闭后跳过 x-oauth-sign 验证，仅用于调试</span></div>
  </div>
  <div class="card">
    <h2>⏱ Token 配置</h2>
    <div class="row"><span class="label">自动续期 (renew)</span>
      <label class="toggle"><input type="checkbox" id="renew" onchange="toggleRenew(this.checked)"><span class="slider"></span></label></div>
    <div class="row"><span class="label">RT 轮换 (rotation)</span>
      <label class="toggle"><input type="checkbox" id="rtRotation" onchange="toggleRtRotation(this.checked)"><span class="slider"></span></label></div>
    <div class="row" style="gap:8px;flex-wrap:wrap">
      <span class="label">AT 过期(s)</span><input type="number" id="expire" style="width:90px" min="60">
      <span class="label">RT 过期(s)</span><input type="number" id="refreshExpire" style="width:90px" min="60">
      <button class="btn btn-sm" onclick="updateExpire()">保存</button></div>
  </div>
  <div class="card">
    <h2>👥 在线模式</h2>
    <div class="row"><span class="label">模式</span>
      <select id="onlineMode"><option value="MULTIPLE">MULTIPLE（不限制）</option><option value="SINGLE">SINGLE（单点登录）</option><option value="LIMIT">LIMIT（限制设备数）</option></select></div>
    <div class="row" id="maxCountRow" style="display:none"><span class="label">最大设备数</span>
      <input type="number" id="maxOnlineCount" style="width:80px" min="1" value="3"></div>
    <div class="row"><button class="btn" onclick="updateOnlineMode()">应用</button></div>
  </div>
  <div class="card">
    <h2>🚦 限流配置</h2>
    <div class="row"><span class="label">启用限流</span>
      <label class="toggle"><input type="checkbox" id="rateLimitEnabled"><span class="slider"></span></label></div>
    <div class="row"><span class="label">QPS</span><input type="number" id="rateLimitQps" style="width:90px" min="1" value="100"></div>
    <div class="row"><span class="label">突发容量</span><input type="number" id="rateLimitBurst" style="width:90px" min="1" value="200"></div>
    <div class="row"><span class="label">限流维度</span>
      <select id="rateLimitDimension"><option value="global">global（全局）</option><option value="ip">ip（按 IP）</option><option value="user">user（按用户）</option></select></div>
    <div class="row"><button class="btn" onclick="updateRateLimit()">应用</button></div>
  </div>
  <div class="card" style="grid-column: 1 / -1">
    <h2>🔌 协议状态</h2><div id="protocolTags">加载中...</div>
  </div>
</div>
<script>
async function api(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const r = await fetch('/admin' + path, opts);
  return r.json();
}
async function loadStatus() {
  try {
    const r = await api('GET', '/status');
    const d = r.data;
    document.getElementById('signVerify').checked = d.signVerify;
    document.getElementById('renew').checked = d.renew;
    document.getElementById('rtRotation').checked = d.refreshTokenRotation;
    document.getElementById('expire').value = d.expire;
    document.getElementById('refreshExpire').value = d.refreshExpire;
    document.getElementById('onlineMode').value = d.online;
    document.getElementById('maxOnlineCount').value = d.maxOnlineCount;
    toggleMaxCountRow(d.online);
    document.getElementById('rateLimitEnabled').checked = d.rateLimit.enabled;
    document.getElementById('rateLimitQps').value = d.rateLimit.qps;
    document.getElementById('rateLimitBurst').value = d.rateLimit.burstSize;
    document.getElementById('rateLimitDimension').value = d.rateLimit.dimension;
    document.getElementById('protocolTags').innerHTML = d.protocols.map(p => `<span class="tag">${p}</span>`).join('');
    showStatus(`✅ 配置已加载 | signVerify=${d.signVerify} | online=${d.online} | rateLimit=${d.rateLimit.enabled}`);
  } catch(e) { showStatus('❌ 加载失败: ' + e.message); }
}
function showStatus(msg) { document.getElementById('statusBar').textContent = new Date().toLocaleTimeString() + '  ' + msg; }
function toggleMaxCountRow(mode) { document.getElementById('maxCountRow').style.display = mode === 'LIMIT' ? 'flex' : 'none'; }
document.getElementById('onlineMode').addEventListener('change', e => toggleMaxCountRow(e.target.value));
async function toggleSignVerify(v) { const r = await api('POST', '/sign-verify?enabled=' + v); showStatus(r.data || r.msg); }
async function toggleRenew(v) { const r = await api('POST', '/renew?enabled=' + v); showStatus(r.data || r.msg); }
async function toggleRtRotation(v) { const r = await api('POST', '/refresh-token-rotation?enabled=' + v); showStatus(r.data || r.msg); }
async function updateExpire() {
  const expire = document.getElementById('expire').value;
  const refreshExpire = document.getElementById('refreshExpire').value;
  const r = await api('POST', `/expire?expire=${expire}&refreshExpire=${refreshExpire}`);
  showStatus(r.data || r.msg);
}
async function updateOnlineMode() {
  const mode = document.getElementById('onlineMode').value;
  const maxCount = document.getElementById('maxOnlineCount').value;
  const r = await api('POST', `/online-mode?mode=${mode}&maxCount=${maxCount}`);
  showStatus(r.data || r.msg);
}
async function updateRateLimit() {
  const config = { enabled: document.getElementById('rateLimitEnabled').checked,
    qps: parseInt(document.getElementById('rateLimitQps').value),
    burstSize: parseInt(document.getElementById('rateLimitBurst').value),
    dimension: document.getElementById('rateLimitDimension').value };
  const r = await api('POST', '/rate-limit', config);
  showStatus(r.data || r.msg);
}
loadStatus();
setInterval(loadStatus, 30000);
</script>
</body>
</html>
""";
    }
}
