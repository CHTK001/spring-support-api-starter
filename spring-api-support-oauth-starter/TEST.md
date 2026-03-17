# OAuth 服务端测试清单

> 服务端：`http://localhost:19180`（Armeria）/ RSocket `7000`
> 客户端：`http://localhost:18170/system/api`（Undertow）
> 鉴权路径：`/verify`（默认，`plugin.oauth.oauth-url` 默认值已改为 `/verify`）
> 加密：AES，`plugin.oauth.enable-encryption: true`（默认开启，测试环境关闭）

---

## 测试清单总览

| # | 测试项 | 状态 | 备注 |
|---|--------|:----:|------|
| 1.1 | 正常登录（加密关闭） | ✅ | code=00000，返回 accessToken+refreshToken |
| 1.2 | 正常登录（加密开启） | ⬜ | enable-encryption: true |
| 1.3 | 错误密码登录 | ⚠️ | 服务端正确拒绝，客户端返回 C0400S0000 鉴权服务器异常（消息透传未生效） |
| 1.4 | 用户不存在 | ⬜ | 账户解锁后重测 |
| 2.1 | 鉴权（有效 token，加密关闭） | ✅ | code=00000 user=sa |
| 2.2 | 鉴权（有效 token，加密开启） | ⬜ | enable-encryption: true |
| 2.3 | 鉴权（无效 token） | ⚠️ | 服务端拒绝，客户端返回 HTTP 500（应为 401，待修复） |
| 2.4 | 鉴权（黑名单 token） | ✅ | 登出后旧 token 被拒绝（HTTP 500） |
| 3.1 | 正常登出 | ✅ | code=00000 注销成功，旧 token 失效 |
| 3.2 | 全部登出（踢所有设备） | ✅ | LOGOUT_ALL 后所有 token 失效（修复：从 token 查 userId 再删 Redis） |
| 4.1 | refreshToken 刷新（不轮换） | ✅ | 返回新 accessToken，refreshToken 不变 |
| 4.2 | refreshToken 刷新（轮换） | ⬜ | 需配置 refresh-token-rotation: true |
| 4.3 | version 升级 | ⬜ | |
| 4.4 | timestamp 升级 | ⬜ | |
| 4.5 | 临时令牌 | ⬜ | |
| 5.1 | MULTIPLE 模式（多点登录） | ✅ | 多 token 均有效 |
| 5.2 | SINGLE 模式（单点登录） | ⬜ | 需改配置 |
| 5.3 | LIMIT 模式（限制设备数） | ⬜ | 需改配置 |
| 6.1 | 签名校验开启 | ⬜ | 需改配置 |
| 6.2 | 签名校验关闭 | ⬜ | 需改配置 |
| 7.1 | RSocket 登录 | ⬜ | |
| 7.2 | RSocket 鉴权 | ⬜ | |
| 7.3 | RSocket 登出 | ⬜ | |
| 7.4 | RSocket upgrade | ⬜ | |
| 8.1 | 登录日志写入 DB | ✅ | `[LOGIN][traceId=xxx]` 日志存在 |
| 8.2 | 登出日志写入 DB | ⬜ | 待验证 |
| 8.3 | IP 归属地解析 | ⬜ | 新版本已修复但懒加载未触发 |

> 状态：⬜ 待测 ✅ 通过 ❌ 失败 ⚠️ 部分通过 🔧 已修复待验证

---

## 1. 登录测试

> 配置开关：`plugin.oauth.enable-encryption: false/true`（在 `application-dev.yml` 中切换）

### 1.1 正常登录（加密关闭）

**配置**：`enable-encryption: false`

**步骤**：
```bash
curl -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sa","password":"admin123"}'
```

**预期**：
- `code: "00000"`，返回 `accessToken` + `refreshToken`
- 请求体为明文 JSON，抓包可直接看到字段

---

### 1.2 正常登录（加密开启）

