# 微信公众号 OAuth2 登录申请指南

## 1. 概述

微信公众号网页授权是腾讯为公众号提供的用户身份验证服务。通过微信公众号网页授权,用户可以在微信内置浏览器中快速登录您的网站应用,无需注册新账号。

**官方网站**:https://mp.weixin.qq.com  
**管理中心**:https://mp.weixin.qq.com

**适用场景**:
- 微信内置浏览器中的网页应用
- 公众号菜单跳转的 H5 页面
- 需要获取微信用户信息的移动网页
- 公众号内的用户身份验证

⚠️ **重要限制**:
- 只能在微信内置浏览器中使用
- 需要服务号(订阅号不支持网页授权)
- 必须完成微信认证(300元/年)

## 2. 前置条件

- [x] 微信服务号(订阅号不支持网页授权)
- [x] 完成微信认证(需要营业执照)
- [x] 已备案的域名
- [x] 认证费用:300元/年

⚠️ **重要提示**:
- 订阅号不支持网页授权功能,必须使用服务号
- 服务号必须完成微信认证才能使用网页授权
- 网页授权域名必须已完成 ICP 备案
- 认证需要企业营业执照或个体工商户营业执照

## 3. 申请流程

### 3.1 注册公众号

1. 访问 [微信公众平台](https://mp.weixin.qq.com)
2. 点击右上角"立即注册"按钮
3. 选择账号类型:
   - **服务号**:适合企业和组织,支持网页授权(推荐)
   - **订阅号**:不支持网页授权,不适用于本场景
4. 填写账号信息:
   - 邮箱地址:用于接收通知
   - 密码:设置登录密码
5. 选择主体类型:
   - 企业
   - 个体工商户
   - 政府
   - 其他组织

### 3.2 完成微信认证(必须)

1. 登录微信公众平台
2. 进入"设置与开发" → "微信认证"
3. 填写认证信息:

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 企业名称 | 营业执照上的企业名称 | XX科技有限公司 | ✓ |
| 营业执照注册号 | 统一社会信用代码 | 91110000XXXXXXXXXX | ✓ |
| 企业开户名称 | 对公账户开户名 | XX科技有限公司 | ✓ |
| 对公账户 | 企业对公银行账号 | 1234567890 | ✓ |
| 开户银行 | 开户银行名称 | 中国工商银行XX支行 | ✓ |
| 运营者信息 | 运营者姓名、身份证号、手机号 | - | ✓ |

4. 上传认证材料:
   - 营业执照扫描件
   - 运营者身份证正反面
   - 授权运营书(加盖公章)
5. 支付认证费用:300元/年
6. 等待审核(通常 1-7 个工作日)

### 3.3 获取 AppID 和 AppSecret

1. 登录微信公众平台
2. 进入"开发" → "基本配置"
3. 在"开发者ID(AppID)"栏目中查看:
   - **AppID**(应用唯一标识)
   - **AppSecret**(应用密钥,点击"重置"可生成新密钥)

⚠️ **重要**:
- AppSecret 只在重置时显示一次,请立即保存
- 请妥善保管 AppSecret,不要泄露给他人或提交到代码仓库
- 重置 AppSecret 后,旧密钥立即失效

### 3.4 配置网页授权域名

1. 登录微信公众平台
2. 进入"设置与开发" → "公众号设置" → "功能设置"
3. 找到"网页授权域名"栏目,点击"设置"
4. 填写授权域名:

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 网页授权域名 | 授权回调域名(不含协议和路径) | example.com | ✓ |

**重要提示**:
- 网页授权域名只填写域名,不包含 `http://`、`https://` 和路径
- 例如:填写 `example.com`,不要填写 `http://example.com/oauth2/callback`
- 域名必须已完成 ICP 备案
- 最多可配置 2 个网页授权域名
- 需要下载验证文件并上传到域名根目录进行验证

5. 下载验证文件(MP_verify_xxxxxx.txt)
6. 将验证文件上传到域名根目录(例如:http://example.com/MP_verify_xxxxxx.txt)
7. 点击"确认"完成验证

### 3.5 配置权限

微信公众号网页授权支持两种权限范围:

| Scope | 说明 | 用户体验 | 获取信息 |
|-------|------|----------|----------|
| snsapi_base | 静默授权 | 无感知,自动跳转 | 只能获取 openid |
| snsapi_userinfo | 用户确认授权 | 需要用户点击"确认登录" | 可获取用户详细信息(昵称、头像、性别、地区等) |

**推荐使用 snsapi_userinfo**,可以获取完整的用户信息。

## 4. 配置说明

### 4.1 配置参数

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用微信公众号登录 | ✓ |
| client-id | 微信公众号的 AppID | ✓ |
| client-secret | 微信公众号的 AppSecret | ✓ |
| redirect-uri | 授权回调地址(完整URL) | ✓ |
| scope | 授权范围(snsapi_base 或 snsapi_userinfo) | 可选,默认 snsapi_userinfo |

**回调地址说明**:
- 格式:`http://your-domain/oauth2/callback/wechat-mp`
- 域名部分必须与微信公众平台配置的"网页授权域名"一致
- 支持 http 和 https 协议(生产环境建议使用 https)
- 本地测试需要配置 hosts 文件映射域名到 127.0.0.1

**Scope 说明**:
- `snsapi_base`:静默授权,用户无感知,只能获取 openid
- `snsapi_userinfo`:需要用户确认,可获取用户详细信息(推荐)

## 5. 配置示例

### 5.1 application.yml 配置

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-mp:
          enabled: true
          client-id: wx1234567890abcdef
          client-secret: 1234567890abcdef1234567890abcdef
          redirect-uri: http://example.com/oauth2/callback/wechat-mp
          scope: snsapi_userinfo
```

### 5.2 本地测试配置

由于微信公众平台要求网页授权域名已备案,本地测试需要特殊配置:

**方法一:配置 hosts 文件**

1. 在微信公众平台配置网页授权域名:`test.example.com`
2. 修改本地 hosts 文件(Windows: `C:\Windows\System32\drivers\etc\hosts`,Mac/Linux: `/etc/hosts`):
   ```
   127.0.0.1 test.example.com
   ```
3. 配置文件使用:
   ```yaml
   redirect-uri: http://test.example.com:19180/oauth2/callback/wechat-mp
   ```

**方法二:使用内网穿透工具**

1. 使用 ngrok、frp 等工具将本地服务映射到公网域名
2. 在微信公众平台配置该公网域名
3. 配置文件使用公网域名

## 6. 测试指南

### 6.1 本地测试

⚠️ **重要**:微信公众号网页授权只能在微信内置浏览器中使用,无法在普通浏览器中测试。

**测试步骤**:

1. 启动您的应用
2. 生成授权链接:
   ```
   GET http://test.example.com:19180/oauth2/authorize/wechat-mp
   ```
3. 将链接转换为二维码或发送到微信
4. 在微信中打开链接
5. 如果 scope 为 snsapi_userinfo,会显示授权确认页面,点击"确认登录"
6. 授权成功后会自动跳转回您的应用

**如何在微信中打开测试链接**:
- 方法一:将链接发送给微信好友或文件传输助手,在微信中点击打开
- 方法二:使用草料二维码等工具生成二维码,在微信中扫码打开
- 方法三:在公众号菜单中配置测试链接

### 6.2 生产环境测试

1. 确保应用已部署到生产服务器
2. 确保域名已备案且可以正常访问
3. 在微信中访问授权接口:
   ```
   GET https://your-domain.com/oauth2/authorize/wechat-mp
   ```
4. 完成授权测试

### 6.3 验证登录

检查应用日志,确认登录成功:

```
INFO  - OAuth2 登录成功: platform=wechat-mp, userId=oxxxxxxxxxxxxxxxxxxxxxx
INFO  - 用户信息: nickname=张三, sex=1, province=广东, city=深圳
```

## 7. 常见问题

### 7.1 redirect_uri 参数错误

**问题描述**:回调地址域名不匹配

**错误信息**:
```
errcode: 10003
errmsg: redirect_uri域名与后台配置不一致
```

**原因**:配置的回调地址域名与微信公众平台配置的网页授权域名不一致

**解决方案**:
1. 检查配置文件中的 `redirect-uri` 参数
2. 登录微信公众平台,检查"网页授权域名"配置
3. 确保回调地址的域名部分与网页授权域名一致
4. 注意:网页授权域名不包含协议和路径,只填写域名

**示例**:
- 网页授权域名配置:`example.com`
- 正确的回调地址:`http://example.com/oauth2/callback/wechat-mp`
- 错误的回调地址:`http://www.example.com/oauth2/callback/wechat-mp`(子域名不匹配)

### 7.2 scope 参数错误

**问题描述**:授权范围配置错误

**错误信息**:
```
errcode: 10005
errmsg: scope参数错误或没有scope权限
```

**原因**:scope 参数不是 snsapi_base 或 snsapi_userinfo

**解决方案**:
1. 检查配置文件中的 `scope` 参数
2. 确保 scope 为 `snsapi_base` 或 `snsapi_userinfo`
3. 如果未配置 scope,系统默认使用 `snsapi_userinfo`

### 7.3 appid 参数错误

**问题描述**:AppID 配置错误

**错误信息**:
```
errcode: 40013
errmsg: invalid appid
```

**原因**:AppID 配置错误或公众号已被删除

**解决方案**:
1. 检查配置文件中的 `client-id` 是否正确
2. 登录微信公众平台,确认 AppID 是否正确
3. 确保复制 AppID 时没有多余的空格或换行
4. 注意区分微信开放平台、微信公众平台、微信小程序的 AppID

### 7.4 appsecret 参数错误

**问题描述**:AppSecret 配置错误

**错误信息**:
```
errcode: 40001
errmsg: invalid credential, access_token is invalid or not latest
```

**原因**:AppSecret 配置错误或已重置

**解决方案**:
1. 检查配置文件中的 `client-secret` 是否正确
2. 登录微信公众平台,确认 AppSecret 是否已重置
3. 如果 AppSecret 已重置,需要使用新的 AppSecret
4. 确保复制时没有多余的空格或换行

### 7.5 用户取消授权

**问题描述**:用户在授权页面取消授权

**错误信息**:
```
error=access_denied&error_description=user%20denied
```

**原因**:用户在授权确认页面点击了"取消"按钮

**解决方案**:
- 这是正常的用户行为
- 应用应该友好地提示用户需要授权才能登录
- 可以提供重新授权的链接

### 7.6 未备案域名

**问题描述**:使用未备案域名无法配置

**错误信息**:
```
网页授权域名未备案,请先完成域名备案
```

**原因**:微信公众平台要求网页授权域名必须已完成 ICP 备案

**解决方案**:
1. 完成域名的 ICP 备案(通过云服务商申请)
2. 备案通过后,在微信公众平台配置该域名
3. 本地测试可以使用 hosts 文件映射已备案的域名

### 7.7 只能在微信内置浏览器中使用

**问题描述**:在普通浏览器中无法完成授权

**原因**:微信公众号网页授权只能在微信内置浏览器中使用

**解决方案**:
1. 确保在微信中打开授权链接
2. 可以通过以下方式在微信中打开:
   - 将链接发送给微信好友或文件传输助手
   - 生成二维码在微信中扫码
   - 在公众号菜单中配置链接
3. 如果需要在普通浏览器中使用,请使用微信开放平台(扫码登录)

### 7.8 未认证的服务号

**问题描述**:未认证的服务号无法使用网页授权

**错误信息**:
```
该公众号暂时无法提供服务,请稍后再试
```

**原因**:服务号未完成微信认证

**解决方案**:
1. 登录微信公众平台
2. 进入"设置与开发" → "微信认证"
3. 完成微信认证(需要营业执照,300元/年)
4. 认证通过后即可使用网页授权功能

## 8. 最佳实践

### 8.1 安全建议

1. **保护 AppSecret**
   - 不要将 AppSecret 提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息
   - 定期更换 AppSecret(在微信公众平台重置)

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS 协议
   - 避免在不安全的网络环境下传输用户信息

3. **验证回调来源**
   - 验证 state 参数,防止 CSRF 攻击
   - 验证回调请求的来源域名

4. **Access Token 管理**
   - Access Token 有效期为 2 小时
   - Refresh Token 有效期为 30 天
   - 建议实现 Token 刷新机制
   - 不要频繁调用接口获取 Access Token

### 8.2 用户体验优化

1. **选择合适的 Scope**
   - 如果只需要用户标识,使用 `snsapi_base`(静默授权)
   - 如果需要用户信息,使用 `snsapi_userinfo`(需要用户确认)
   - 避免不必要的用户确认步骤

2. **清晰的授权说明**
   - 在跳转到授权页面前,告知用户将要进行的操作
   - 说明需要获取哪些信息以及用途

3. **错误处理**
   - 提供友好的错误提示
   - 对于常见错误,提供解决建议
   - 提供客服联系方式

4. **环境检测**
   - 检测是否在微信内置浏览器中
   - 如果不在微信中,提示用户在微信中打开
   - 可以生成二维码供用户扫码

### 8.3 性能优化

1. **缓存用户信息**
   - 合理缓存用户基本信息
   - 减少对微信 API 的调用频率
   - 注意用户信息的时效性

2. **异步处理**
   - 用户信息获取采用异步方式
   - 避免阻塞主流程

3. **监控告警**
   - 监控授权成功率
   - 监控 API 调用失败率
   - 设置异常告警

### 8.4 开发建议

1. **环境检测代码示例**
   ```javascript
   // 检测是否在微信内置浏览器中
   function isWechat() {
     var ua = navigator.userAgent.toLowerCase();
     return ua.indexOf('micromessenger') !== -1;
   }
   
   if (!isWechat()) {
     alert('请在微信中打开');
     // 或显示二维码
   }
   ```

2. **生成授权链接**
   ```javascript
   // 前端调用后端接口获取授权URL
   fetch('/oauth2/authorize/wechat-mp')
     .then(response => response.json())
     .then(data => {
       // 跳转到授权页面
       window.location.href = data.authorizeUrl;
     });
   ```

## 9. 参考资料

- [微信公众号网页授权文档](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html)
- [微信公众平台](https://mp.weixin.qq.com/)
- [微信公众平台接口调用频率限制](https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Explanation_of_interface_privileges.html)
- [JustAuth 文档](https://justauth.wiki)

## 10. 更新日志

- 2026-03-09:初始版本
- 文档版本:v1.0.0

## 11. 技术支持

如果您在使用过程中遇到问题,可以通过以下方式获取帮助:

1. 查看 [微信公众平台开发文档](https://developers.weixin.qq.com/doc/offiaccount/)
2. 访问 [微信开放社区](https://developers.weixin.qq.com/community/develop)
3. 联系微信公众平台客服

---

**提示**:本文档基于微信公众号网页授权开发指南编写,如有变更请以官方文档为准。
