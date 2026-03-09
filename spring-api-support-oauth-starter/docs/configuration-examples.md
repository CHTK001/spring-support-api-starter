# OAuth2 第三方登录配置示例

## 1. 概述

本文档提供 OAuth2 第三方登录的完整配置示例，帮助开发者快速集成第三方登录功能。

**配置前缀**：`plugin.oauth.third-party.login`

**支持的平台**：
- Gitee（码云）
- Github
- 微信开放平台（扫码登录）
- 微信公众号（网页授权）
- 微信小程序
- 企业微信
- Google
- 百度、阿里云、淘宝、钉钉、微博、QQ 等

## 2. 基础配置

### 2.1 最小配置

最简单的配置示例（以 Gitee 为例）：

```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true
          client-id: your_client_id
          client-secret: your_client_secret
          redirect-uri: http://your-domain/oauth2/callback/gitee
```

### 2.2 完整配置

包含所有可选参数的完整配置：

```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true                                          # 是否启用，默认 false
          client-id: your_client_id                              # 应用 ID（必填）
          client-secret: your_client_secret                      # 应用密钥（必填）
          redirect-uri: http://your-domain/oauth2/callback/gitee # 回调地址（必填）
          scope: user_info                                       # 权限范围（可选）
```

### 2.3 配置参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| enabled | boolean | ✓ | false | 是否启用该平台登录 |
| client-id | string | ✓ | - | 第三方平台分配的应用 ID |
| client-secret | string | ✓ | - | 第三方平台分配的应用密钥 |
| redirect-uri | string | ✓ | - | OAuth2 授权回调地址 |
| scope | string | ✗ | - | 权限范围，不同平台有不同的默认值 |

## 3. 单平台配置示例

### 3.1 Gitee（码云）

```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true
          client-id: f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943
          client-secret: 158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/gitee
          scope: user_info
```

**申请指南**：[Gitee 申请指南](./third-party-login/gitee.md)

### 3.2 Github

```yaml
plugin:
  oauth:
    third-party:
      login:
        github:
          enabled: true
          client-id: Ov23liABCDEFGHIJKLMN
          client-secret: 1234567890abcdef1234567890abcdef12345678
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/github
          scope: user:email
```

**申请指南**：[Github 申请指南](./third-party-login/github.md)

### 3.3 微信开放平台（扫码登录）

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-open:
          enabled: true
          app-id: wx1234567890abcdef
          app-secret: 1234567890abcdef1234567890abcdef
          redirect-uri: http://your-domain/oauth2/callback/wechat-open
```

**申请指南**：[微信开放平台申请指南](./third-party-login/wechat-open.md)

### 3.4 微信公众号（网页授权）

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-mp:
          enabled: true
          app-id: wx1234567890abcdef
          app-secret: 1234567890abcdef1234567890abcdef
          redirect-uri: http://your-domain/oauth2/callback/wechat-mp
```

**申请指南**：[微信公众号申请指南](./third-party-login/wechat-mp.md)

### 3.5 微信小程序

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-mini:
          enabled: true
          app-id: wx1234567890abcdef
          app-secret: 1234567890abcdef1234567890abcdef
```

**注意**：微信小程序登录不需要配置 `redirect-uri`，因为小程序使用 code 换取 session_key 的方式。

**申请指南**：[微信小程序申请指南](./third-party-login/wechat-mini.md)

### 3.6 企业微信

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-enterprise:
          enabled: true
          corp-id: ww1234567890abcdef
          corp-secret: 1234567890abcdef1234567890abcdef
          agent-id: 1000001
          redirect-uri: http://your-domain/oauth2/callback/wechat-enterprise
```

**申请指南**：[企业微信申请指南](./third-party-login/wechat-enterprise.md)

### 3.7 Google

```yaml
plugin:
  oauth:
    third-party:
      login:
        google:
          enabled: true
          client-id: 123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com
          client-secret: GOCSPX-abcdefghijklmnopqrstuvwx
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/google
          scope: openid email profile
```

**申请指南**：[Google 申请指南](./third-party-login/google.md)

### 3.8 其他平台