**配置**：`enable-encryption: true`（去掉 application-dev.yml 中的 `enable-encryption: false`）

**步骤**：同 1.1，相同请求

**预期**：
- `code: "00000"`，返回 `accessToken` + `refreshToken`
- 抓包可见请求体 `data` 字段为 AES 加密的十六进制字符串，非明文
- 响应 `data` 字段同样为加密内容，客户端自动解密后正常返回

---

### 1.3 错误密码

**配置**：`enable-encryption: false`

**步骤**：
```bash
curl -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sa","password":"wrongpassword"}'
```

**预期**：
- `code != "00000"`，msg 应为"账号/密码不正确"（服务端透传）
- `sys_user_log` 中有 `sys_log_status=0` 的失败记录

---

### 1.4 用户不存在

**前置**：确认账户未被锁定（之前多次失败可能触发锁定）

**步骤**：
```bash
curl -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nonexistent_user","password":"any"}'
```

**预期**：
- `code != "00000"`，msg 提示用户不存在
- `sys_user_log` 中有失败记录

---

## 2. 鉴权测试

> 鉴权路径固定为 `/verify`（`plugin.oauth.oauth-url` 默认值）

### 2.1 有效 token（加密关闭）

**配置**：`enable-encryption: false`

**步骤**：
```bash
# 先登录获取 token
TOKEN=$(curl -s -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sa","password":"admin123"}' | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

# 鉴权
curl http://localhost:18170/system/api/v2/user/me \
  -H "x-oauth-token: $TOKEN"
```

**预期**：`code: "00000"`，返回用户信息，`username=sa`

---

### 2.2 有效 token（加密开启）

**配置**：`enable-encryption: true`

**步骤**：同 2.1，相同请求

**预期**：
- `code: "00000"`，返回用户信息
- 客户端与服务端之间的鉴权请求体为 AES 加密内容，对业务层透明

---

### 2.3 无效 token

**步骤**：
```bash
curl http://localhost:18170/system/api/v2/user/me \
  -H "x-oauth-token: invalid_token_string_12345"
```

**预期**：返回 `code: "40100"` 或 HTTP 401（当前返回 HTTP 500，待修复）

---

### 2.4 黑名单 token（登出后重用）

**步骤**：
```bash
# 1. 登录
# 2. 登出
curl -X DELETE http://localhost:18170/system/api/v2/user/logout \
  -H "x-oauth-token: $TOKEN"

# 3. 用旧 token 再次鉴权
curl http://localhost:18170/system/api/v2/user/me \
  -H "x-oauth-token: $TOKEN"
```

**预期**：第 3 步返回 `code: "40100"`，不再命中本地缓存

---

## 3. 登出测试

### 3.1 正常登出

**步骤**：
```bash
curl -X DELETE http://localhost:18170/system/api/v2/user/logout \
  -H "x-oauth-token: $TOKEN"
```

**预期**：
- `code: "00000"`，msg="注销成功"
- 再用旧 token 调 `/v2/user/me` 返回 401
- `sys_user_log` 中有 `sys_log_from=LOGOUT` 记录

---

### 3.2 全部登出

**步骤**：
```bash
# 同一账号登录 3 次，获得 token1/token2/token3
# 用任意一个 token 发起全部登出
curl -X DELETE http://localhost:18170/system/api/v2/user/logout \
  -H "x-oauth-token: $TOKEN1" \
  -H "x-oauth-logout-type: LOGOUT_ALL"

# 分别验证 3 个 token
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TOKEN1"
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TOKEN2"
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TOKEN3"
```

**预期**：3 个 token 均返回 401，本地 Guava 缓存已全部清除

---

## 4. Token 升级测试

> 刷新接口：`POST /system/api/v2/user/refresh-token`（已加入白名单，无需 token）

### 4.1 refreshToken 刷新（不轮换，默认）

**配置**：`plugin.oauth.server.refresh-token-rotation: false`（默认）

