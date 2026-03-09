# Google OAuth2 登录申请指南

## 1. 概述

Google 是全球最大的搜索引擎和互联网服务提供商。通过 Google OAuth2 登录，用户可以使用 Google 账号快速登录您的应用。

**官方网站**：https://www.google.com  
**开发者控制台**：https://console.cloud.google.com

## 2. 前置条件

- [x] Google 账号
- [x] 应用名称
- [ ] 应用主页地址（可选）
- [ ] 隐私政策链接（可选，但推荐）

## 3. 申请流程

### 3.1 创建 Google Cloud 项目

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 登录您的 Google 账号
3. 点击顶部的项目选择器
4. 点击"新建项目"
5. 输入项目名称（如：My Application）
6. 点击"创建"

### 3.2 启用 Google+ API（可选）

对于基本登录功能，不需要启用额外的 API。如果需要访问更多用户信息：

1. 在左侧菜单选择"API和服务" → "库"
2. 搜索"Google+ API"
3. 点击"启用"

### 3.3 配置 OAuth 同意屏幕

1. 在左侧菜单选择"API和服务" → "OAuth 同意屏幕"
2. 选择用户类型：
   - **外部**：任何 Google 账号都可以登录（推荐）
   - **内部**：仅限组织内部用户
3. 点击"创建"
4. 填写应用信息：

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 应用名称 | 显示给用户的应用名称 | My Application | ✓ |
| 用户支持电子邮件 | 用户联系邮箱 | support@example.com | ✓ |
| 应用徽标 | 应用图标（120x120） | - | ✗ |
| 应用首页 | 应用主页地址 | https://example.com | ✗ |
| 应用隐私权政策链接 | 隐私政策页面 | https://example.com/privacy | ✗ |
| 应用服务条款链接 | 服务条款页面 | https://example.com/terms | ✗ |
| 已获授权的网域 | 授权域名 | example.com | ✗ |
| 开发者联系信息 | 开发者邮箱 | dev@example.com | ✓ |

5. 点击"保存并继续"
6. 在"范围"页面，点击"添加或移除范围"
7. 选择以下范围：
   - `openid`
   - `profile`
   - `email`
8. 点击"保存并继续"
9. 如果是外部应用，添加测试用户（可选）
10. 点击"保存并继续"完成配置

### 3.4 创建 OAuth 2.0 凭据

1. 在左侧菜单选择"API和服务" → "凭据"
2. 点击顶部的"创建凭据" → "OAuth 客户端 ID"
3. 选择应用类型："Web 应用"
4. 填写信息：

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 名称 | 凭据名称 | Web client 1 | ✓ |
| 已获授权的 JavaScript 来源 | 允许的来源域名 | http://localhost:19180 | ✗ |
| 已获授权的重定向 URI | OAuth2 回调地址 | http://localhost:19180/oauth2/callback/google | ✓ |

5. 点击"创建"
6. 在弹出的对话框中，您将看到：
   - **客户端 ID**（Client ID）
   - **客户端密钥**（Client Secret）
7. 点击"下载 JSON"保存凭据（可选）
8. 点击"确定"

⚠️ **重要**：请妥善保管客户端密钥，不要泄露给他人。

## 4. 配置示例

### 4.1 application.yml 配置

```yaml
plugin:
  oauth:
    third-party:
      login:
        google:
          enabled: true
          client-id: 123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com
          client-secret: GOCSPX-abcdefghijklmnopqrstuvwxyz
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/google
          scope: openid,profile,email
```

### 4.2 配置说明

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用 Google 登录 | ✓ |
| client-id | Google 应用的客户端 ID | ✓ |
| client-secret | Google 应用的客户端密钥 | ✓ |
| redirect-uri | 授权回调地址 | ✓ |
| scope | 权限范围 | ✗ |

### 4.3 权限范围（Scope）

| Scope | 说明 | 推荐 |
|-------|------|------|
| openid | OpenID Connect 登录 | ✓ |
| profile | 访问用户的基本资料信息 | ✓ |
| email | 访问用户的邮箱地址 | ✓ |

## 5. 测试

### 5.1 本地测试

1. 启动您的应用
2. 访问授权接口：
   ```
   GET http://localhost:19180/oauth2/authorize/google
   ```
3. 浏览器会跳转到 Google 授权页面
4. 选择 Google 账号并授权
5. 授权成功后会自动跳转回您的应用

### 5.2 验证登录

检查应用日志，确认登录成功：

