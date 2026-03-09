# 钉钉登录申请指南

## 1. 概述

钉钉是阿里巴巴集团打造的企业级智能移动办公平台。通过钉钉OAuth2登录，用户可以使用钉钉账号快速登录您的应用。

**官方网站**：https://www.dingtalk.com  
**开放平台**：https://open.dingtalk.com

## 2. 前置条件

- [x] 钉钉账号
- [x] 企业钉钉账号（企业应用）
- [x] 应用图标（114x114像素）
- [ ] 企业认证（企业应用推荐）

## 3. 申请流程

### 3.1 注册开发者账号

1. 访问 [钉钉开放平台](https://open.dingtalk.com)
2. 使用钉钉账号扫码登录
3. 完成开发者认证

### 3.2 创建应用

1. 进入开发者后台
2. 选择应用类型：企业内部应用/第三方企业应用/个人应用
3. 点击"创建应用"

### 3.3 填写应用信息

| 字段 | 说明 | 必填 |
|------|------|------|
| 应用名称 | 显示给用户的应用名称 | ✓ |
| 应用图标 | 114x114像素 | ✓ |
| 应用描述 | 应用的简要描述 | ✓ |

### 3.4 配置权限

1. 在应用详情页面，点击"权限管理"
2. 添加所需权限：获取用户基本信息

### 3.5 配置回调地址

1. 在"登录与分享"设置中
2. 配置回调域名：`example.com`
3. 完整回调地址：`http://example.com/oauth2/callback/dingtalk`

### 3.6 获取凭证

创建完成后，您将获得：
- **AppKey**：应用的唯一标识
- **AppSecret**：应用的密钥

## 4. 配置示例

```yaml
plugin:
  oauth:
    third-party:
      login:
        dingtalk:
          enabled: true
          client-id: "dingoa1234567890abcdef"
          client-secret: "abcdefghijklmnopqrstuvwxyz123456"
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/dingtalk
          scope: openid
```

## 5. 测试

访问授权接口：
```
GET http://localhost:19180/oauth2/authorize/dingtalk
```

## 6. 常见问题

### 6.1 invalid_client

**原因**：AppKey或AppSecret错误

**解决方案**：
- 检查配置文件
- 确认应用已发布

### 6.2 redirect_uri_mismatch

**原因**：回调地址不匹配

**解决方案**：
- 检查配置的回调域名
- 确保回调地址使用已配置的域名

## 7. 最佳实践

1. **安全建议**
   - 保护AppSecret
   - 使用HTTPS
   - 权限最小化

2. **用户体验**
   - 提供清晰的扫码指引
   - 支持移动端自动跳转
   - 友好的错误提示

## 8. 参考资料

- [钉钉开放平台文档](https://open.dingtalk.com/document/)
- [扫码登录](https://open.dingtalk.com/document/orgapp/tutorial-obtaining-user-personal-information)
- [JustAuth文档](https://justauth.wiki)

---

**提示**：本文档基于钉钉开放平台OAuth2.0编写，如有变更请以官方文档为准。