**步骤**：
```bash
# 1. 登录获取 refreshToken
REFRESH=$(curl -s -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sa","password":"admin123"}' | python -c "import sys,json; print(json.load(sys.stdin)['data']['refreshToken'])")

# 2. 刷新
curl -X POST http://localhost:18170/system/api/v2/user/refresh-token \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"

# 3. 再次用同一 refreshToken 刷新（不轮换模式下应仍有效）
curl -X POST http://localhost:18170/system/api/v2/user/refresh-token \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"
```

**预期**：
- 返回新 `accessToken`，`refreshToken` 不变
- 旧 `accessToken` 已失效
- 第 3 步仍能刷新成功

---

### 4.2 refreshToken 刷新（轮换）

**配置**：`plugin.oauth.server.refresh-token-rotation: true`

**步骤**：同 4.1，第 3 步用旧 `refreshToken` 再次刷新

**预期**：
- 第 2 步返回新 `accessToken` + 新 `refreshToken`
- 第 3 步用旧 `refreshToken` 返回 401（已失效）

---

### 4.3 version 升级

**步骤**：
```bash
curl -X POST http://localhost:18170/system/api/v2/user/upgrade \
  -H "x-oauth-token: $TOKEN" \
  -H "x-oauth-upgrade-type: VERSION"
```

**预期**：返回最新用户信息，token 不变，用户缓存已刷新

---

### 4.4 timestamp 升级（续期）

**步骤**：
```bash
curl -X POST http://localhost:18170/system/api/v2/user/upgrade \
  -H "x-oauth-token: $TOKEN" \
  -H "x-oauth-upgrade-type: TIMESTAMP"
```

**预期**：token 有效期延长，返回用户信息

---

### 4.5 临时令牌

**步骤**：
```bash
# 1. 申请临时令牌（10秒有效）
TEMP=$(curl -s -X POST http://localhost:18170/system/api/v2/user/temporary-token \
  -H "x-oauth-token: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"expireTime":10}' | python -c "import sys,json; print(json.load(sys.stdin)['data'])")

# 2. 立即鉴权（应成功）
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TEMP"

# 3. 等待 11 秒后再次鉴权（应失败）
sleep 11
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TEMP"
```

**预期**：步骤 2 返回 200，步骤 3 返回 401

---

## 5. 在线模式测试

### 5.1 MULTIPLE（默认，不限制）

**配置**：`plugin.oauth.server.online: MULTIPLE`（默认，无需改配置）

**步骤**：
```bash
# 登录 3 次获得 3 个 token
for i in 1 2 3; do
  curl -s -X POST http://localhost:18170/system/api/v2/user/login \
    -H "Content-Type: application/json" \
    -d '{"username":"sa","password":"admin123"}' | python -c "import sys,json; d=json.load(sys.stdin); print(d['data']['token'])"
done
# 分别用 3 个 token 鉴权，均应成功
```

**预期**：3 个 token 均返回 200，互不影响

---

### 5.2 SINGLE（单点登录）

**配置**：修改 oauth-starter `application.yml`：
```yaml
plugin:
  oauth:
    server:
      online: SINGLE
```

**步骤**：
```bash
# 第一次登录
TOKEN1=<登录获取>
# 第二次登录（同账号）
TOKEN2=<登录获取>

# 验证 token1（应被踢出）
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TOKEN1"
# 验证 token2（应有效）
curl http://localhost:18170/system/api/v2/user/me -H "x-oauth-token: $TOKEN2"
```

**预期**：`token1` 返回 401，`token2` 返回 200

---

### 5.3 LIMIT（限制设备数）

**配置**：
```yaml
plugin:
  oauth:
    server:
      online: LIMIT
      max-online-count: 3
```

**步骤**：
```bash
# 登录 3 次（均成功）
# 第 4 次登录（应失败）
curl -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sa","password":"admin123"}'

# 登出一个设备后再登录（应成功）
curl -X DELETE http://localhost:18170/system/api/v2/user/logout -H "x-oauth-token: $TOKEN1"
curl -X POST http://localhost:18170/system/api/v2/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sa","password":"admin123"}'
```