```
INFO  - OAuth2 登录成功: platform=google, userId=123456789012345678901
INFO  - 用户信息: username=Zhang San, email=zhangsan@gmail.com
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**问题描述**：回调地址不匹配错误

**错误信息**：
```
Error 400: redirect_uri_mismatch
The redirect URI in the request, http://example.com/oauth2/callback/google, does not match the ones authorized for the OAuth client.
```

**解决方案**：
1. 检查配置文件中的 `redirect-uri` 参数
2. 登录 Google Cloud Console，检查 OAuth 2.0 客户端的重定向 URI 配置
3. 确保两者完全一致（包括协议、域名、端口、路径）
4. 注意：Google 区分 `http://localhost` 和 `http://127.0.0.1`

### 6.2 invalid_client

**问题描述**：客户端 ID 或密钥错误

**错误信息**：
```json
{
  "error": "invalid_client",
  "error_description": "The OAuth client was not found."
}
```

**解决方案**：
1. 检查配置文件中的 `client-id` 和 `client-secret`
2. 登录 Google Cloud Console，确认凭据是否正确
3. 确保复制时没有多余的空格或换行

### 6.3 access_denied

**问题描述**：用户拒绝授权

**错误信息**：
```
error=access_denied
```

**原因**：用户在授权页面点击了"拒绝"或"取消"按钮

**解决方案**：
- 这是正常的用户行为
- 应用应该友好地提示用户需要授权才能登录

### 6.4 未验证的应用警告

**问题描述**：用户看到"此应用未经 Google 验证"警告

**原因**：应用处于测试状态，未完成 Google 验证

**解决方案**：
1. **开发测试阶段**：
   - 添加测试用户到 OAuth 同意屏幕
   - 测试用户不会看到警告
   
2. **生产环境**：
   - 提交应用进行 Google 验证
   - 验证过程可能需要几周时间
   - 需要提供隐私政策和服务条款

3. **临时方案**：
   - 用户可以点击"高级" → "转至应用（不安全）"继续授权
   - 但这会影响用户体验

## 7. 应用验证

### 7.1 何时需要验证

如果您的应用满足以下条件之一，需要进行 Google 验证：

- 请求敏感或受限范围
- 用户数超过 100 个
- 需要移除"未验证"警告

### 7.2 验证流程

1. 在 OAuth 同意屏幕页面，点击"发布应用"
2. 点击"准备验证"
3. 填写验证表单：
   - 应用说明
   - 隐私政策链接
   - 服务条款链接
   - 应用演示视频
4. 提交审核
5. 等待 Google 审核（通常需要 3-5 个工作日）

### 7.3 验证要求

- 有效的隐私政策页面
- 有效的服务条款页面
- 应用功能演示视频
- 清晰的应用说明

## 8. 高级配置

### 8.1 获取用户详细信息

使用 Google People API 获取更多用户信息：

```
GET https://people.googleapis.com/v1/people/me?personFields=names,emailAddresses,photos
Authorization: Bearer {access_token}
```

### 8.2 刷新 Access Token

Access Token 有效期为 1 小时，可以使用 Refresh Token 刷新：

```
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

client_id={client_id}&
client_secret={client_secret}&
refresh_token={refresh_token}&
grant_type=refresh_token
```

## 9. 最佳实践

### 9.1 安全建议

1. **保护客户端密钥**
   - 不要将密钥提交到代码仓库
   - 使用环境变量管理
   - 定期轮换密钥

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS
   - Google 强烈建议使用 HTTPS

3. **最小权限原则**
   - 只请求必要的 scope
   - 避免请求敏感权限

### 9.2 用户体验优化

1. **清晰的授权说明**
   - 说明为什么需要 Google 登录
   - 说明需要获取哪些信息

2. **处理多账号**
   - Google 用户可能有多个账号
   - 提供账号选择功能

3. **离线访问**
   - 如需离线访问，添加 `access_type=offline` 参数
   - 可以获取 Refresh Token

## 10. 配额和限制

### 10.1 API 配额

- 默认配额：10,000 请求/天
- 可以申请提高配额
- 建议实现请求缓存

### 10.2 频率限制

- 每个用户每秒最多 10 个请求
- 建议实现请求队列

## 11. 参考资料

- [Google OAuth 2.0 文档](https://developers.google.com/identity/protocols/oauth2)
- [Google Cloud Console](https://console.cloud.google.com/)
- [Google People API](https://developers.google.com/people)
- [JustAuth 文档](https://justauth.wiki)

## 12. 更新日志

- 2026-03-09：初始版本
- 文档版本：v1.0.0

## 13. 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

1. 查看 [Google 开发者文档](https://developers.google.com)
2. 在 [Stack Overflow](https://stackoverflow.com/questions/tagged/google-oauth) 提问
3. 联系 Google Cloud Support

---

**提示**：本文档基于 Google OAuth 2.0 编写，如有变更请以官方文档为准。
