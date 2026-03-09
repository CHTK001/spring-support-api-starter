# 微信开放平台 OAuth2 登录申请指南

## 1. 概述

微信开放平台是腾讯为第三方网站应用提供的微信登录服务平台。通过微信开放平台 OAuth2 登录，用户可以使用微信扫码快速登录您的网站应用，无需注册新账号。

**官方网站**：https://open.weixin.qq.com  
**管理中心**：https://open.weixin.qq.com/cgi-bin/index

**适用场景**：
- PC 网站应用扫码登录
- 移动网站应用授权登录
- 需要获取微信用户信息的 Web 应用

## 2. 前置条件

- [x] 企业营业执照或个体工商户营业执照
- [x] 对公账户信息（用于开发者资质认证）
- [x] 已备案的域名
- [x] 应用图标（108x108 像素）
- [x] 应用官网地址
- [ ] 认证费用：300元/年（可选，但推荐认证）

⚠️ **重要提示**：
- 微信开放平台需要企业资质，个人开发者无法申请
- 回调域名必须已完成 ICP 备案
- 未认证的应用有功能限制，建议完成开发者资质认证

## 3. 申请流程

### 3.1 注册开发者账号

1. 访问 [微信开放平台](https://open.weixin.qq.com)
2. 点击右上角"注册"按钮
3. 使用微信扫码注册
4. 填写账号信息：
   - 账号类型：选择"企业"或"个体工商户"
   - 邮箱地址：用于接收通知
   - 密码：设置登录密码

### 3.2 开发者资质认证（推荐）

1. 登录微信开放平台
2. 进入"账号中心" → "开发者资质认证"
3. 填写认证信息：

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 企业名称 | 营业执照上的企业名称 | XX科技有限公司 | ✓ |
| 营业执照注册号 | 统一社会信用代码 | 91110000XXXXXXXXXX | ✓ |
| 企业开户名称 | 对公账户开户名 | XX科技有限公司 | ✓ |
| 对公账户 | 企业对公银行账号 | 1234567890 | ✓ |
| 开户银行 | 开户银行名称 | 中国工商银行XX支行 | ✓ |

4. 上传营业执照扫描件
5. 支付认证费用：300元/年
6. 等待审核（通常 1-3 个工作日）

### 3.3 创建网站应用

1. 登录微信开放平台
2. 进入"管理中心" → "网站应用"
3. 点击"创建网站应用"按钮
4. 填写应用信息：

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 应用名称 | 显示给用户的应用名称 | My Application | ✓ |
| 应用简介 | 应用的简要描述（20-200字） | 这是一个提供XX服务的应用 | ✓ |
| 应用官网 | 应用的官方网站地址 | https://example.com | ✓ |
| 应用图标 | 应用图标（108x108像素） | - | ✓ |
| 授权回调域 | OAuth2 回调域名（不含协议和路径） | example.com | ✓ |

**重要提示**：
- 授权回调域只填写域名，不包含 `http://`、`https://` 和路径
- 例如：填写 `example.com`，不要填写 `http://example.com/oauth2/callback`
- 回调域名必须已完成 ICP 备案
- 支持填写多个回调域，用分号分隔

5. 提交审核
6. 审核通过后，在应用详情页面获取：
   - **AppID**（应用唯一标识）
   - **AppSecret**（应用密钥）

⚠️ **重要**：请妥善保管 AppSecret，不要泄露给他人或提交到代码仓库。

### 3.4 配置权限

微信开放平台网站应用默认权限：

| Scope | 说明 | 默认 |
|-------|------|------|
| snsapi_login | 网站应用微信登录 | ✓ |

**注意**：网站应用只支持 `snsapi_login` 权限，可以获取用户的基本信息（昵称、头像、性别、地区等）。

## 4. 配置说明

### 4.1 配置参数

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用微信开放平台登录 | ✓ |
| client-id | 微信开放平台应用的 AppID | ✓ |
| client-secret | 微信开放平台应用的 AppSecret | ✓ |
| redirect-uri | 授权回调地址（完整URL） | ✓ |

**回调地址说明**：
- 格式：`http://your-domain/oauth2/callback/wechat-open`
- 域名部分必须与微信开放平台配置的"授权回调域"一致
- 支持 http 和 https 协议（生产环境建议使用 https）
- 本地测试需要配置 hosts 文件映射域名到 127.0.0.1

## 5. 配置示例

### 5.1 application.yml 配置

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-open:
          enabled: true
          client-id: wx1234567890abcdef
          client-secret: 1234567890abcdef1234567890abcdef
          redirect-uri: http://example.com/oauth2/callback/wechat-open
```

### 5.2 本地测试配置

由于微信开放平台要求回调域名已备案，本地测试需要特殊配置：

**方法一：配置 hosts 文件**

1. 在微信开放平台配置授权回调域：`test.example.com`
2. 修改本地 hosts 文件（Windows: `C:\Windows\System32\drivers\etc\hosts`，Mac/Linux: `/etc/hosts`）：
   ```
   127.0.0.1 test.example.com
   ```
3. 配置文件使用：
   ```yaml
   redirect-uri: http://test.example.com:19180/oauth2/callback/wechat-open
   ```

**方法二：使用内网穿透工具**

1. 使用 ngrok、frp 等工具将本地服务映射到公网域名
2. 在微信开放平台配置该公网域名
3. 配置文件使用公网域名

## 6. 测试指南

### 6.1 本地测试

1. 启动您的应用
2. 访问授权接口：
   ```
   GET http://test.example.com:19180/oauth2/authorize/wechat-open
   ```
3. 浏览器会跳转到微信开放平台授权页面
4. 使用微信扫描二维码
5. 在手机上确认授权
6. 授权成功后会自动跳转回您的应用

### 6.2 生产环境测试

1. 确保应用已部署到生产服务器
2. 确保域名已备案且可以正常访问
3. 访问授权接口：
   ```
   GET https://your-domain.com/oauth2/authorize/wechat-open
   ```
4. 扫码授权测试

### 6.3 验证登录

检查应用日志，确认登录成功：

```
INFO  - OAuth2 登录成功: platform=wechat-open, userId=oxxxxxxxxxxxxxxxxxxxxxx
INFO  - 用户信息: nickname=张三, sex=1, province=广东, city=深圳
```

## 7. 常见问题

### 7.1 redirect_uri 参数错误

**问题描述**：回调地址域名不匹配

**错误信息**：
```json
{
  "errcode": 10003,
  "errmsg": "redirect_uri域名与后台配置不一致"
}
```

**原因**：配置的回调地址域名与微信开放平台配置的授权回调域不一致

**解决方案**：
1. 检查配置文件中的 `redirect-uri` 参数
2. 登录微信开放平台，检查应用的"授权回调域"配置
3. 确保回调地址的域名部分与授权回调域一致
4. 注意：授权回调域不包含协议和路径，只填写域名

**示例**：
- 授权回调域配置：`example.com`
- 正确的回调地址：`http://example.com/oauth2/callback/wechat-open`
- 错误的回调地址：`http://www.example.com/oauth2/callback/wechat-open`（子域名不匹配）

### 7.2 invalid_appid

**问题描述**：AppID 参数错误

**错误信息**：
```json
{
  "errcode": 40013,
  "errmsg": "invalid appid"
}
```

**原因**：AppID 配置错误或应用已被删除

**解决方案**：
1. 检查配置文件中的 `client-id` 是否正确
2. 登录微信开放平台，确认应用状态是否正常
3. 确保复制 AppID 时没有多余的空格或换行
4. 注意区分微信开放平台、微信公众平台、微信小程序的 AppID

### 7.3 invalid_secret

**问题描述**：AppSecret 参数错误

**错误信息**：
```json
{
  "errcode": 40001,
  "errmsg": "invalid credential, access_token is invalid or not latest"
}
```

**原因**：AppSecret 配置错误或已重置

**解决方案**：
1. 检查配置文件中的 `client-secret` 是否正确
2. 登录微信开放平台，确认 AppSecret 是否已重置
3. 如果 AppSecret 已重置，需要使用新的 AppSecret
4. 确保复制时没有多余的空格或换行

### 7.4 用户取消授权

**问题描述**：用户在扫码后取消授权

**错误信息**：
```
error=access_denied&error_description=user%20denied
```

**原因**：用户在手机上点击了"取消"按钮

**解决方案**：
- 这是正常的用户行为
- 应用应该友好地提示用户需要授权才能登录
- 可以提供重新授权的链接

### 7.5 未备案域名

**问题描述**：使用未备案域名无法配置

**错误信息**：
```
授权回调域未备案，请先完成域名备案
```

**原因**：微信开放平台要求授权回调域必须已完成 ICP 备案

**解决方案**：
1. 完成域名的 ICP 备案（通过云服务商申请）
2. 备案通过后，在微信开放平台配置该域名
3. 本地测试可以使用 hosts 文件映射已备案的域名

### 7.6 二维码过期

**问题描述**：扫码时提示二维码已过期

**原因**：微信登录二维码有效期为 5 分钟

**解决方案**：
- 刷新页面重新获取二维码
- 建议在前端实现二维码自动刷新功能

## 8. 最佳实践

### 8.1 安全建议

1. **保护 AppSecret**
   - 不要将 AppSecret 提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息
   - 定期更换 AppSecret（在微信开放平台重置）

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS 协议
   - 避免在不安全的网络环境下传输用户信息

3. **验证回调来源**
   - 验证 state 参数，防止 CSRF 攻击
   - 验证回调请求的来源域名

4. **Access Token 管理**
   - Access Token 有效期为 2 小时
   - Refresh Token 有效期为 30 天
   - 建议实现 Token 刷新机制

### 8.2 用户体验优化

1. **清晰的授权说明**
   - 在跳转到微信授权页面前，告知用户将要进行的操作
   - 说明需要获取哪些信息以及用途

2. **二维码展示优化**
   - 提供清晰的扫码提示
   - 实现二维码自动刷新功能
   - 提供"刷新二维码"按钮

3. **移动端适配**
   - 在移动端浏览器中，自动跳转到微信授权页面
   - 避免在移动端显示二维码

4. **错误处理**
   - 提供友好的错误提示
   - 对于常见错误，提供解决建议
   - 提供客服联系方式

### 8.3 性能优化

1. **缓存用户信息**
   - 合理缓存用户基本信息
   - 减少对微信 API 的调用频率

2. **异步处理**
   - 用户信息获取采用异步方式
   - 避免阻塞主流程

3. **监控告警**
   - 监控授权成功率
   - 监控 API 调用失败率
   - 设置异常告警

## 9. 参考资料

- [微信开放平台网站应用开发文档](https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html)
- [微信开放平台](https://open.weixin.qq.com)
- [微信开放平台接口调用频率限制](https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/2.0/api/Before_Develop/interface_frequency.html)
- [JustAuth 文档](https://justauth.wiki)

## 10. 更新日志

- 2026-03-09：初始版本
- 文档版本：v1.0.0

## 11. 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

1. 查看 [微信开放平台开发文档](https://developers.weixin.qq.com/doc/oplatform/)
2. 访问 [微信开放社区](https://developers.weixin.qq.com/community/develop)
3. 联系微信开放平台客服

---

**提示**：本文档基于微信开放平台网站应用开发指南编写，如有变更请以官方文档为准。
