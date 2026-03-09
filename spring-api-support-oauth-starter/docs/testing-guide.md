# OAuth2 第三方登录测试指南

## 1. 概述

本文档提供 OAuth2 第三方登录功能的完整测试指南，帮助开发者在本地环境和生产环境中测试第三方登录功能。

## 2. 本地测试环境配置

### 2.1 前置条件

在开始测试前，请确保：

- [ ] 已安装 JDK 8 或更高版本
- [ ] 已安装 Maven 或 Gradle
- [ ] 已配置好开发环境（IDE）
- [ ] 已申请第三方平台的测试应用凭证
- [ ] 已了解 OAuth2 基本流程

### 2.2 配置测试应用

#### 2.2.1 创建测试配置文件

创建 `application-test.yml` 用于测试：

```yaml
server:
  port: 19180  # 使用非标准端口避免冲突

plugin:
  oauth:
    third-party:
      login:
        # Gitee 测试配置
        gitee:
          enabled: true
          client-id: ${GITEE_TEST_CLIENT_ID}
          client-secret: ${GITEE_TEST_CLIENT_SECRET}
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/gitee
          
        # Github 测试配置
        github:
          enabled: true
          client-id: ${GITHUB_TEST_CLIENT_ID}
          client-secret: ${GITHUB_TEST_CLIENT_SECRET}
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/github

# 启用调试日志
logging:
  level:
    com.chua.starter.oauth: DEBUG
    me.zhyd.oauth: DEBUG
```

#### 2.2.2 设置测试环境变量

**Linux/Mac**：
```bash
# 创建 .env 文件
cat > .env << EOF
export GITEE_TEST_CLIENT_ID=your_test_client_id
export GITEE_TEST_CLIENT_SECRET=your_test_client_secret
export GITHUB_TEST_CLIENT_ID=your_test_client_id
export GITHUB_TEST_CLIENT_SECRET=your_test_client_secret
EOF

# 加载环境变量
source .env
```

**Windows**：
```cmd
# 创建 set-test-env.bat
@echo off
set GITEE_TEST_CLIENT_ID=your_test_client_id
set GITEE_TEST_CLIENT_SECRET=your_test_client_secret
set GITHUB_TEST_CLIENT_ID=your_test_client_id
set GITHUB_TEST_CLIENT_SECRET=your_test_client_secret

# 运行脚本
set-test-env.bat
```

**IDE 配置（IntelliJ IDEA）**：
1. 打开 Run/Debug Configurations
2. 选择 Spring Boot 应用配置
3. 在 Environment variables 中添加：
   ```
   GITEE_TEST_CLIENT_ID=your_test_client_id;
   GITEE_TEST_CLIENT_SECRET=your_test_client_secret;
   GITHUB_TEST_CLIENT_ID=your_test_client_id;
   GITHUB_TEST_CLIENT_SECRET=your_test_client_secret
   ```

#### 2.2.3 配置第三方平台回调地址

在各第三方平台的开发者中心配置测试回调地址：

