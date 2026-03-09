# Github OAuth2 登录申请指南

## 1. 概述

Github 是全球最大的代码托管平台和开发者社区。通过 Github OAuth2 登录，用户可以使用 Github 账号快速登录您的应用。

**官方网站**：https://github.com  
**开发者设置**：https://github.com/settings/developers

## 2. 前置条件

- [x] Github 账号
- [x] 应用主页地址
- [ ] 应用图标（可选）

## 3. 申请流程

### 3.1 创建 OAuth App

1. 访问 [Github Developer Settings](https://github.com/settings/developers)
2. 点击左侧菜单的 "OAuth Apps"
3. 点击右上角的 "New OAuth App" 按钮

### 3.2 填写应用信息

填写以下信息：

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| Application name | 应用名称 | My Application | ✓ |
| Homepage URL | 应用主页地址 | https://example.com | ✓ |
| Application description | 应用描述 | 这是一个测试应用 | ✗ |
| Authorization callback URL | OAuth2 回调地址 | http://example.com/oauth2/callback/github | ✓ |

**重要提示**：
- Authorization callback URL 必须与您在配置文件中设置的 `redirect-uri` 完全一致
- 回调地址格式：`http://your-domain/oauth2/callback/github`
- 本地测试可以使用：`http://127.0.0.1:19180/oauth2/callback/github`

### 3.3 获取凭证

1. 点击 "Register application" 完成创建
2. 在应用详情页面，您将看到 **Client ID**
3. 点击 "Generate a new client secret" 生成 **Client Secret**
4. ⚠️ **重要**：Client Secret 只显示一次，请立即复制并妥善保管

### 3.4 配置权限

Github OAuth2 支持以下权限范围（Scope）：

| Scope | 说明 | 推荐 |
|-------|------|------|
| (no scope) | 只读访问公开信息 | ✗ |
| user | 访问用户的个人信息 | ✓ |
| user:email | 访问用户的邮箱地址 | ✓ |
| read:user | 只读访问用户信息 | ✗ |
| repo | 访问用户的仓库信息 | ✗ |
| public_repo | 访问公开仓库 | ✗ |

**推荐配置**：对于登录功能，使用 `user` 或 `user:email` 权限即可。

## 4. 配置示例

### 4.1 application.yml 配置

```yaml
plugin:
  oauth:
    third-party:
      login:
        github:
          enabled: true
          client-id: Iv1.a629723d4e5f6a7b
          client-secret: 3d7a6d8c4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/github
          scope: user
```

### 4.2 配置说明

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用 Github 登录 | ✓ |
| client-id | Github 应用的 Client ID | ✓ |
| client-secret | Github 应用的 Client Secret | ✓ |
| redirect-uri | 授权回调地址 | ✓ |
| scope | 权限范围，默认 user | ✗ |

## 5. 测试

### 5.1 本地测试

1. 启动您的应用
2. 访问授权接口：
   ```
   GET http://localhost:19180/oauth2/authorize/github
   ```
3. 浏览器会跳转到 Github 授权页面
4. 点击"Authorize"授权
5. 授权成功后会自动跳转回您的应用

### 5.2 验证登录

检查应用日志，确认登录成功：

```
INFO  - OAuth2 登录成功: platform=github, userId=12345678
INFO  - 用户信息: username=zhangsan, email=zhangsan@users.noreply.github.com
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**问题描述**：回调地址不匹配错误

**错误信息**：
```
The redirect_uri MUST match the registered callback URL for this application.
```

**解决方案**：
1. 检查配置文件中的 `redirect-uri` 参数
2. 登录 Github Developer Settings，检查 OAuth App 的回调地址配置
3. 确保两者完全一致（包括协议、域名、端口、路径）

### 6.2 incorrect_client_credentials

**问题描述**：Client ID 或 Client Secret 错误

**错误信息**：
```json
{
  "error": "incorrect_client_credentials",
  "error_description": "The client_id and/or client_secret passed are incorrect."
}
```

**解决方案**：
1. 检查配置文件中的 `client-id` 和 `client-secret`
2. 登录 Github Developer Settings，确认凭证是否正确
3. 如果 Client Secret 丢失，需要重新生成

### 6.3 access_denied

**问题描述**：用户拒绝授权

**错误信息**：
```
error=access_denied&error_description=The+user+has+denied+your+application+access.
```

**原因**：用户在授权页面点击了"Cancel"按钮

**解决方案**：
- 这是正常的用户行为，应用应该友好地提示用户需要授权才能登录
- 可以提供重新授权的链接

### 6.4 rate limit exceeded

**问题描述**：API 调用频率超限

**错误信息**：
```json
{
  "message": "API rate limit exceeded"
}
```

**解决方案**：
1. Github 对未认证请求有频率限制（60次/小时）
2. 认证后的请求限制为 5000次/小时
3. 实现请求缓存和频率控制
4. 考虑使用 Github App 代替 OAuth App（更高的频率限制）

## 7. 高级配置

### 7.1 获取用户邮箱

如果需要获取用户的邮箱地址，需要：

1. 在 scope 中添加 `user:email`：
   ```yaml
   scope: user,user:email
   ```

2. 调用 Github API 获取邮箱列表：
   ```
   GET https://api.github.com/user/emails
   ```

### 7.2 获取用户仓库

如果需要访问用户的仓库信息，需要：

1. 在 scope 中添加 `repo` 或 `public_repo`：
   ```yaml
   scope: user,repo
   ```

2. 调用 Github API 获取仓库列表：
   ```
   GET https://api.github.com/user/repos
   ```

## 8. 最佳实践

### 8.1 安全建议

1. **保护 Client Secret**
   - 不要将 Client Secret 提交到代码仓库
   - 使用环境变量或配置中心管理
   - 定期更换 Client Secret

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS 协议
   - Github 强烈建议使用 HTTPS

3. **最小权限原则**
   - 只请求必要的权限
   - 避免请求过多的 scope

### 8.2 用户体验优化

1. **清晰的授权说明**
   - 说明为什么需要 Github 登录
   - 说明需要获取哪些权限

2. **处理邮箱隐私**
   - Github 用户可能使用 `@users.noreply.github.com` 邮箱
   - 应用应该能够处理这种情况

3. **头像处理**
   - Github 提供用户头像 URL
   - 可以缓存头像以提高性能

## 9. Github App vs OAuth App

### 9.1 区别

| 特性 | OAuth App | Github App |
|------|-----------|------------|
| 安装方式 | 用户授权 | 组织/用户安装 |
| 权限粒度 | 粗粒度 | 细粒度 |
| API 频率限制 | 5000次/小时 | 15000次/小时 |
| Webhook | 不支持 | 支持 |
| 推荐场景 | 简单登录 | 复杂集成 |

### 9.2 选择建议

- **使用 OAuth App**：只需要用户登录功能
- **使用 Github App**：需要深度集成 Github 功能

## 10. 参考资料

- [Github OAuth Apps 文档](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [Github API 文档](https://docs.github.com/en/rest)
- [Github Developer Settings](https://github.com/settings/developers)
- [JustAuth 文档](https://justauth.wiki)

## 11. 更新日志

- 2026-03-09：初始版本
- 文档版本：v1.0.0

## 12. 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

1. 查看 [Github 帮助文档](https://docs.github.com)
2. 在 [Github Community](https://github.community) 提问
3. 联系 Github Support

---

**提示**：本文档基于 Github OAuth Apps 编写，如有变更请以官方文档为准。
