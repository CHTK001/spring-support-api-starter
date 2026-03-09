# QQ互联 OAuth2 登录申请指南

## 1. 概述

QQ互联是腾讯公司推出的开放平台，允许第三方网站和应用接入QQ账号登录体系。通过QQ互联OAuth2登录，用户可以使用QQ账号快速登录您的应用。

**官方网站**：https://connect.qq.com  
**开发者中心**：https://connect.qq.com/manage.html

## 2. 前置条件

- [x] QQ账号
- [x] 网站备案信息（ICP备案号）
- [x] 网站域名
- [x] 应用图标（200x200像素）
- [ ] 企业营业执照（企业应用需要）

## 3. 申请流程

### 3.1 注册开发者账号

1. 访问 [QQ互联官网](https://connect.qq.com)
2. 点击右上角"登录"，使用QQ账号登录
3. 首次登录需要完成开发者认证：
   - 个人开发者：填写个人信息
   - 企业开发者：上传营业执照

### 3.2 创建应用

1. 登录后进入 [管理中心](https://connect.qq.com/manage.html)
2. 点击"创建应用"
3. 选择应用类型：
   - **网站应用**：适用于Web网站
   - **移动应用**：适用于iOS/Android应用
   - **桌面应用**：适用于PC客户端

### 3.3 填写应用信息（以网站应用为例）

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 应用名称 | 显示给用户的应用名称 | 我的应用 | ✓ |
| 应用简介 | 应用的简要描述 | 这是一个测试应用 | ✓ |
| 应用图标 | 应用图标（200x200） | - | ✓ |
| 网站地址 | 应用的主页地址 | https://example.com | ✓ |
| 网站备案号 | ICP备案号 | 京ICP备12345678号 | ✓ |
| 回调地址 | OAuth2回调地址 | http://example.com/oauth2/callback/qq | ✓ |

**重要提示**：
- 回调地址必须与配置文件中的 `redirect-uri` 完全一致
- 回调地址格式：`http://your-domain/oauth2/callback/qq`
- 本地测试需要配置：`http://127.0.0.1:19180/oauth2/callback/qq`
- QQ互联要求回调地址必须是已备案的域名（生产环境）

### 3.4 提交审核

1. 填写完成后点击"提交审核"
2. 审核时间：通常1-3个工作日
3. 审核通过后，您将获得：
   - **APP ID**：应用的唯一标识
   - **APP Key**：应用的密钥

⚠️ **重要**：请妥善保管APP Key，不要泄露给他人或提交到代码仓库。

### 3.5 配置权限

QQ互联OAuth2支持以下权限范围（Scope）：

| Scope | 说明 | 默认 |
|-------|------|------|
| get_user_info | 获取用户基本信息 | ✓ |
| get_vip_info | 获取QQ会员信息 | ✗ |
| get_vip_rich_info | 获取QQ会员详细信息 | ✗ |
| list_album | 获取用户相册列表 | ✗ |
| upload_pic | 上传图片到相册 | ✗ |
| add_share | 发表说说 | ✗ |

**推荐配置**：对于登录功能，只需要 `get_user_info` 权限即可。

## 4. 配置示例

### 4.1 application.yml 配置

```yaml
plugin:
  oauth:
    third-party:
      login:
        qq:
          enabled: true
          client-id: "101234567"
          client-secret: "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
          redirect-uri: http://127.0.0.1:19180/oauth2/callback/qq
          scope: get_user_info
```

### 4.2 配置说明

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用QQ登录 | ✓ |
| client-id | QQ互联的APP ID | ✓ |
| client-secret | QQ互联的APP Key | ✓ |
| redirect-uri | 授权回调地址 | ✓ |
| scope | 权限范围，默认 get_user_info | ✗ |

## 5. 测试

### 5.1 本地测试

1. 启动您的应用
2. 访问授权接口：
   ```
   GET http://localhost:19180/oauth2/authorize/qq
   ```
3. 浏览器会跳转到QQ登录授权页面
4. 使用QQ账号登录并授权
5. 授权成功后会自动跳转回您的应用

### 5.2 验证登录

检查应用日志，确认登录成功：

```
INFO  - OAuth2 登录成功: platform=qq, userId=A1B2C3D4E5F6G7H8
INFO  - 用户信息: nickname=张三, figureurl=http://qzapp.qlogo.cn/...
```

## 6. 常见问题

### 6.1 redirect_uri_mismatch

**问题描述**：回调地址不匹配错误

**错误信息**：
```json
{
  "error": 100010,
  "error_description": "redirect uri is illegal"
}
```

**解决方案**：
1. 检查配置文件中的 `redirect-uri` 参数
2. 登录QQ互联管理中心，检查应用的回调地址配置
3. 确保两者完全一致（包括协议、域名、端口、路径）
4. 注意：生产环境必须使用已备案的域名

### 6.2 invalid_client

**问题描述**：APP ID或APP Key错误

**错误信息**：
```json
{
  "error": 100001,
  "error_description": "client_id is invalid"
}
```

**解决方案**：
1. 检查配置文件中的 `client-id` 和 `client-secret`
2. 登录QQ互联管理中心，确认APP ID和APP Key
3. 确保复制时没有多余的空格或引号

### 6.3 access_denied

**问题描述**：用户拒绝授权

**错误信息**：
```
error=access_denied
```

**原因**：用户在授权页面点击了"取消"按钮

**解决方案**：
- 这是正常的用户行为，应用应该友好地提示用户需要授权才能登录
- 可以提供重新授权的链接

### 6.4 本地测试域名问题

**问题描述**：本地测试时无法使用localhost

**解决方案**：
1. QQ互联不支持localhost作为回调域名
2. 本地测试时使用 `http://127.0.0.1` 代替
3. 或者配置本地hosts文件，将域名指向127.0.0.1
4. 生产环境必须使用已备案的域名

### 6.5 网站备案问题

**问题描述**：没有网站备案无法创建应用

**解决方案**：
1. **开发测试阶段**：
   - 可以使用测试域名（需要在QQ互联申请测试资格）
   - 或者使用已备案的其他域名进行测试

2. **生产环境**：
   - 必须完成网站ICP备案
   - 备案流程请咨询您的服务器提供商

## 7. 高级配置

### 7.1 获取用户详细信息

调用QQ互联API获取用户信息：

```
GET https://graph.qq.com/user/get_user_info
?access_token={access_token}
&oauth_consumer_key={app_id}
&openid={openid}
```

返回数据示例：
```json
{
  "ret": 0,
  "msg": "",
  "nickname": "张三",
  "figureurl": "http://qzapp.qlogo.cn/...",
  "figureurl_1": "http://qzapp.qlogo.cn/...",
  "figureurl_2": "http://qzapp.qlogo.cn/...",
  "figureurl_qq_1": "http://qzapp.qlogo.cn/...",
  "figureurl_qq_2": "http://qzapp.qlogo.cn/...",
  "gender": "男",
  "is_yellow_vip": "0",
  "vip": "0",
  "yellow_vip_level": "0",
  "level": "0",
  "is_yellow_year_vip": "0"
}
```

### 7.2 获取OpenID

在获取Access Token后，需要先获取OpenID：

```
GET https://graph.qq.com/oauth2.0/me?access_token={access_token}
```

返回格式：
```
callback( {"client_id":"YOUR_APPID","openid":"YOUR_OPENID"} );
```

## 8. 最佳实践

### 8.1 安全建议

1. **保护APP Key**
   - 不要将APP Key提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息
   - 定期更换APP Key

2. **使用HTTPS**
   - 生产环境必须使用HTTPS协议
   - 避免在不安全的网络环境下传输敏感信息

3. **验证回调来源**
   - 验证state参数，防止CSRF攻击
   - 验证回调请求的来源

### 8.2 用户体验优化

1. **清晰的授权说明**
   - 在跳转到QQ授权页面前，告知用户将要进行的操作
   - 说明需要获取哪些权限以及用途

2. **错误处理**
   - 提供友好的错误提示
   - 对于常见错误，提供解决建议

3. **头像处理**
   - QQ提供多种尺寸的头像URL
   - 根据需要选择合适的尺寸
   - 建议缓存头像以提高性能

### 8.3 性能优化

1. **缓存用户信息**
   - 合理缓存用户基本信息
   - 减少API调用次数

2. **异步处理**
   - 用户信息获取可以异步处理
   - 避免阻塞主流程

## 9. API限制

### 9.1 调用频率限制

- 单个用户每小时最多调用5000次
- 单个应用每天最多调用100万次
- 超过限制会返回错误码：1002

### 9.2 并发限制

- 建议控制并发请求数
- 实现请求队列和重试机制

## 10. 参考资料

- [QQ互联官方文档](https://wiki.connect.qq.com/)
- [QQ互联管理中心](https://connect.qq.com/manage.html)
- [QQ互联API文档](https://wiki.connect.qq.com/api%e5%88%97%e8%a1%a8)
- [JustAuth文档](https://justauth.wiki)

## 11. 更新日志

- 2026-03-09：初始版本
- 文档版本：v1.0.0

## 12. 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

1. 查看 [QQ互联帮助中心](https://wiki.connect.qq.com/)
2. 在 [QQ互联论坛](https://support.qq.com/products/36940) 提问
3. 联系QQ互联技术支持

---

**提示**：本文档基于QQ互联OAuth2.0编写，如有变更请以官方文档为准。