**Gitee**：
- 登录 [Gitee 第三方应用](https://gitee.com/oauth/applications)
- 编辑应用
- 添加回调地址：`http://127.0.0.1:19180/oauth2/callback/gitee`

**Github**：
- 登录 [Github Developer Settings](https://github.com/settings/developers)
- 编辑 OAuth App
- 设置 Authorization callback URL：`http://127.0.0.1:19180/oauth2/callback/github`

**注意事项**：
- 使用 `127.0.0.1` 而不是 `localhost`（部分平台不支持 localhost）
- 端口号必须与应用配置一致
- 路径必须完全匹配（包括大小写）

### 2.3 启动测试应用

#### 2.3.1 使用 Maven 启动

```bash
# 使用测试配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 或指定端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=19180"
```

#### 2.3.2 使用 Gradle 启动

```bash
# 使用测试配置启动
./gradlew bootRun --args='--spring.profiles.active=test'
```

#### 2.3.3 使用 IDE 启动

1. 在 IDE 中打开主类（包含 `@SpringBootApplication` 注解）
2. 右键选择 Run 或 Debug
3. 确保 Active profiles 设置为 `test`

#### 2.3.4 验证启动成功

查看控制台输出，确认：

```
INFO  - Started Application in 3.456 seconds
INFO  - 第三方登录配置加载完成
INFO  - 已启用的平台: [gitee, github]
INFO  - Gitee 登录配置: client-id=xxx, redirect-uri=http://127.0.0.1:19180/oauth2/callback/gitee
INFO  - Github 登录配置: client-id=xxx, redirect-uri=http://127.0.0.1:19180/oauth2/callback/github
```

### 2.4 本地测试工具

#### 2.4.1 使用浏览器测试

直接在浏览器中访问授权接口：

```
http://127.0.0.1:19180/oauth2/authorize/gitee
```

浏览器会自动跳转到 Gitee 授权页面。

#### 2.4.2 使用 cURL 测试

```bash
# 测试授权接口（会返回重定向地址）
curl -i http://127.0.0.1:19180/oauth2/authorize/gitee

# 预期响应
HTTP/1.1 302
Location: https://gitee.com/oauth/authorize?client_id=xxx&redirect_uri=xxx&response_type=code&state=xxx
```

#### 2.4.3 使用 Postman 测试

1. 创建新请求
2. 方法：GET
3. URL：`http://127.0.0.1:19180/oauth2/authorize/gitee`
4. 发送请求
5. 查看响应头中的 Location 字段

#### 2.4.4 使用 HTTPie 测试

```bash
# 安装 HTTPie
pip install httpie

# 测试授权接口
http GET http://127.0.0.1:19180/oauth2/authorize/gitee
```

### 2.5 本地调试技巧

#### 2.5.1 启用详细日志

在 `application-test.yml` 中添加：

```yaml
logging:
  level:
    root: INFO
    com.chua.starter.oauth: DEBUG
    me.zhyd.oauth: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

#### 2.5.2 使用断点调试

在以下关键位置设置断点：

1. `OAuth2LoginController.authorize()` - 授权请求入口
2. `OAuth2LoginController.callback()` - 回调处理入口
3. `AuthRequestFactory.getAuthRequest()` - 创建授权请求
4. 用户信息处理逻辑

#### 2.5.3 查看请求参数

在回调方法中打印请求参数：

```java
@GetMapping("/oauth2/callback/{platform}")
public ResponseEntity<?> callback(
    @PathVariable String platform,
    @RequestParam String code,
    @RequestParam(required = false) String state,
    HttpServletRequest request) {
    
    log.debug("OAuth2 callback: platform={}, code={}, state={}", platform, code, state);
    log.debug("Request URL: {}", request.getRequestURL());
    log.debug("Query String: {}", request.getQueryString());
    
    // ... 处理逻辑
}
```

#### 2.5.4 模拟回调请求

使用 cURL 模拟回调请求（用于测试回调处理逻辑）：

```bash
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=test_code&state=test_state"
```

### 2.6 常见本地测试问题

#### 问题1：端口被占用

**错误信息**：
```
Web server failed to start. Port 19180 was already in use.
```

**解决方案**：
```bash
# 查找占用端口的进程
# Linux/Mac
lsof -i :19180

# Windows
netstat -ano | findstr :19180

# 终止进程或更换端口
```

#### 问题2：回调地址无法访问

**原因**：
- 防火墙阻止
- 应用未启动
- 端口配置错误

**解决方案**：
1. 检查应用是否正常启动
2. 检查防火墙设置
3. 确认端口配置正确

#### 问题3：环境变量未生效

**解决方案**：
1. 重启 IDE
2. 在 IDE 运行配置中直接设置环境变量
3. 使用配置文件中的默认值进行测试

## 3. 回调地址配置说明

### 3.1 回调地址格式

OAuth2 回调地址格式：

```
{protocol}://{domain}:{port}/oauth2/callback/{platform}
```

**组成部分**：
- `protocol`：协议（http 或 https）
- `domain`：域名或 IP 地址
- `port`：端口号（可选，默认端口可省略）
- `platform`：平台标识（gitee、github、wechat-open 等）

**示例**：
```
http://127.0.0.1:19180/oauth2/callback/gitee
https://example.com/oauth2/callback/github
http://localhost:8080/oauth2/callback/wechat-open
```

### 3.2 平台标识映射

| 平台 | 平台标识 | 回调地址示例 |
|------|----------|--------------|
| Gitee | gitee | http://127.0.0.1:19180/oauth2/callback/gitee |
| Github | github | http://127.0.0.1:19180/oauth2/callback/github |
| 微信开放平台 | wechat-open | http://example.com/oauth2/callback/wechat-open |
| 微信公众号 | wechat-mp | http://example.com/oauth2/callback/wechat-mp |
| 微信小程序 | wechat-mini | 不需要回调地址 |
| 企业微信 | wechat-enterprise | http://example.com/oauth2/callback/wechat-enterprise |
| Google | google | http://127.0.0.1:19180/oauth2/callback/google |
| 百度 | baidu | http://example.com/oauth2/callback/baidu |
| 微博 | weibo | http://example.com/oauth2/callback/weibo |
| QQ | qq | http://example.com/oauth2/callback/qq |

### 3.3 本地测试回调地址

#### 3.3.1 推荐配置

**开发环境**：
```yaml
redirect-uri: http://127.0.0.1:19180/oauth2/callback/{platform}
```

**优点**：
- 大多数平台支持 127.0.0.1
- 避免 DNS 解析问题
- 不需要修改 hosts 文件

#### 3.3.2 使用 localhost

```yaml
redirect-uri: http://localhost:19180/oauth2/callback/{platform}
```

**注意**：
- 部分平台（如 Gitee）可能不支持 localhost
- 建议优先使用 127.0.0.1

#### 3.3.3 使用自定义域名

如果需要使用自定义域名进行本地测试：

1. 修改 hosts 文件：
   ```
   # Linux/Mac: /etc/hosts
   # Windows: C:\Windows\System32\drivers\etc\hosts
   127.0.0.1 local.example.com
   ```

2. 配置回调地址：
   ```yaml
   redirect-uri: http://local.example.com:19180/oauth2/callback/{platform}
   ```

3. 在第三方平台配置相同的回调地址

### 3.4 生产环境回调地址

#### 3.4.1 使用 HTTPS

生产环境必须使用 HTTPS：

```yaml
redirect-uri: https://your-domain.com/oauth2/callback/{platform}
```

**要求**：
- 有效的 SSL 证书
- 证书域名与回调地址域名匹配
- 中间证书链完整

#### 3.4.2 使用标准端口

生产环境建议使用标准端口（80/443）：

```yaml
# HTTPS 默认端口 443，可以省略
redirect-uri: https://your-domain.com/oauth2/callback/{platform}

# 非标准端口必须明确指定
redirect-uri: https://your-domain.com:8443/oauth2/callback/{platform}
```

#### 3.4.3 使用 CDN 或负载均衡

如果使用 CDN 或负载均衡：

```yaml
# 使用 CDN 域名
redirect-uri: https://cdn.your-domain.com/oauth2/callback/{platform}

# 或使用负载均衡域名
redirect-uri: https://lb.your-domain.com/oauth2/callback/{platform}
```

**注意**：
- 确保 CDN/LB 正确转发请求到应用服务器
- 保持请求参数完整（code、state 等）

### 3.5 回调地址配置检查清单

部署前请检查：

- [ ] 回调地址格式正确
- [ ] 协议匹配（开发环境 http，生产环境 https）
- [ ] 域名/IP 正确
- [ ] 端口号正确（如果不是默认端口）
- [ ] 平台标识正确
- [ ] 已在第三方平台配置相同的回调地址
- [ ] 应用可以正常访问回调地址
- [ ] SSL 证书有效（生产环境）

## 4. 测试流程说明

### 4.1 完整测试流程

OAuth2 第三方登录的完整测试流程：

```
1. 用户点击第三方登录按钮
   ↓
2. 前端调用授权接口
   GET /oauth2/authorize/{platform}
   ↓
3. 后端生成授权 URL 并重定向
   302 → https://platform.com/oauth/authorize?...
   ↓
4. 用户在第三方平台授权
   ↓
5. 第三方平台回调应用
   GET /oauth2/callback/{platform}?code=xxx&state=xxx
   ↓
6. 后端使用 code 换取 access_token
   ↓
7. 后端使用 access_token 获取用户信息
   ↓
8. 后端创建或更新用户
   ↓
9. 后端生成应用 token
   ↓
10. 返回 token 给前端
```

### 4.2 授权接口测试

#### 4.2.1 测试授权请求

**请求**：
```http
GET /oauth2/authorize/gitee HTTP/1.1
Host: 127.0.0.1:19180
```

**预期响应**：
```http
HTTP/1.1 302 Found
Location: https://gitee.com/oauth/authorize?client_id=xxx&redirect_uri=http%3A%2F%2F127.0.0.1%3A19180%2Foauth2%2Fcallback%2Fgitee&response_type=code&state=xxx
```

**验证点**：
- [ ] 响应状态码为 302
- [ ] Location 头包含第三方平台授权 URL
- [ ] URL 参数包含 client_id、redirect_uri、response_type、state
- [ ] redirect_uri 已正确编码
- [ ] state 参数已生成（用于防止 CSRF 攻击）

#### 4.2.2 测试不同平台

```bash
# Gitee
curl -i http://127.0.0.1:19180/oauth2/authorize/gitee

# Github
curl -i http://127.0.0.1:19180/oauth2/authorize/github

# 微信开放平台
curl -i http://127.0.0.1:19180/oauth2/authorize/wechat-open

# Google
curl -i http://127.0.0.1:19180/oauth2/authorize/google
```

#### 4.2.3 测试错误情况

```bash
# 不支持的平台
curl -i http://127.0.0.1:19180/oauth2/authorize/invalid_platform
# 预期：400 Bad Request 或 404 Not Found

# 未启用的平台
curl -i http://127.0.0.1:19180/oauth2/authorize/baidu
# 预期：400 Bad Request（如果 baidu 未启用）
```

### 4.3 回调接口测试

#### 4.3.1 手动测试回调

1. 访问授权接口获取授权 URL
2. 在浏览器中打开授权 URL
3. 在第三方平台完成授权
4. 观察浏览器是否正确跳转回应用
5. 检查应用日志

**预期日志**：
```
DEBUG - OAuth2 callback: platform=gitee, code=xxx, state=xxx
DEBUG - 获取 access_token 成功
DEBUG - 获取用户信息成功: userId=xxx, username=xxx
INFO  - OAuth2 登录成功: platform=gitee, userId=xxx
```

#### 4.3.2 模拟回调测试

使用有效的 code 模拟回调（需要先完成真实授权获取 code）：

```bash
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=valid_code&state=valid_state"
```

**注意**：
- code 只能使用一次
- code 有效期通常为 10 分钟
- state 必须与授权时生成的 state 一致

#### 4.3.3 测试错误情况

```bash
# 无效的 code
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=invalid_code&state=xxx"
# 预期：返回错误信息

# 缺少 code 参数
curl "http://127.0.0.1:19180/oauth2/callback/gitee?state=xxx"
# 预期：400 Bad Request

# state 不匹配
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=xxx&state=wrong_state"
# 预期：返回错误信息（CSRF 检查失败）
```

### 4.4 用户信息获取测试

验证是否正确获取用户信息：

**检查点**：
- [ ] 用户 ID（openid/uid）
- [ ] 用户名（username/nickname）
- [ ] 头像（avatar）
- [ ] 邮箱（email）
- [ ] 其他信息（根据 scope 权限）

**日志示例**：
```
DEBUG - 用户信息: {
  "uuid": "123456",
  "username": "test_user",
  "nickname": "测试用户",
  "avatar": "https://avatar.url",
  "email": "test@example.com",
  "gender": "MALE"
}
```

### 4.5 Token 生成测试

验证应用 token 是否正确生成：

**检查点**：
- [ ] Token 格式正确（JWT 或其他格式）
- [ ] Token 包含必要信息（userId、username 等）
- [ ] Token 有效期正确
- [ ] Token 可以正常使用（访问受保护的接口）

**测试方法**：
```bash
# 1. 完成 OAuth2 登录获取 token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 2. 使用 token 访问受保护的接口
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:19180/api/user/profile
```

## 5. 常见测试问题解决

### 5.1 授权跳转失败

**问题**：点击授权按钮后无反应或报错

**排查步骤**：
1. 检查浏览器控制台是否有错误
2. 检查网络请求是否成功
3. 检查后端日志
4. 验证配置是否正确

**常见原因**：
- 前端请求地址错误
- 后端接口异常
- 配置未启用
- client-id 或 client-secret 错误

### 5.2 回调失败

**问题**：授权后无法跳转回应用或报错

**排查步骤**：
1. 检查回调地址是否正确
2. 检查第三方平台配置
3. 检查应用日志
4. 验证 code 是否有效

**常见原因**：
- 回调地址配置不一致
- code 已过期或已使用
- state 验证失败
- 网络问题导致回调失败

### 5.3 用户信息获取失败

**问题**：回调成功但无法获取用户信息

**排查步骤**：
1. 检查 access_token 是否获取成功
2. 检查 scope 权限是否足够
3. 检查第三方平台 API 是否正常
4. 检查网络连接

**常见原因**：
- access_token 无效
- scope 权限不足
- 第三方平台 API 异常
- 网络超时

### 5.4 Token 生成失败

**问题**：用户信息获取成功但 token 生成失败

**排查步骤**：
1. 检查 token 生成逻辑
2. 检查数据库连接
3. 检查用户创建/更新逻辑
4. 检查应用日志

**常见原因**：
- 数据库连接失败
- 用户信息不完整
- Token 生成配置错误
- 业务逻辑异常

## 6. 自动化测试

### 6.1 单元测试

创建单元测试验证核心逻辑：

```java
@SpringBootTest
class OAuth2LoginServiceTest {
    
    @Autowired
    private OAuth2LoginService loginService;
    
    @Test
    void testGenerateAuthUrl() {
        String authUrl = loginService.generateAuthUrl("gitee");
        
        assertNotNull(authUrl);
        assertTrue(authUrl.startsWith("https://gitee.com/oauth/authorize"));
        assertTrue(authUrl.contains("client_id="));
        assertTrue(authUrl.contains("redirect_uri="));
        assertTrue(authUrl.contains("state="));
    }
    
    @Test
    void testHandleCallback() {
        // Mock 第三方平台响应
        // 测试回调处理逻辑
    }
}
```

### 6.2 集成测试

创建集成测试验证完整流程：

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OAuth2LoginIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testOAuth2LoginFlow() throws Exception {
        // 1. 测试授权接口
        MvcResult result = mockMvc.perform(get("/oauth2/authorize/gitee"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        
        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        assertTrue(location.contains("gitee.com"));
        
        // 2. 模拟回调（需要 mock 第三方平台响应）
        // ...
    }
}
```

### 6.3 端到端测试

使用 Selenium 或 Playwright 进行端到端测试：

```java
@Test
void testGiteeLoginE2E() {
    // 1. 打开登录页面
    driver.get("http://127.0.0.1:19180/login");
    
    // 2. 点击 Gitee 登录按钮
    driver.findElement(By.id("gitee-login-btn")).click();
    
    // 3. 在 Gitee 授权页面输入账号密码
    driver.findElement(By.id("user_login")).sendKeys("test_user");
    driver.findElement(By.id("user_password")).sendKeys("test_password");
    driver.findElement(By.name("commit")).click();
    
    // 4. 点击授权按钮
    driver.findElement(By.name("authorize")).click();
    
    // 5. 验证是否跳转回应用并登录成功
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.urlContains("/dashboard"));
    
    assertTrue(driver.getCurrentUrl().contains("/dashboard"));
}
```

## 7. 性能测试

### 7.1 压力测试

使用 JMeter 或 Gatling 进行压力测试：

**测试场景**：
- 并发授权请求
- 并发回调处理
- Token 生成性能

**性能指标**：
- 响应时间（P50、P95、P99）
- 吞吐量（TPS/QPS）
- 错误率
- 资源使用率（CPU、内存）

### 7.2 性能优化建议

- 使用缓存减少第三方 API 调用
- 异步处理用户信息获取
- 优化数据库查询
- 使用连接池
- 启用 HTTP/2

## 8. 安全测试

### 8.1 CSRF 攻击测试

验证 state 参数是否正确实现 CSRF 防护：

```bash
# 尝试使用错误的 state
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=xxx&state=wrong_state"
# 预期：返回错误
```

### 8.2 重放攻击测试

验证 code 是否只能使用一次：

```bash
# 第一次使用 code
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=xxx&state=xxx"
# 预期：成功

# 第二次使用相同的 code
curl "http://127.0.0.1:19180/oauth2/callback/gitee?code=xxx&state=xxx"
# 预期：失败
```

### 8.3 敏感信息泄露测试

检查日志和响应中是否包含敏感信息：

- [ ] client-secret 未在日志中输出
- [ ] access_token 未在日志中输出
- [ ] 用户密码未在日志中输出
- [ ] 错误信息不包含敏感信息

## 9. 参考资料

- [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [JustAuth 官方文档](https://justauth.wiki)
- [Spring Security OAuth2 文档](https://spring.io/projects/spring-security-oauth)

---

**文档版本**：v1.0.0  
**更新日期**：2026-03-09
