# Gitee（码云）OAuth2 登录申请指南

## 1. 概述

Gitee（码云）是中国最大的代码托管平台，提供 Git 仓库管理、代码审查、问题跟踪等功能。通过 Gitee OAuth2 登录，用户可以使用 Gitee 账号快速登录您的应用。

**官方网站**：https://gitee.com  
**开发者中心**：https://gitee.com/oauth/applications

## 2. 前置条件

- [x] Gitee 账号（已实名认证）
- [x] 应用主页地址
- [ ] 应用图标（可选，建议尺寸：200x200）

## 3. 申请流程

### 3.1 创建应用

1. 访问 [Gitee 第三方应用](https://gitee.com/oauth/applications)
2. 登录您的 Gitee 账号
3. 点击右上角的"创建应用"按钮

### 3.2 填写应用信息

填写以下信息：

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 应用名称 | 您的应用名称 | My Application | ✓ |
| 应用主页 | 应用的主页地址 | https://example.com | ✓ |
| 应用描述 | 应用的简要描述 | 这是一个测试应用 | ✓ |
| 应用回调地址 | OAuth2 回调地址 | http://example.com/oauth2/callback/gitee | ✓ |
| 应用图标 | 应用图标（200x200） | - | ✗ |

**重要提示**：
- 应用回调地址必须与您在配置文件中设置的 `redirect-uri` 完全一致
- 回调地址格式：`http://your-domain/oauth2/callback/gitee`
- 本地测试可以使用：`http://127.0.0.1:19180/oauth2/callback/gitee`

### 3.3 获取凭证

提交后，您将获得：

- **Client ID**：应用的唯一标识（32位字符串）
- **Client Secret**：应用的密钥（64位字符串）

⚠️ **重要**：请妥善保管 Client Secret，不要泄露给他人或提交到代码仓库。

### 3.4 配置权限

Gitee OAuth2 支持以下权限范围（Scope）：

| Scope | 说明 | 默认 |
|-------|------|------|
| user_info | 访问用户的个人信息 | ✓ |
| projects | 访问用户的项目信息 | ✗ |
| pull_requests | 访问用户的 Pull Request | ✗ |
| issues | 访问用户的 Issue | ✗ |
| notes | 访问用户的评论 | ✗ |
| keys | 访问用户的 SSH 公钥 | ✗ |
| hook | 访问用户的 Webhook | ✗ |

**推荐配置**：对于登录功能，只需要 `user_info` 权限即可。

## 4. 配置示例

### 4.1 application.yml 配置

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

### 4.2 配置说明

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用 Gitee 登录 | ✓ |
| client-id | Gitee 应用的 Client ID | ✓ |
| client-secret | Gitee 应用的 Client Secret | ✓ |
| redirect-uri | 授权回调地址 | ✓ |
| scope | 权限范围，默认 user_info | ✗ |

## 5. 测试

### 5.1 本地测试

1. 启动您的应用
2. 访问授权接口：
   ```
   GET http://localhost:19180/oauth2/authorize/gitee
   ```
3. 浏览器会跳转到 Gitee 授权页面
4. 点击"同意授权"
5. 授权成功后会自动跳转回您的应用

### 5.2 验证登录

检查应用日志，确认登录成功：

```
INFO  - OAuth2 登录成功: platform=gitee, userId=12345678
INFO  - 用户信息: username=zhangsan, email=zhangsan@example.com
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**问题描述**：回调地址不匹配错误

**错误信息**：
```json
{
  "error": "redirect_uri_mismatch",
  "error_description": "The redirect_uri MUST match the registered callback URL for this application."
}
```

**原因**：配置的回调地址与 Gitee 应用配置不一致

**解决方案**：
1. 检查配置文件中的 `redirect-uri` 参数
2. 登录 Gitee 开发者中心，检查应用的回调地址配置
3. 确保两者完全一致（包括协议、域名、端口、路径）

### 6.2 invalid_client

**问题描述**：Client ID 或 Client Secret 错误

**错误信息**：
```json
{
  "error": "invalid_client",
  "error_description": "Client authentication failed"
}
```

**原因**：Client ID 或 Client Secret 配置错误

**解决方案**：
1. 检查配置文件中的 `client-id` 和 `client-secret`
2. 登录 Gitee 开发者中心，重新获取凭证
3. 确保复制时没有多余的空格或换行

### 6.3 access_denied

**问题描述**：用户拒绝授权

**错误信息**：
```json
{
  "error": "access_denied",
  "error_description": "The resource owner or authorization server denied the request"
}
```

**原因**：用户在授权页面点击了"拒绝"按钮

**解决方案**：
- 这是正常的用户行为，应用应该友好地提示用户需要授权才能登录
- 可以提供重新授权的链接

### 6.4 本地测试回调地址问题

**问题描述**：本地测试时无法使用 localhost

**解决方案**：
1. 使用 `http://127.0.0.1` 代替 `http://localhost`
2. 或者在 Gitee 应用配置中同时添加两个回调地址：
   - `http://127.0.0.1:19180/oauth2/callback/gitee`
   - `http://localhost:19180/oauth2/callback/gitee`

## 7. 最佳实践

### 7.1 安全建议

1. **保护 Client Secret**
   - 不要将 Client Secret 提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息
   - 定期更换 Client Secret

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS 协议
   - 避免在不安全的网络环境下传输敏感信息

3. **验证回调来源**
   - 验证 state 参数，防止 CSRF 攻击
   - 验证回调请求的来源

### 7.2 用户体验优化

1. **清晰的授权说明**
   - 在跳转到 Gitee 授权页面前，告知用户将要进行的操作
   - 说明需要获取哪些权限以及用途

2. **错误处理**
   - 提供友好的错误提示
   - 对于常见错误，提供解决建议

3. **登录状态管理**
   - 合理设置 Session 过期时间
   - 提供"记住我"功能

## 8. 参考资料

- [Gitee OAuth2 官方文档](https://gitee.com/api/v5/oauth_doc)
- [Gitee 第三方应用管理](https://gitee.com/oauth/applications)
- [Gitee API 文档](https://gitee.com/api/v5/swagger)
- [JustAuth 文档](https://justauth.wiki)

## 9. 更新日志

- 2026-03-09：初始版本
- 文档版本：v1.0.0

## 10. 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

1. 查看 [Gitee 帮助中心](https://gitee.com/help)
2. 在 [Gitee 社区](https://gitee.com/explore) 提问
3. 联系 Gitee 技术支持

---

**提示**：本文档基于 Gitee OAuth2 API v5 编写，如有变更请以官方文档为准。
