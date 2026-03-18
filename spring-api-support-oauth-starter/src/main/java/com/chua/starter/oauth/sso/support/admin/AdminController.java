package com.chua.starter.oauth.sso.support.admin;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OAuth 服务端热控制管理页面
 * <p>
 * 访问地址：/oauth/admin（GET 返回 HTML 控制台，POST 接口热更新配置）
 * </p>
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth 管理控制台", description = "运行时热控制 OAuth 服务端配置")
public class AdminController {

    private final AuthServerProperties authServerProperties;
    private final RateLimitManager rateLimitManager;

    /** 管理控制台首页（内嵌 HTML） */
    @GetMapping(produces = "text/html;charset=UTF-8")
    public String index() {
        return buildHtml();
    }

    /** 获取当前配置状态 */
    @GetMapping("/status")
    @Operation(summary = "获取当前配置状态")
    public ReturnResult<Map<String, Object>> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("signVerify", authServerProperties.isSignVerify());
        status.put("online", authServerProperties.getOnline().name());
        status.put("maxOnlineCount", authServerProperties.getMaxOnlineCount());
        status.put("expire", authServerProperties.getExpire());
        status.put("refreshExpire", authServerProperties.getRefreshExpire());
        status.put("renew", authServerProperties.isRenew());
        status.put("refreshTokenRotation", authServerProperties.isRefreshTokenRotation());
        status.put("protocols", authServerProperties.getProtocols());
        status.put("rateLimit", rateLimitManager.getStatus());
        return ReturnResult.ok(status);
    }

    /** 热切换签名校验 */
    @PostMapping("/sign-verify")
    @Operation(summary = "切换签名校验开关")
    public ReturnResult<String> toggleSignVerify(@RequestParam boolean enabled) {
        authServerProperties.setSignVerify(enabled);
        log.info("[Admin] 签名校验已{}", enabled ? "开启" : "关闭");
        return ReturnResult.ok(enabled ? "签名校验已开启" : "签名校验已关闭");
    }

    /** 热切换在线模式 */
    @PostMapping("/online-mode")
    @Operation(summary = "切换在线模式")
    public ReturnResult<String> setOnlineMode(@RequestParam String mode,
                                               @RequestParam(required = false, defaultValue = "3") int maxCount) {
        try {
            AuthServerProperties.Online onlineMode = AuthServerProperties.Online.valueOf(mode.toUpperCase());
            authServerProperties.setOnline(onlineMode);
            if (onlineMode == AuthServerProperties.Online.LIMIT) {
                authServerProperties.setMaxOnlineCount(maxCount);
            }
            log.info("[Admin] 在线模式已切换为: {}, maxCount: {}", onlineMode, maxCount);
            return ReturnResult.ok("在线模式已切换为: " + onlineMode);
        } catch (IllegalArgumentException e) {
            return ReturnResult.illegal("无效的在线模式: " + mode + "，可选值: SINGLE/MULTIPLE/LIMIT");
        }
    }

    /** 热更新限流配置 */
    @PostMapping("/rate-limit")
    @Operation(summary = "配置限流")
    public ReturnResult<String> configRateLimit(@RequestBody RateLimitConfig config) {
        rateLimitManager.update(config);
        log.info("[Admin] 限流配置已更新: enabled={}, qps={}", config.isEnabled(), config.getQps());
        return ReturnResult.ok("限流配置已更新");
    }

    /** 热切换 token 自动续期 */
    @PostMapping("/renew")
    @Operation(summary = "切换 token 自动续期")
    public ReturnResult<String> toggleRenew(@RequestParam boolean enabled) {
        authServerProperties.setRenew(enabled);
        log.info("[Admin] Token 自动续期已{}", enabled ? "开启" : "关闭");
        return ReturnResult.ok(enabled ? "Token 自动续期已开启" : "Token 自动续期已关闭");
    }

    /** 热切换 refreshToken 轮换 */
    @PostMapping("/refresh-token-rotation")
    @Operation(summary = "切换 refreshToken 轮换")
    public ReturnResult<String> toggleRefreshTokenRotation(@RequestParam boolean enabled) {
        authServerProperties.setRefreshTokenRotation(enabled);
        log.info("[Admin] RefreshToken 轮换已{}", enabled ? "开启" : "关闭");
        return ReturnResult.ok(enabled ? "RefreshToken 轮换已开启" : "RefreshToken 轮换已关闭");
    }

    /** 修改 token 过期时间 */
    @PostMapping("/expire")
    @Operation(summary = "修改 token 过期时间（秒）")
    public ReturnResult<String> setExpire(@RequestParam long expire,
                                           @RequestParam(required = false, defaultValue = "0") long refreshExpire) {
        if (expire <= 0) {
            return ReturnResult.illegal("过期时间必须大于 0");
        }
        authServerProperties.setExpire(expire);
        if (refreshExpire > 0) {
            authServerProperties.setRefreshExpire(refreshExpire);
        }
        log.info("[Admin] Token 过期时间已更新: expire={}s, refreshExpire={}s", expire, refreshExpire);
        return ReturnResult.ok(String.format("Token 过期时间已更新: %ds / refreshToken: %ds", expire,
                refreshExpire > 0 ? refreshExpire : authServerProperties.getRefreshExpire()));
    }

    // ---- 内嵌 HTML 控制台 ----
    private String buildHtml() {
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
  .value { font-size: 13px; font-weight: 600; color: #1a1a2e; }
  .toggle { position: relative; width: 44px; height: 24px; }
  .toggle input { opacity: 0; width: 0; height: 0; }
  .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background: #ccc; border-radius: 24px; transition: .3s; }
  .slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background: white; border-radius: 50%; transition: .3s; }
  input:checked + .slider { background: #4ade80; }
  input:checked + .slider:before { transform: translateX(20px); }
  select, input[type=number] { border: 1px solid #ddd; border-radius: 6px; padding: 6px 10px; font-size: 13px; outline: none; }
  select:focus, input[type=number]:focus { border-color: #4ade80; }
  .btn { background: #1a1a2e; color: #fff; border: none; border-radius: 6px; padding: 7px 16px; font-size: 13px; cursor: pointer; transition: background .2s; }
  .btn:hover { background: #2d2d4e; }
  .btn-sm { padding: 5px 12px; font-size: 12px; }
  .status-bar { grid-column: 1 / -1; background: #1a1a2e; color: #4ade80; border-radius: 12px; padding: 12px 20px; font-size: 13px; font-family: monospace; min-height: 48px; }
  .tag { display: inline-block; background: #e8f4fd; color: #1a6fb5; border-radius: 4px; padding: 2px 8px; font-size: 12px; margin: 2px; }
  .tag.active { background: #d1fae5; color: #065f46; }
  .tag.inactive { background: #fee2e2; color: #991b1b; }
  @media (max-width: 700px) { .container { grid-template-columns: 1fr; } }
</style>
</head>
<body>
<div class="header">
  <h1>🔐 OAuth 管理控制台</h1>
  <span class="badge">LIVE</span>
</div>
<div class="container">

  <!-- 状态栏 -->
  <div class="status-bar" id="statusBar">正在加载配置...</div>

  <!-- 签名校验 -->
  <div class="card">
    <h2>🔏 签名校验</h2>
    <div class="row">
      <span class="label">签名校验（sign-verify）</span>
      <label class="toggle">
        <input type="checkbox" id="signVerify" onchange="toggleSignVerify(this.checked)">
        <span class="slider"></span>
      </label>
    </div>
    <div class="row">
      <span class="label" style="font-size:12px;color:#999">关闭后跳过 x-oauth-sign 验证，仅用于调试</span>
    </div>
  </div>

  <!-- Token 配置 -->
  <div class="card">
    <h2>⏱ Token 配置</h2>
    <div class="row">
      <span class="label">自动续期（renew）</span>
      <label class="toggle">
        <input type="checkbox" id="renew" onchange="toggleRenew(this.checked)">
        <span class="slider"></span>
      </label>
    </div>
    <div class="row">
      <span class="label">RT 轮换（rotation）</span>
      <label class="toggle">
        <input type="checkbox" id="rtRotation" onchange="toggleRtRotation(this.checked)">
        <span class="slider"></span>
      </label>
    </div>
    <div class="row" style="gap:8px;flex-wrap:wrap">
      <span class="label">AT 过期(s)</span>
      <input type="number" id="expire" style="width:90px" min="60">
      <span class="label">RT 过期(s)</span>
      <input type="number" id="refreshExpire" style="width:90px" min="60">
      <button class="btn btn-sm" onclick="updateExpire()">保存</button>
    </div>
  </div>

  <!-- 在线模式 -->
  <div class="card">
    <h2>👥 在线模式</h2>
    <div class="row">
      <span class="label">模式</span>
      <select id="onlineMode">
        <option value="MULTIPLE">MULTIPLE（不限制）</option>
        <option value="SINGLE">SINGLE（单点登录）</option>
        <option value="LIMIT">LIMIT（限制设备数）</option>
      </select>
    </div>
    <div class="row" id="maxCountRow" style="display:none">
      <span class="label">最大设备数</span>
      <input type="number" id="maxOnlineCount" style="width:80px" min="1" value="3">
    </div>
    <div class="row">
      <button class="btn" onclick="updateOnlineMode()">应用</button>
    </div>
  </div>

  <!-- 限流配置 -->
  <div class="card">
    <h2>🚦 限流配置</h2>
    <div class="row">
      <span class="label">启用限流</span>
      <label class="toggle">
        <input type="checkbox" id="rateLimitEnabled">
        <span class="slider"></span>
      </label>
    </div>
    <div class="row">
      <span class="label">QPS（每秒请求数）</span>
      <input type="number" id="rateLimitQps" style="width:90px" min="1" value="100">
    </div>
    <div class="row">
      <span class="label">突发容量</span>
      <input type="number" id="rateLimitBurst" style="width:90px" min="1" value="200">
    </div>
    <div class="row">
      <span class="label">限流维度</span>
      <select id="rateLimitDimension">
        <option value="global">global（全局）</option>
        <option value="ip">ip（按 IP）</option>
        <option value="user">user（按用户）</option>
      </select>
    </div>
    <div class="row">
      <button class="btn" onclick="updateRateLimit()">应用</button>
    </div>
  </div>

  <!-- 协议状态 -->
  <div class="card" style="grid-column: 1 / -1">
    <h2>🔌 协议状态</h2>
    <div id="protocolTags">加载中...</div>
  </div>

</div>

<script>
const BASE = '';

async function api(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const r = await fetch(BASE + '/oauth/admin' + path, opts);
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
    document.getElementById('protocolTags').innerHTML = d.protocols.map(p =>
      `<span class="tag active">${p}</span>`).join('');
    showStatus(`✅ 配置已加载 | signVerify=${d.signVerify} | online=${d.online} | rateLimit=${d.rateLimit.enabled}`);
  } catch(e) {
    showStatus('❌ 加载失败: ' + e.message);
  }
}

function showStatus(msg) {
  document.getElementById('statusBar').textContent = new Date().toLocaleTimeString() + '  ' + msg;
}

function toggleMaxCountRow(mode) {
  document.getElementById('maxCountRow').style.display = mode === 'LIMIT' ? 'flex' : 'none';
}
document.getElementById('onlineMode').addEventListener('change', e => toggleMaxCountRow(e.target.value));

async function toggleSignVerify(v) {
  const r = await api('POST', '/sign-verify?enabled=' + v);
  showStatus(r.data || r.msg);
}
async function toggleRenew(v) {
  const r = await api('POST', '/renew?enabled=' + v);
  showStatus(r.data || r.msg);
}
async function toggleRtRotation(v) {
  const r = await api('POST', '/refresh-token-rotation?enabled=' + v);
  showStatus(r.data || r.msg);
}
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
  const config = {
    enabled: document.getElementById('rateLimitEnabled').checked,
    qps: parseInt(document.getElementById('rateLimitQps').value),
    burstSize: parseInt(document.getElementById('rateLimitBurst').value),
    dimension: document.getElementById('rateLimitDimension').value
  };
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
