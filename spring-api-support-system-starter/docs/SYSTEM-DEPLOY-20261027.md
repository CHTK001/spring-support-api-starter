# system 后端部署文档（instabox 远程 + lib 资源分离）

> 交付日期：2026-10-27
> 部署目标：`ssh -p 2222 ibssh_lpjexpuxrqawbyhaknptviqnka@ssh.instabox.sh`
> 部署目录：`/data/workspace/system-deploy/`
> 公网入口：`https://18170-agqwy5qu7aattthjw7rgsmp7xe.instabox.tech`（沙箱自动映射 18170 → 公网 https）

---

## 1. 结论

| 项 | 结果 |
|---|---|
| 后端进程 | `system.jar` active（`--enable-preview -Xms512m -Xmx1024m`），Tomcat `18170`，context-path `/system/api` |
| 资源分离 | 瘦 jar（5.5MB）+ 外置 `lib/`（**610 jar**）+ 外置 `config/` |
| 数据库 | MySQL `127.0.0.1:3306/system`（utf8mb4，专户 `system/system123`） |
| 启动耗时 | `Started SystemApplication in ~17s` |
| 端到端验收 | 登录 → 在线用户列表/强制下线/重登录 全链路 PASS；消息管理 Tab 右对齐 PASS |
| 登录链路 | 前端 CSRF double-submit 适配（后端安全语义零改动，见 §5） |

---

## 2. 目录布局

```text
/data/workspace/system-deploy/
├── system.jar                  # 瘦 jar（本次只替换这一个文件即可发版）
├── lib/                        # 610 个依赖 jar（首次全量，之后不变）
├── config/
│   ├── application.yml         # base 配置（打包自带副本，外置优先）
│   ├── application-server.yml  # ★server profile（datasource/redis/oauth 关键配置）
│   ├── application-dev*.yml    # 打包携带，未被加载（server profile 生效）
│   ├── application-h2.yml
│   ├── application-verify-*.yml
│   └── oauth.whitelist         # 空模板占位
├── logs/system.log             # 运行日志
├── start.sh                    # 启动脚本（PID 管理）
├── system.pid                  # 当前进程 PID
├── data/  oss/  tmp/  face/  skills/  rag-uploads/   # 运行期数据目录
└── .chunks/                    # 中断续传分片
```

**关键设计**：jar 的 `MANIFEST Class-Path` 引用相对目录 `config/` 与 `lib/`，因此 `java -jar system.jar` 直接可用，依赖全部外置。**后续增量发版：只替换 `system.jar` + 必要的 `config/*.yml` 即可，`lib/` 不再变动**。

---

## 3. 启动脚本（start.sh）

```bash
#!/usr/bin/env bash
APP_DIR=/data/workspace/system-deploy
JAR=$APP_DIR/system.jar
LOG=$APP_DIR/logs/system.log
PID=$APP_DIR/system.pid

export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64   # 环境非 25 时自动探测
[ -d "$JAVA_HOME" ] || JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(which java)")")")
export PATH="$JAVA_HOME/bin:$PATH"

if [ -f "$PID" ] && kill -0 "$(cat "$PID")" 2>/dev/null; then
  echo "already running pid=$(cat "$PID")"
  exit 0
fi

mkdir -p "$APP_DIR/logs" "$APP_DIR/tmp" "$APP_DIR/data" "$APP_DIR/oss/.chunks"
cd "$APP_DIR"
nohup java --enable-preview \
  -Xms512m -Xmx1024m \
  -XX:MaxMetaspaceSize=384m \
  -Duser.timezone=Asia/Shanghai \
  -Djava.net.preferIPv4Stack=true \
  -Djava.io.tmpdir="$APP_DIR/tmp" \
  -jar "$JAR" \
  --spring.profiles.active=server \
  >> "$LOG" 2>&1 &
echo $! > "$PID"
```

---

## 4. 关键配置（config/application-server.yml）

```yaml
server:
  port: 18170
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: system
    password: system123
  data:
    redis:
      database: 6
      host: 127.0.0.1
      port: 6379
plugin:
  datasource:
    script:                       # FlywayLike 建表 + 灌 seed
      enable: true
      scan-mode: ONCE
      data-scan-mode: ONCE
      migration-scan-mode: ALWAYS
      continue-on-error: true
      verbose: true
  system:
    server:
      enable: true
  oauth:
    protocol: static              # 静态协议：sys_user 表校验
    enable-encryption: false      # 前端明文密码
    temp:
      user: admin:admin123        # 兜底临时用户
    server:
      service-key: <随机>          # OAuth 服务端密钥（勿泄露）
      sign-verify: true
      jwt:
        enabled: true
        algorithm: HS256
        secret: <随机>
        issuer: oauth-server
        expire-seconds: 3600
        jwks-enabled: true
      expire: 2592000
      refresh-expire: 3456000
      online: SINGLE              # 单点登录：新登录踢旧会话
```

> ⚠️ **重要**：datasource 必须通过 `server` profile 文件提供，**不能**用命令行 `--spring.datasource.*` —— 本项目走 datasource-starter 的动态数据源体系，命令行参数不被消费。

---

## 5. 登录 403（CSRF）根因与修复

### 现象
浏览器登录 `POST /v2/user/login` 返回 `403 {"code":403,"msg":"CSRF校验失败"}`。

