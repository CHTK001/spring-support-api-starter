# 支付宝登录申请指南

## 1. 概述

支付宝是中国领先的第三方支付平台。通过支付宝OAuth2登录，用户可以使用支付宝账号快速登录您的应用。

**官方网站**：https://www.alipay.com  
**开放平台**：https://open.alipay.com

## 2. 前置条件

- [x] 支付宝账号
- [x] 企业支付宝账号（企业应用）
- [x] 营业执照（企业应用）
- [x] 应用图标（120x120像素）

## 3. 申请流程

### 3.1 注册开发者账号

1. 访问 [支付宝开放平台](https://open.alipay.com)
2. 使用支付宝账号登录
3. 完成开发者认证（个人/企业）

### 3.2 创建应用

1. 进入控制台，点击"创建应用"
2. 选择应用类型：网页/移动应用
3. 填写应用信息

| 字段 | 说明 | 必填 |
|------|------|------|
| 应用名称 | 显示给用户的应用名称 | ✓ |
| 应用图标 | 120x120像素 | ✓ |
| 授权回调地址 | OAuth2回调地址 | ✓ |

### 3.3 配置密钥

1. 生成RSA密钥对
2. 上传应用公钥
3. 获取支付宝公钥

### 3.4 添加功能

1. 添加"获取会员信息"功能
2. 提交审核（1-3个工作日）

## 4. 配置示例

```yaml
plugin:
  oauth:
    third-party:
      login:
        alipay:
          enabled: true
          client-id: "2021001234567890"
          client-secret: "MIIEvQIBADANBgkqhkiG9w0BAQEFAASC..."
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/alipay
          alipay-public-key: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
```

## 5. 测试

访问授权接口：
```
GET http://localhost:19180/oauth2/authorize/alipay
```

## 6. 常见问题

### 6.1 invalid_signature

**原因**：签名验证失败

**解决方案**：
- 检查应用私钥和支付宝公钥
- 确认签名方式为RSA2
- 检查私钥格式（PKCS8）

### 6.2 invalid_app_id

**原因**：APPID错误

**解决方案**：
- 检查client-id配置
- 确保应用已通过审核

## 7. 最佳实践

1. **安全建议**
   - 保护应用私钥
   - 使用HTTPS
   - 验证返回数据签名

2. **用户体验**
   - 清晰的授权说明
   - 友好的错误提示
   - 支持移动端优化

## 8. 参考资料

- [支付宝开放平台文档](https://opendocs.alipay.com/)
- [用户信息授权](https://opendocs.alipay.com/open/284/web)
- [JustAuth文档](https://justauth.wiki)

---

**提示**：本文档基于支付宝开放平台OAuth2.0编写，如有变更请以官方文档为准。