**预期**：第 4 次登录返回"已达到最大在线设备数量限制(3)"，登出后可再次登录

---

## 6. 签名校验测试

### 6.1 签名校验开启（默认）

**配置**：`plugin.oauth.server.sign-verify: true`（默认）

**步骤**：直接向服务端发送缺少签名头的请求
```bash
curl -X POST http://localhost:19180/login \
  -H "Content-Type: application/json" \
  -d '{"data":"test"}'
```

**预期**：返回 `sign签名认证失败!`

---

### 6.2 签名校验关闭

**配置**：`plugin.oauth.server.sign-verify: false`

**步骤**：同 6.1，发送无签名请求

**预期**：正常处理（不因签名缺失而拒绝）

---

## 7. RSocket 协议测试（端口 7000）

RSocket 使用 request-response 模式，metadata 为路由，data 为请求体（同 Armeria）。

### 7.1 RSocket 登录

**步骤**：
```
route: /login
metadata: {x-oauth-uuid, x-oauth-serial, x-oauth-sign, x-oauth-encode}
data: <加密的登录数据>
```

**预期**：同 Armeria 登录，返回 `accessToken` + `refreshToken`

---

### 7.2 RSocket 鉴权

**步骤**：
```
route: /oauth
data: {x-oauth-token: <accessToken>}
```

**预期**：返回用户信息

---

### 7.3 RSocket 登出

**步骤**：
```
route: /logout
data: {x-oauth-token: <accessToken>, x-oauth-logout-type: LOGOUT}
```

**预期**：token 失效

---

### 7.4 RSocket upgrade

**步骤**：
```
route: /upgrade
data: {x-oauth-refresh-token: <refreshToken>, x-oauth-upgrade-type: refresh}
```

**预期**：返回新 `accessToken`

---

## 8. 日志验证

### 8.1 登录日志写入 DB

**步骤**：登录后执行 SQL
```sql
SELECT sys_log_username, sys_log_status, sys_log_ip, sys_log_browser,
       sys_log_cost, sys_log_login_type, sys_log_address, sys_log_isp
FROM sys_user_log
WHERE sys_log_from = 'LOGIN'
ORDER BY create_time DESC LIMIT 1;
```

**预期**：
- `sys_log_username` = `sa`
- `sys_log_status` = 1（成功）
- `sys_log_ip`、`sys_log_browser`、`sys_log_cost`、`sys_log_login_type` 均有值

---

### 8.2 登出日志写入 DB

**步骤**：登出后执行 SQL
```sql
SELECT sys_log_username, sys_log_status, sys_log_param
FROM sys_user_log
WHERE sys_log_from = 'LOGOUT'
ORDER BY create_time DESC LIMIT 1;
```

**预期**：
- `sys_log_username` 有值
- `sys_log_status` = 1
- `sys_log_param` 包含 `logoutType`

---

### 8.3 IP 归属地解析

**步骤**：登录后执行 SQL
```sql
SELECT sys_log_ip, sys_log_address, sys_log_isp
FROM sys_user_log
WHERE sys_log_from = 'LOGIN'
ORDER BY create_time DESC LIMIT 1;
```

**预期**：
- `sys_log_address` 有值（如"中国-广东-深圳"）
- `sys_log_isp` 有值（如"电信"）
- 若为本地 IP（127.0.0.1），address 可能为"内网"

---

## 多协议测试矩阵

| 功能 | Armeria (19180) | RSocket (7000) |
|------|:-:|:-:|
| 登录 `/login` | ✅ | ✅ |
| 鉴权 `/oauth` | ✅ | ✅ |
| 登出 `/logout` | ✅ | ✅ |
| Token 升级 `/upgrade` | ✅ | ✅ |
| 临时令牌 `/temporary-token` | ✅ | ✅ |