其他平台（百度、阿里云、淘宝、钉钉、微博、QQ等）的配置方式类似，具体参数请参考 [JustAuth 官方文档](https://justauth.wiki)。

### 3.9 配置参数详细说明

#### 3.9.1 通用参数

所有平台都支持以下通用参数：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| enabled | boolean | 是 | false | 是否启用该平台的 OAuth2 登录 |
| client-id | string | 是 | - | 第三方平台分配的应用标识（不同平台名称可能不同：Client ID、App ID、API Key 等） |
| client-secret | string | 是 | - | 第三方平台分配的应用密钥（不同平台名称可能不同：Client Secret、App Secret、Secret Key 等） |
| redirect-uri | string | 是* | - | OAuth2 授权回调地址（微信小程序除外） |
| scope | string | 否 | 平台默认 | 权限范围，多个权限用逗号或空格分隔（具体格式取决于平台） |

**注意**：
- `client-id` 和 `client-secret` 在不同平台的命名可能不同，但配置时统一使用这两个参数名
- `redirect-uri` 必须与第三方平台配置的回调地址完全一致
- 微信小程序不需要配置 `redirect-uri`

#### 3.9.2 Gitee 特有参数

Gitee 使用标准的 OAuth2 参数，无特殊参数。

**Scope 说明**：
- 默认：`user_info`
- 可选值：`user_info`、`projects`、`pull_requests`、`issues`、`notes`、`keys`、`hook`、`groups`、`gists`、`enterprises`
- 多个 scope 使用逗号分隔

#### 3.9.3 Github 特有参数

Github 使用标准的 OAuth2 参数，无特殊参数。

**Scope 说明**：
- 默认：无（仅公开只读访问）
- 推荐：`user:email` 或 `user:email,read:user`
- 多个 scope 使用逗号分隔
- 常用 scope：`user`、`user:email`、`user:follow`、`read:user`、`repo`、`public_repo`、`gist`、`read:org`

#### 3.9.4 微信开放平台特有参数

微信开放平台使用 `app-id` 和 `app-secret` 而不是 `client-id` 和 `client-secret`，但配置时仍使用通用参数名。

**Scope 说明**：
- 固定值：`snsapi_login`（网站应用微信登录）
- 不支持其他 scope

**特殊要求**：
- 回调地址必须使用 https://（本地测试除外）
- 需要通过微信开放平台审核
- 需要配置网站应用的授权回调域

#### 3.9.5 微信公众号特有参数

微信公众号使用 `app-id` 和 `app-secret`。

**Scope 说明**：
- `snsapi_base`：静默授权，仅获取 openid，不弹出授权页面
- `snsapi_userinfo`：弹窗授权，获取用户详细信息（昵称、头像、性别等）

**特殊要求**：
- 需要在微信公众平台配置网页授权域名
- 回调地址的域名必须与授权域名一致
- 服务号才支持网页授权

#### 3.9.6 微信小程序特有参数

微信小程序使用 `app-id` 和 `app-secret`。

**特殊说明**：
- 不需要配置 `redirect-uri`
- 不需要配置 `scope`
- 使用小程序 code 换取 session_key 的方式
- 需要在小程序后台配置服务器域名

#### 3.9.7 企业微信特有参数

企业微信除了通用参数外，还需要额外的参数：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| corp-id | string | 是 | 企业 ID，在企业微信管理后台获取 |
| corp-secret | string | 是 | 应用的凭证密钥 |
| agent-id | string | 是 | 应用的 AgentId |

**Scope 说明**：
- `snsapi_base`：静默授权，获取成员基本信息
- `snsapi_userinfo`：弹窗授权，获取成员详细信息
- `snsapi_privateinfo`：获取成员敏感信息

**特殊要求**：
- 需要在企业微信管理后台创建自建应用
- 回调地址需要在应用的网页授权及JS-SDK中配置

#### 3.9.8 Google 特有参数

Google 使用标准的 OAuth2 参数，无特殊参数。

**Scope 说明**：
- 推荐：`openid email profile`
- 多个 scope 使用空格分隔
- 常用 scope：
  - `openid`：OpenID Connect 登录
  - `email`：访问用户邮箱地址
  - `profile`：访问用户基本资料
  - `https://www.googleapis.com/auth/userinfo.email`：查看用户邮箱
  - `https://www.googleapis.com/auth/userinfo.profile`：查看用户个人信息

**特殊要求**：
- 需要在 Google Cloud Console 创建 OAuth 2.0 客户端 ID
- 回调地址必须在 Google Cloud Console 中配置
- 支持多个回调地址

## 4. 多平台配置示例

### 4.1 同时启用多个平台

```yaml
plugin:
  oauth:
    third-party:
      login:
        # Gitee 登录
        gitee:
          enabled: true
          client-id: gitee_client_id
          client-secret: gitee_client_secret
          redirect-uri: http://your-domain/oauth2/callback/gitee
        
        # Github 登录
        github:
          enabled: true
          client-id: github_client_id
          client-secret: github_client_secret
          redirect-uri: http://your-domain/oauth2/callback/github
        
        # 微信开放平台登录
        wechat-open:
          enabled: true
          app-id: wechat_app_id
          app-secret: wechat_app_secret
          redirect-uri: http://your-domain/oauth2/callback/wechat-open
        
        # Google 登录
        google:
          enabled: true
          client-id: google_client_id
          client-secret: google_client_secret
          redirect-uri: http://your-domain/oauth2/callback/google
```

### 4.2 不同环境配置

#### 开发环境（application-dev.yml）

```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true
          client-id: dev_client_id
          client-secret: dev_client_secret
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/gitee
```

#### 生产环境（application-prod.yml）

```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true
          client-id: prod_client_id
          client-secret: prod_client_secret
          redirect-uri: https://your-domain.com/oauth2/callback/gitee
```

## 5. 回调地址配置

### 5.1 回调地址格式

回调地址格式：`{protocol}://{domain}:{port}/oauth2/callback/{platform}`

**示例**：
- 本地开发：`http://127.0.0.1:19180/oauth2/callback/gitee`
- 生产环境：`https://example.com/oauth2/callback/gitee`

### 5.2 平台标识（platform）

| 平台 | platform 值 | 回调地址示例 |
|------|-------------|--------------|
| Gitee | gitee | http://domain/oauth2/callback/gitee |
| Github | github | http://domain/oauth2/callback/github |
| 微信开放平台 | wechat-open | http://domain/oauth2/callback/wechat-open |
| 微信公众号 | wechat-mp | http://domain/oauth2/callback/wechat-mp |
| 微信小程序 | wechat-mini | 不需要回调地址 |
| 企业微信 | wechat-enterprise | http://domain/oauth2/callback/wechat-enterprise |
| Google | google | http://domain/oauth2/callback/google |

### 5.3 回调地址注意事项

1. **协议匹配**
   - 本地测试可以使用 `http://`
   - 生产环境建议使用 `https://`
   - 部分平台（如微信）要求必须使用 `https://`

2. **域名匹配**
   - 回调地址必须与第三方平台配置完全一致
   - 包括协议、域名、端口、路径
   - 本地测试建议使用 `127.0.0.1` 而不是 `localhost`

3. **端口号**
   - 如果使用默认端口（80/443），可以省略端口号
   - 非默认端口必须明确指定

## 6. 环境变量配置

### 6.1 使用环境变量

为了安全起见，建议使用环境变量管理敏感信息：

```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true
          client-id: ${GITEE_CLIENT_ID}
          client-secret: ${GITEE_CLIENT_SECRET}
          redirect-uri: ${GITEE_REDIRECT_URI}
```

### 6.2 设置环境变量

**Linux/Mac**：
```bash
export GITEE_CLIENT_ID=your_client_id
export GITEE_CLIENT_SECRET=your_client_secret
export GITEE_REDIRECT_URI=http://127.0.0.1:19180/oauth2/callback/gitee
```

**Windows**：
```cmd
set GITEE_CLIENT_ID=your_client_id
set GITEE_CLIENT_SECRET=your_client_secret
set GITEE_REDIRECT_URI=http://127.0.0.1:19180/oauth2/callback/gitee
```

## 7. 配置验证

### 7.1 启动日志检查

应用启动时，会输出已启用的第三方登录平台：

```
INFO  - 第三方登录配置加载完成
INFO  - 已启用的平台: [gitee, github, wechat-open]
INFO  - Gitee 登录配置: client-id=f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943, redirect-uri=http://127.0.0.1:19180/oauth2/callback/gitee
```

### 7.2 测试授权流程

访问授权接口测试配置：

```bash
# 测试 Gitee 登录
GET http://localhost:19180/oauth2/authorize/gitee

# 浏览器会跳转到 Gitee 授权页面
# 授权成功后会回调到配置的 redirect-uri
```

### 7.3 配置验证清单

在部署前，请按照以下清单验证配置：

#### 7.3.1 基础配置验证

- [ ] 配置文件位置正确（`src/main/resources/application.yml`）
- [ ] 配置前缀正确（`plugin.oauth.third-party.login`）
- [ ] YAML 缩进正确（使用空格，不使用 Tab）
- [ ] 所有必填参数已配置（enabled、client-id、client-secret、redirect-uri）

#### 7.3.2 平台配置验证

对于每个启用的平台，验证：

- [ ] `enabled` 设置为 `true`
- [ ] `client-id` 和 `client-secret` 正确无误
- [ ] `redirect-uri` 与第三方平台配置完全一致
- [ ] `scope` 配置正确（如果需要）
- [ ] 第三方平台应用状态正常（未被禁用或删除）

#### 7.3.3 回调地址验证

- [ ] 回调地址格式正确：`{protocol}://{domain}:{port}/oauth2/callback/{platform}`
- [ ] 协议匹配（http:// 或 https://）
- [ ] 域名匹配（不要混用 localhost 和 127.0.0.1）
- [ ] 端口号正确（如果不是默认端口）
- [ ] 平台标识正确（gitee、github、wechat-open 等）
- [ ] 回调地址已在第三方平台配置

#### 7.3.4 环境变量验证

如果使用环境变量：

- [ ] 环境变量已设置
- [ ] 环境变量名称正确
- [ ] 环境变量值正确
- [ ] IDE 运行配置中已设置环境变量（如果在 IDE 中运行）

### 7.4 自动化验证脚本

#### 7.4.1 配置文件语法验证

使用 YAML 验证工具检查配置文件语法：

```bash
# 使用 yamllint（需要先安装）
yamllint src/main/resources/application.yml

# 或使用在线工具
# https://www.yamllint.com/
```

#### 7.4.2 配置完整性验证

创建验证脚本 `verify-oauth-config.sh`：

```bash
#!/bin/bash

CONFIG_FILE="src/main/resources/application.yml"

echo "验证 OAuth2 配置..."

# 检查配置文件是否存在
if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ 配置文件不存在: $CONFIG_FILE"
    exit 1
fi

# 检查配置前缀
if ! grep -q "plugin:" "$CONFIG_FILE"; then
    echo "❌ 未找到配置前缀: plugin"
    exit 1
fi

# 检查已启用的平台
PLATFORMS=$(grep -A 100 "third-party:" "$CONFIG_FILE" | grep "enabled: true" -B 1 | grep -v "enabled" | grep -v "^--$" | awk '{print $1}' | tr -d ':')

if [ -z "$PLATFORMS" ]; then
    echo "⚠️  未启用任何第三方登录平台"
    exit 0
fi

echo "✓ 已启用的平台:"
for platform in $PLATFORMS; do
    echo "  - $platform"
    
    # 检查必填参数
    if ! grep -A 10 "$platform:" "$CONFIG_FILE" | grep -q "client-id:"; then
        echo "    ❌ 缺少 client-id"
    else
        echo "    ✓ client-id 已配置"
    fi
    
    if ! grep -A 10 "$platform:" "$CONFIG_FILE" | grep -q "client-secret:"; then
        echo "    ❌ 缺少 client-secret"
    else
        echo "    ✓ client-secret 已配置"
    fi
    
    # 微信小程序不需要 redirect-uri
    if [ "$platform" != "wechat-mini" ]; then
        if ! grep -A 10 "$platform:" "$CONFIG_FILE" | grep -q "redirect-uri:"; then
            echo "    ❌ 缺少 redirect-uri"
        else
            echo "    ✓ redirect-uri 已配置"
        fi
    fi
done

echo ""
echo "✓ 配置验证完成"
```

#### 7.4.3 运行时验证

创建 Spring Boot 配置验证类：

```java
@Component
@Slf4j
public class OAuth2ConfigValidator implements ApplicationRunner {
    
    @Autowired
    private ThirdPartyLoginProperties properties;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("开始验证 OAuth2 配置...");
        
        int enabledCount = 0;
        List<String> errors = new ArrayList<>();
        
        // 验证 Gitee
        if (properties.getGitee() != null && properties.getGitee().isEnabled()) {
            enabledCount++;
            validatePlatform("Gitee", properties.getGitee(), errors);
        }
        
        // 验证 Github
        if (properties.getGithub() != null && properties.getGithub().isEnabled()) {
            enabledCount++;
            validatePlatform("Github", properties.getGithub(), errors);
        }
        
        // ... 验证其他平台
        
        if (enabledCount == 0) {
            log.warn("未启用任何第三方登录平台");
        } else {
            log.info("已启用 {} 个第三方登录平台", enabledCount);
        }
        
        if (!errors.isEmpty()) {
            log.error("OAuth2 配置验证失败:");
            errors.forEach(log::error);
            throw new IllegalStateException("OAuth2 配置验证失败，请检查配置");
        }
        
        log.info("OAuth2 配置验证通过");
    }
    
    private void validatePlatform(String platformName, ThirdPartyLoginProperties.Base config, List<String> errors) {
        if (StringUtils.isBlank(config.getClientId())) {
            errors.add(platformName + ": client-id 未配置");
        }
        
        if (StringUtils.isBlank(config.getClientSecret())) {
            errors.add(platformName + ": client-secret 未配置");
        }
        
        if (StringUtils.isBlank(config.getRedirectUri())) {
            errors.add(platformName + ": redirect-uri 未配置");
        } else {
            // 验证回调地址格式
            if (!config.getRedirectUri().startsWith("http://") && 
                !config.getRedirectUri().startsWith("https://")) {
                errors.add(platformName + ": redirect-uri 必须以 http:// 或 https:// 开头");
            }
            
            if (!config.getRedirectUri().contains("/oauth2/callback/")) {
                errors.add(platformName + ": redirect-uri 格式不正确，应包含 /oauth2/callback/");
            }
        }
    }
}
```

### 7.5 集成测试

创建集成测试验证 OAuth2 配置：

```java
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2LoginControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGiteeAuthorize() throws Exception {
        mockMvc.perform(get("/oauth2/authorize/gitee"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://gitee.com/oauth/authorize*"));
    }
    
    @Test
    void testGithubAuthorize() throws Exception {
        mockMvc.perform(get("/oauth2/authorize/github"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://github.com/login/oauth/authorize*"));
    }
    
    // ... 其他平台测试
}
```

### 7.6 生产环境验证

部署到生产环境前，请完成以下验证：

#### 7.6.1 配置检查

- [ ] 使用生产环境的 client-id 和 client-secret
- [ ] redirect-uri 使用生产域名和 HTTPS
- [ ] 环境变量已在生产环境设置
- [ ] 敏感信息未硬编码在配置文件中

#### 7.6.2 网络检查

- [ ] 应用服务器可以访问第三方平台 API
- [ ] 防火墙规则允许 HTTPS 出站连接
- [ ] DNS 解析正常

#### 7.6.3 SSL 证书检查

- [ ] SSL 证书有效且未过期
- [ ] SSL 证书域名与回调地址域名匹配
- [ ] 中间证书链完整

#### 7.6.4 功能测试

- [ ] 授权流程正常
- [ ] 回调处理正常
- [ ] 用户信息获取正常
- [ ] Token 生成和存储正常
- [ ] 错误处理正常

### 7.7 监控和告警

建议配置以下监控指标：

- OAuth2 授权成功率
- OAuth2 授权失败率
- 各平台授权耗时
- 回调处理成功率
- Token 生成成功率

配置告警规则：

- 授权失败率 > 10%
- 授权耗时 > 5秒
- 回调处理失败率 > 5%

## 8. 参考资料

### 8.1 平台申请指南

- [Gitee 申请指南](./third-party-login/gitee.md)
- [Github 申请指南](./third-party-login/github.md)
- [微信开放平台申请指南](./third-party-login/wechat-open.md)
- [微信公众号申请指南](./third-party-login/wechat-mp.md)
- [微信小程序申请指南](./third-party-login/wechat-mini.md)
- [企业微信申请指南](./third-party-login/wechat-enterprise.md)
- [Google 申请指南](./third-party-login/google.md)

### 8.2 相关文档

- [测试指南](./testing-guide.md)
- [常见问题 FAQ](./faq.md)
- [JustAuth 官方文档](https://justauth.wiki)

## 9. 更新日志

- 2026-03-09：初始版本，包含基础配置示例
- 文档版本：v1.0.0

## 10. 常见配置错误

### 10.1 回调地址配置错误

#### 错误1：回调地址不匹配

**错误现象**：
```
redirect_uri_mismatch
The redirect URI provided does not match
```

**原因**：
- 配置文件中的 `redirect-uri` 与第三方平台配置不一致
- 协议不匹配（http vs https）
- 域名不匹配（localhost vs 127.0.0.1）
- 端口号不匹配或遗漏
- 路径不匹配（大小写敏感）

**解决方案**：
1. 检查配置文件中的 `redirect-uri`
2. 登录第三方平台开发者中心，检查应用的回调地址配置
3. 确保两者完全一致，包括：
   - 协议（http:// 或 https://）
   - 域名（example.com）
   - 端口号（:8080）
   - 路径（/oauth2/callback/gitee）

**正确示例**：
```yaml
# 配置文件
redirect-uri: http://127.0.0.1:19180/oauth2/callback/gitee

# 第三方平台配置
回调地址：http://127.0.0.1:19180/oauth2/callback/gitee
```

#### 错误2：使用 localhost 而不是 127.0.0.1

**错误现象**：
部分平台（如 Gitee）不支持 localhost 作为回调地址。

**解决方案**：
使用 `127.0.0.1` 代替 `localhost`：
```yaml
# 错误
redirect-uri: http://localhost:19180/oauth2/callback/gitee

# 正确
redirect-uri: http://127.0.0.1:19180/oauth2/callback/gitee
```

#### 错误3：生产环境未使用 HTTPS

**错误现象**：
微信等平台要求生产环境必须使用 HTTPS。

**解决方案**：
```yaml
# 开发环境可以使用 HTTP
redirect-uri: http://127.0.0.1:19180/oauth2/callback/wechat-open

# 生产环境必须使用 HTTPS
redirect-uri: https://your-domain.com/oauth2/callback/wechat-open
```

### 10.2 Client ID / Secret 配置错误

#### 错误1：Client ID 或 Secret 错误

**错误现象**：
```
invalid_client
Client authentication failed
```

**原因**：
- `client-id` 或 `client-secret` 配置错误
- 复制时包含了多余的空格或换行
- 使用了错误的凭证（如测试环境的凭证用于生产环境）

**解决方案**：
1. 登录第三方平台开发者中心，重新获取凭证
2. 确保复制时没有多余的空格或换行
3. 检查是否使用了正确环境的凭证

#### 错误2：凭证过期或被重置

**错误现象**：
之前可以正常登录，突然无法登录。

**解决方案**：
1. 检查第三方平台是否重置了应用凭证
2. 重新获取 `client-secret` 并更新配置

### 10.3 权限范围（Scope）配置错误

#### 错误1：Scope 格式错误

**错误现象**：
授权失败或无法获取用户信息。

**原因**：
- 不同平台的 scope 分隔符不同
- Gitee/Github 使用逗号：`user_info,projects`
- Google 使用空格：`openid email profile`

**解决方案**：
按照平台要求使用正确的分隔符：
```yaml
# Gitee - 使用逗号
gitee:
  scope: user_info,projects,pull_requests

# Google - 使用空格
google:
  scope: openid email profile
```

#### 错误2：请求了未授权的 Scope

**错误现象**：
```
invalid_scope
The requested scope is invalid
```

**原因**：
- 请求的权限未在第三方平台应用配置中勾选
- 请求的权限不存在或拼写错误

**解决方案**：
1. 登录第三方平台开发者中心
2. 检查应用的权限配置
3. 勾选需要的权限
4. 确保 scope 拼写正确

### 10.4 平台特定配置错误

#### 错误1：微信公众号授权域名未配置

**错误现象**：
```
redirect_uri 参数错误
```

**解决方案**：
1. 登录微信公众平台
2. 进入"开发" -> "接口权限" -> "网页授权"
3. 配置授权回调域名（不包含 http:// 和路径）
4. 例如：`your-domain.com`

#### 错误2：企业微信缺少必要参数

**错误现象**：
配置不完整，无法启动。

**解决方案**：
企业微信需要配置三个参数：
```yaml
wechat-enterprise:
  enabled: true
  corp-id: ww1234567890abcdef      # 企业 ID
  corp-secret: 1234567890abcdef    # 应用密钥
  agent-id: 1000001                # 应用 AgentId
```

### 10.5 环境变量配置错误

#### 错误1：环境变量未设置

**错误现象**：
```
Could not resolve placeholder 'GITEE_CLIENT_ID'
```

**原因**：
配置文件使用了环境变量，但环境变量未设置。

**解决方案**：
```bash
# Linux/Mac
export GITEE_CLIENT_ID=your_client_id
export GITEE_CLIENT_SECRET=your_client_secret

# Windows
set GITEE_CLIENT_ID=your_client_id
set GITEE_CLIENT_SECRET=your_client_secret
```

#### 错误2：环境变量在 IDE 中未生效

**解决方案**：
1. 在 IDE 的运行配置中设置环境变量
2. 或者在 `application.yml` 中设置默认值：
```yaml
plugin:
  oauth:
    third-party:
      login:
        gitee:
          client-id: ${GITEE_CLIENT_ID:default_value}
```

### 10.6 配置未生效

#### 错误1：配置文件位置错误

**原因**：
配置文件不在正确的位置或文件名错误。

**解决方案**：
确保配置文件位于以下位置之一：
- `src/main/resources/application.yml`
- `src/main/resources/application.properties`
- `src/main/resources/application-{profile}.yml`

#### 错误2：配置前缀错误

**错误现象**：
配置未被加载。

**原因**：
使用了错误的配置前缀。

**解决方案**：
确保使用正确的配置前缀：
```yaml
# 正确
plugin:
  oauth:
    third-party:
      login:

# 错误（旧版本）
plugin:
  oauth:
    third:
      login:
```

#### 错误3：YAML 缩进错误

**错误现象**：
配置解析失败或配置未生效。

**原因**：
YAML 对缩进非常敏感，必须使用空格（不能使用 Tab）。

**解决方案**：
```yaml
# 正确 - 使用 2 个空格缩进
plugin:
  oauth:
    third-party:
      login:
        gitee:
          enabled: true

# 错误 - 缩进不一致
plugin:
 oauth:
   third-party:
     login:
      gitee:
        enabled: true
```

### 10.7 调试建议

#### 1. 启用调试日志

在 `application.yml` 中添加：
```yaml
logging:
  level:
    com.chua.starter.oauth: DEBUG
    me.zhyd.oauth: DEBUG
```

#### 2. 检查启动日志

应用启动时会输出已启用的平台：
```
INFO  - 第三方登录配置加载完成
INFO  - 已启用的平台: [gitee, github, wechat-open]
```

#### 3. 测试授权流程

访问授权接口测试：
```bash
curl http://localhost:19180/oauth2/authorize/gitee
```

#### 4. 检查回调日志

授权回调时会输出详细日志：
```
DEBUG - OAuth2 callback: platform=gitee, code=xxx, state=xxx
INFO  - OAuth2 登录成功: userId=xxx, username=xxx
```

---

**提示**：本文档提供的配置示例仅供参考，实际使用时请替换为您自己的应用凭证。
