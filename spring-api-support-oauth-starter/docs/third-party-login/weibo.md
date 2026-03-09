# 新浪微博登录申请指南

## 1. 概述

新浪微博是中国最大的社交媒体平台之一。通过微博OAuth2登录，用户可以使用微博账号快速登录您的应用。

**官方网站**：https://weibo.com  
**开放平台**：https://open.weibo.com

## 2. 前置条件

- [x] 新浪微博账号
- [x] 实名认证
- [x] 应用图标（120x120像素）
- [x] 应用网站地址

## 3. 申请流程

### 3.1 注册开发者账号

1. 访问 [微博开放平台](https://open.weibo.com)
2. 使用微博账号登录
3. 完成开发者认证（个人/企业）

### 3.2 创建应用

1. 进入 [我的应用](https://open.weibo.com/apps)
2. 点击"创建应用"
3. 选择应用类型：网站应用/移动应用

### 3.3 填写应用信息

| 字段 | 说明 | 必填 |
|------|------|------|
| 应用名称 | 显示给用户的应用名称 | ✓ |
| 应用图标 | 120x120像素 | ✓ |
| 应用介绍 | 应用的简要描述 | ✓ |
| 应用网站 | 应用的主页地址 | ✓ |
| 授权回调页 | OAuth2回调地址 | ✓ |

### 3.4 提交审核

审核通过后，您将获得：
- **App Key**：应用的唯一标识
- **App Secret**：应用的密钥

## 4. 配置示例

```yaml
plugin:
  oauth:
    third-party:
      login:
        weibo:
          enabled: true
          client-id: "1234567890"
          client-secret: "abcdef1234567890abcdef1234567890"
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/weibo
```

## 5. 测试

访问授权接口：
```
GET http://localhost:19180/oauth2/authorize/weibo
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**原因**：回调地址不匹配

**解决方案**：
- 检查配置文件中的redirect-uri
- 确保与开放平台配置一致

### 6.2 invalid_client

**原因**：App Key或App Secret错误

**解决方案**：
- 检查配置文件
- 确认凭证正确

### 6.3 测试账号限制

**问题**：未审核应用只能15个测试账号使用

**解决方案**：
- 开发阶段：添加测试账号
- 生产环境：通过应用审核

## 7. 最佳实践

1. **安全建议**
   - 保护App Secret
   - 使用HTTPS
   - 验证回调来源

2. **用户体验**
   - 清晰的授权说明
   - 友好的错误提示
   - 头像缓存优化

## 8. 参考资料

- [微博开放平台文档](https://open.weibo.com/wiki/首页)
- [OAuth2.0授权](https://open.weibo.com/wiki/Oauth2)
- [JustAuth文档](https://justauth.wiki)

---

**提示**：本文档基于新浪微博OAuth2.0编写，如有变更请以官方文档为准。
