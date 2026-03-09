# Microsoft 登录申请指南

## 1. 概述

Microsoft是全球领先的科技公司。通过Microsoft OAuth2登录，用户可以使用Microsoft账号快速登录您的应用。

**官方网站**：https://www.microsoft.com  
**开发者中心**：https://portal.azure.com

## 2. 前置条件

- [x] Microsoft账号
- [x] Azure订阅（免费订阅即可）
- [x] 应用名称

## 3. 申请流程

### 3.1 注册Azure账号

1. 访问 [Azure Portal](https://portal.azure.com)
2. 使用Microsoft账号登录
3. 创建免费订阅（如需要）

### 3.2 注册应用

1. 在Azure Portal中，搜索"Azure Active Directory"
2. 点击"应用注册" → "新注册"

### 3.3 填写应用信息

| 字段 | 说明 | 必填 |
|------|------|------|
| 名称 | 显示给用户的应用名称 | ✓ |
| 支持的账户类型 | 选择账户类型 | ✓ |
| 重定向URI | OAuth2回调地址 | ✓ |

**支持的账户类型**（推荐选择"任何组织目录中的账户和个人Microsoft账户"）

### 3.4 获取凭证

1. 复制**应用程序(客户端)ID**
2. 点击"证书和密码" → "新客户端密码"
3. 立即复制**客户端密码值**（只显示一次）

### 3.5 配置API权限

默认已添加"User.Read"权限，可根据需要添加其他权限。

## 4. 配置示例

```yaml
plugin:
  oauth:
    third-party:
      login:
        microsoft:
          enabled: true
          client-id: "12345678-1234-1234-1234-123456789012"
          client-secret: "abcdefghijklmnopqrstuvwxyz~123456"
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/microsoft
          scope: openid,profile,email
```

## 5. 测试

访问授权接口：
```
GET http://localhost:19180/oauth2/authorize/microsoft
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**原因**：重定向URI不匹配

**解决方案**：
- 检查配置文件中的redirect-uri
- 确保与Azure Portal配置一致

### 6.2 invalid_client

**原因**：客户端ID或密码错误

**解决方案**：
- 检查配置文件
- 如果密码过期，需要重新生成

### 6.3 consent_required

**原因**：需要管理员同意

**解决方案**：
- 个人账户：用户首次登录时会看到同意页面
- 组织账户：可能需要管理员预先同意

## 7. 最佳实践

1. **安全建议**
   - 保护客户端密码
   - 使用HTTPS
   - 最小权限原则

2. **用户体验**
   - 清晰的授权说明
   - 支持多种账户类型
   - 友好的错误提示

## 8. 参考资料

- [Microsoft身份平台文档](https://docs.microsoft.com/zh-cn/azure/active-directory/develop/)
- [Microsoft Graph API](https://docs.microsoft.com/zh-cn/graph/)
- [Azure Portal](https://portal.azure.com)
- [JustAuth文档](https://justauth.wiki)

---

**提示**：本文档基于Microsoft身份平台OAuth2.0编写，如有变更请以官方文档为准。
