# 百度登录申请指南

## 1. 概述

百度是中国最大的搜索引擎和AI技术公司。通过百度OAuth2登录，用户可以使用百度账号快速登录您的应用。

**官方网站**：https://www.baidu.com  
**开发者中心**：https://developer.baidu.com

## 2. 前置条件

- [x] 百度账号
- [x] 实名认证
- [x] 应用图标（200x200像素）
- [x] 应用网站地址

## 3. 申请流程

### 3.1 注册开发者账号

1. 访问 [百度开发者中心](https://developer.baidu.com)
2. 使用百度账号登录
3. 完成开发者认证

### 3.2 创建应用

1. 进入控制台，选择"创建应用"
2. 选择应用类型：网站应用/移动应用

### 3.3 填写应用信息

| 字段 | 说明 | 必填 |
|------|------|------|
| 应用名称 | 显示给用户的应用名称 | ✓ |
| 应用图标 | 200x200像素 | ✓ |
| 应用首页 | 应用的主页地址 | ✓ |
| 授权回调页 | OAuth2回调地址 | ✓ |

### 3.4 获取凭证

创建完成后，您将获得：
- **API Key**：应用的唯一标识
- **Secret Key**：应用的密钥

## 4. 配置示例

```yaml
plugin:
  oauth:
    third-party:
      login:
        baidu:
          enabled: true
          client-id: "abcdefghijklmnopqrstuvwxyz"
          client-secret: "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456"
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/baidu
          scope: basic
```

## 5. 测试

访问授权接口：
```
GET http://localhost:19180/oauth2/authorize/baidu
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**原因**：回调地址不匹配

**解决方案**：
- 检查配置文件中的redirect-uri
- 确保与开发者中心配置一致

### 6.2 invalid_client

**原因**：API Key或Secret Key错误

**解决方案**：
- 检查配置文件
- 确认凭证正确

## 7. 最佳实践

1. **安全建议**
   - 保护Secret Key
   - 使用HTTPS
   - 验证回调来源

2. **用户体验**
   - 清晰的授权说明
   - 友好的错误提示
   - 头像缓存优化

## 8. 参考资料

- [百度开发者中心](https://developer.baidu.com)
- [OAuth2.0文档](https://developer.baidu.com/wiki/index.php?title=docs/oauth)
- [JustAuth文档](https://justauth.wiki)

---

**提示**：本文档基于百度OAuth2.0编写，如有变更请以官方文档为准。