### 根因（前后端契约不匹配）
后端 `OauthCsrfFilter`（默认启用，`plugin.oauth.server.security.csrf.enabled` 默认 true）：
- 对状态变更请求（POST/PUT/PATCH/DELETE）校验 `X-CSRF-TOKEN` 请求头 == `XSRF-TOKEN` cookie 值，且 Redis 有该 token（double-submit）；
- **但 axios 原生默认把 cookie 值放到 `X-XSRF-TOKEN` 头**，与后端契约 `X-CSRF-TOKEN` 不一致 → 匿名登录 POST 403；
- 登录后的请求带 `Authorization` 头 → 后端判定「非 Cookie 凭据」自动豁免 CSRF，所以只有登录这一个匿名 POST 被卡。

### 修复（前端 double-submit 适配，后端零改动）
1. `packages/core/src/httpCrypto.ts`：请求拦截器从 `XSRF-TOKEN` cookie 读值，注入 `X-CSRF-TOKEN` 请求头；
2. `apps/vue-support-system-starter/vite.config.ts`（仅 dev）：dev 代理剥掉响应 Set-Cookie 的 `Secure` 标志 —— dev 页面在 `http://localhost:8096`，后端经公网 https 下发的 Secure cookie 会被浏览器丢弃。

### 为什么不走后端配置豁免 login
`plugin.oauth.server.security.csrf.ignore-paths` **无法绑定**（实测启动报 `left unbound`）：`AuthServerProperties`（prefix=`plugin.oauth.server`）显式 `ignoreUnknownFields=false`，遇到归属同前缀树下 `OauthSecurityProperties`（prefix=`plugin.oauth.server.security`）的 `security.*` 子属性即报错。当前前端适配已完整解决，csrf 段保持不配置（最干净状态）。

---

## 6. 增量发版流程

```bash
# 1) 本地构建瘦 jar（含 config/ lib/ 之 MANIFEST Class-Path）
mvn -pl spring-api-support-system-starter clean package -DskipTests

# 2) 上传（只传 jar；lib 已全量就位无需再动）
scp -P 2222 spring-api-support-system-starter-4.0.0.42.jar \
    ibssh_lpjexpuxrqawbyhaknptviqnka@ssh.instabox.sh:/data/workspace/system-deploy/system.jar

# 3) 远程重启（SSH 隧道已不需要——直接用公网地址验证）
ssh -p 2222 ibssh_lpjexpuxrqawbyhaknptviqnka@ssh.instabox.sh "kill \$(cat /data/workspace/system-deploy/system.pid); rm -f /data/workspace/system-deploy/system.pid; bash /data/workspace/system-deploy/start.sh"

# 4) 验证
curl -s -o /dev/null -w '%{http_code}' https://18170-agqwy5qu7aattthjw7rgsmp7xe.instabox.tech/system/api/v2/setting/default   # 期望 200
```

> 若只改 `config/`（如 application-server.yml），则只上传 yml + 重启，四步变两步。

---

## 7. 维护命令速查

| 操作 | 命令 |
|---|---|
| 查看进程 | `ps -ef \| grep system.jar` |
| 查看日志 | `tail -f /data/workspace/system-deploy/logs/system.log` |
| 优雅重启 | `kill $(cat system.pid)` → `bash start.sh` |
| 数据库 | `mysql -usystem -psystem123 -e "USE system; ..."`（本机 127.0.0.1:3306） |
| 公网探活 | `https://18170-….instabox.tech/system/api/v2/setting/default`（白名单可匿名） |

---

## 8. 本次验收记录（2026-10-27，本地浏览器实测）

| 验收项 | 结果 |
|---|---|
| 登录 `admin/admin123` | ✅ `#/login` → `#/home` |
| 监控管理 → 在线用户菜单（仅管理员可见） | ✅ id=126 seed 生效 |
| 在线用户列表 | ✅ admin 会话（IP/浏览器/系统/登录时间）真实数据 |
| 强制下线 | ✅ 确认弹窗 → 后端「令牌已吊销」→ 列表 0 人 |
| 被踢后刷新 | ✅ 跳 404（token 已失效） |
| 清除 storage 重新登录 | ✅ 新会话 17:18 出现，旧会话消失 |
| 消息管理 Tab 右对齐 | ✅ `.system-tabs-card` computed `justify-content: flex-end` |

### 提交记录
- 后端 `spring-support-api-starter`：`9f8870f`（SystemApplication 装配修复 + V1.2 在线用户菜单 seed）
- 前端 `vue-support-parent-starter`：`d3bed6d0`（CSRF double-submit 适配 + dev 代理剥 Secure）

---

## 9. 已知事项 / 排障

- **Cassandra 无浏览器**：远程无法起浏览器渲染前端，验收用本地 dev（`http://localhost:8096/system/`，代理指向公网地址）+ agent-browser 完成。
- **curl 直连 403「检测到爬虫」**：后端反爬虫按 UA 拦截自动化客户端，非故障；浏览器/代理带第一方豁免头（`x-trace-id`/`x-requested-with` 以 `-7f3a` 结尾）即通过。
- **`eduAuthDataSource` 空 jdbcUrl 报错非致命**：仅学生登录链才走该数据源，WEB 管理登录不受影响；如启用需配置 `plugin.edu-auth.datasource`。
- **SSH 偶发 `Permission denied (publickey)`**：本机 `ssh-agent` 服务停止导致；`Start-Service ssh-agent` 后恢复。
- **在线用户列表来源**：复用 `SysOnlineController`（`/v2/online/*`，`@Permission(role={ADMIN,SUPER_ADMIN})`）+ `SysOnlineServiceImpl` 走 AuthClientExecute 拉 oauth，非本地表。