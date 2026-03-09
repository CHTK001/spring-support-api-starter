# 微信小程序 OAuth2 登录申请指南

## 1. 概述

微信小程序登录是腾讯为小程序提供的用户身份验证服务。通过微信小程序登录,用户可以在小程序中快速完成身份认证,无需注册新账号。

**官方网站**:https://mp.weixin.qq.com  
**管理中心**:https://mp.weixin.qq.com

**适用场景**:
- 微信小程序用户登录
- 小程序内的用户身份验证
- 需要获取微信用户 openid 的场景
- 小程序与后端服务的用户关联

⚠️ **重要特点**:
- 登录流程与网页授权不同,使用 wx.login() 获取 code
- 不需要配置回调地址(redirect-uri)
- 需要配置服务器域名(request 合法域名)
- 服务器域名必须支持 https

## 2. 前置条件

- [x] 微信账号
- [x] 小程序名称、简介、图标
- [x] 服务器域名(需要已备案且支持 https)
- [ ] 企业认证(可选,个人小程序也可使用登录功能)

⚠️ **重要提示**:
- 个人和企业都可以注册小程序
- 小程序登录功能无需认证即可使用
- 服务器域名必须已完成 ICP 备案
- 服务器域名必须支持 https 协议

## 3. 申请流程

### 3.1 注册小程序账号

1. 访问 [微信公众平台](https://mp.weixin.qq.com)
2. 点击右上角"立即注册"按钮
3. 选择账号类型:
   - **小程序**:选择此类型
4. 填写账号信息:
   - 邮箱地址:用于接收通知
   - 密码:设置登录密码
5. 邮箱激活后,选择主体类型:
   - 个人
   - 企业
   - 政府
   - 其他组织

### 3.2 完善小程序信息

1. 登录微信公众平台
2. 进入"设置" → "基本设置"
3. 填写小程序信息:

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| 小程序名称 | 显示给用户的小程序名称 | My Mini Program | ✓ |
| 小程序简介 | 小程序的简要描述 | 这是一个提供XX服务的小程序 | ✓ |
| 小程序头像 | 小程序图标(建议尺寸:144x144) | - | ✓ |
| 服务类目 | 小程序的服务类别 | 工具-信息查询 | ✓ |

4. 提交审核
5. 审核通过后,小程序信息生效

### 3.3 获取 AppID 和 AppSecret

1. 登录微信公众平台
2. 进入"开发" → "开发管理" → "开发设置"
3. 在"开发者ID"栏目中查看:
   - **AppID**(小程序唯一标识)
   - **AppSecret**(小程序密钥,点击"生成"或"重置"可获取)

⚠️ **重要**:
- AppSecret 只在生成/重置时显示一次,请立即保存
- 请妥善保管 AppSecret,不要泄露给他人或提交到代码仓库
- 重置 AppSecret 后,旧密钥立即失效

### 3.4 配置服务器域名

1. 登录微信公众平台
2. 进入"开发" → "开发管理" → "开发设置"
3. 找到"服务器域名"栏目,点击"修改"
4. 配置 request 合法域名:

| 字段 | 说明 | 示例 | 必填 |
|------|------|------|------|
| request 合法域名 | 小程序可以发起网络请求的域名 | https://api.example.com | ✓ |

**重要提示**:
- 服务器域名必须使用 https:// 协议
- 域名必须已完成 ICP 备案
- 最多可配置 20 个域名
- 配置后需要等待几分钟生效
- 开发工具中可以勾选"不校验合法域名"进行本地测试

5. 点击"保存并提交"完成配置

## 4. 配置说明

### 4.1 配置参数

| 参数 | 说明 | 必填 |
|------|------|------|
| enabled | 是否启用微信小程序登录 | ✓ |
| client-id | 微信小程序的 AppID | ✓ |
| client-secret | 微信小程序的 AppSecret | ✓ |

**与其他登录方式的区别**:
- 小程序登录不需要配置 redirect-uri(回调地址)
- 小程序登录不需要配置 scope(权限范围)
- 小程序通过 wx.login() 获取 code,然后调用后端接口

## 5. 登录流程

### 5.1 小程序端流程

```javascript
// 1. 调用 wx.login() 获取 code
wx.login({
  success: (res) => {
    if (res.code) {
      // 2. 将 code 发送到后端
      wx.request({
        url: 'https://api.example.com/oauth2/login/wechat-mini',
        method: 'POST',
        data: {
          code: res.code
        },
        success: (response) => {
          // 3. 保存后端返回的 token
          wx.setStorageSync('token', response.data.token);
          // 4. 跳转到首页
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      });
    } else {
      console.log('登录失败!' + res.errMsg);
    }
  }
});
```

### 5.2 后端流程

1. 接收小程序传来的 code
2. 使用 code 调用微信接口换取 session_key 和 openid
3. 根据 openid 查询或创建用户
4. 生成自定义登录态(token)返回给小程序

**接口地址**:
```
POST /oauth2/login/wechat-mini
Content-Type: application/json

{
  "code": "081xxxxx"
}
```

**返回示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "openid": "oxxxxxxxxxxxxxxxxxxxxxx",
    "userId": "123456"
  }
}
```

## 6. 配置示例

### 6.1 application.yml 配置

```yaml
plugin:
  oauth:
    third-party:
      login:
        wechat-mini:
          enabled: true
          client-id: wx1234567890abcdef
          client-secret: 1234567890abcdef1234567890abcdef
```

### 6.2 本地测试配置

**方法一:使用微信开发者工具**

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 在开发者工具中打开小程序项目
3. 在"详情" → "本地设置"中勾选:
   - 不校验合法域名、web-view(业务域名)、TLS 版本以及 HTTPS 证书
4. 配置后端服务地址为本地地址:
   ```javascript
   wx.request({
     url: 'http://127.0.0.1:19180/oauth2/login/wechat-mini',
     // ...
   });
   ```

**方法二:使用内网穿透工具**

1. 使用 ngrok、frp 等工具将本地服务映射到公网域名
2. 在微信公众平台配置该公网域名(必须支持 https)
3. 小程序中使用公网域名

## 7. 测试指南

### 7.1 开发工具测试

1. 启动后端服务
2. 打开微信开发者工具
3. 在开发者工具中运行小程序
4. 点击登录按钮,触发 wx.login()
5. 查看控制台日志,确认 code 获取成功
6. 查看网络请求,确认后端接口调用成功
7. 查看后端日志,确认登录流程完成

### 7.2 真机测试

1. 确保后端服务已部署到生产环境
2. 确保服务器域名已配置且支持 https
3. 在微信开发者工具中点击"预览"
4. 使用微信扫描二维码
5. 在真机上测试登录功能

### 7.3 验证登录

检查后端日志,确认登录成功:

```
INFO  - 微信小程序登录: code=081xxxxx
INFO  - 获取 session_key 成功: openid=oxxxxxxxxxxxxxxxxxxxxxx
INFO  - 用户登录成功: userId=123456, openid=oxxxxxxxxxxxxxxxxxxxxxx
```

## 8. 手机号授权

### 8.1 授权流程

微信小程序支持获取用户手机号,需要用户主动授权。

**小程序端代码**:
```javascript
// 方式1:使用 button 组件
<button open-type="getPhoneNumber" bindgetphonenumber="getPhoneNumber">
  授权手机号
</button>

// 方式2:使用 API(基础库 2.21.2 起支持)
wx.getPhoneNumber({
  success: (res) => {
    // 发送到后端解密
    wx.request({
      url: 'https://api.example.com/oauth2/wechat_mini/phone',
      method: 'POST',
      data: {
        encryptedData: res.encryptedData,
        iv: res.iv,
        openid: wx.getStorageSync('openid')
      },
      success: (response) => {
        console.log('手机号:', response.data.data.phoneNumber);
      }
    });
  }
});
```

**后端接口**:
```
POST /oauth2/wechat_mini/phone
Content-Type: application/json

{
  "encryptedData": "xxx",
  "iv": "xxx",
  "openid": "oxxxxxxxxxxxxxxxxxxxxxx"
}
```

**返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "13800138000",
    "purePhoneNumber": "13800138000",
    "countryCode": "86"
  }
}
```

### 8.2 注意事项

- 手机号授权需要用户主动点击按钮
- 个人小程序无法使用手机号授权功能,需要企业认证
- session_key 有效期为 3 天,过期后需要重新登录
- 每次调用 wx.login() 都会刷新 session_key

## 9. 用户信息授权

### 9.1 授权流程

微信小程序支持获取用户基本信息(昵称、头像等),需要用户主动授权。

⚠️ **重要变更**:
- wx.getUserInfo 已废弃,不再弹出授权框
- 建议使用 wx.getUserProfile 获取用户信息
- 每次调用都需要用户确认授权

**小程序端代码**:
```javascript
// 推荐方式:使用 wx.getUserProfile
wx.getUserProfile({
  desc: '用于完善用户资料',
  success: (res) => {
    // 发送到后端解密
    wx.request({
      url: 'https://api.example.com/oauth2/wechat_mini/userinfo',
      method: 'POST',
      data: {
        encryptedData: res.encryptedData,
        iv: res.iv,
        openid: wx.getStorageSync('openid')
      },
      success: (response) => {
        console.log('用户信息:', response.data.data);
        // 保存用户信息
        wx.setStorageSync('userInfo', response.data.data);
      }
    });
  }
});

// 旧方式:使用 button 组件(已废弃)
<button open-type="getUserInfo" bindgetuserinfo="getUserInfo">
  授权用户信息
</button>
```

**后端接口**:
```
POST /oauth2/wechat_mini/userinfo
Content-Type: application/json

{
  "encryptedData": "xxx",
  "iv": "xxx",
  "openid": "oxxxxxxxxxxxxxxxxxxxxxx"
}
```

**返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "nickName": "张三",
    "avatarUrl": "https://wx.qlogo.cn/...",
    "gender": "1",
    "country": "中国",
    "province": "广东",
    "city": "深圳",
    "language": "zh_CN"
  }
}
```

### 9.2 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| nickName | string | 用户昵称 |
| avatarUrl | string | 用户头像图片的 URL |
| gender | string | 用户性别(0:未知、1:男、2:女) |
| country | string | 用户所在国家 |
| province | string | 用户所在省份 |
| city | string | 用户所在城市 |
| language | string | 用户的语言(zh_CN、zh_TW、en) |

### 9.3 注意事项

- 用户信息授权需要用户主动触发
- wx.getUserInfo 已废弃,建议使用 wx.getUserProfile
- 每次调用 wx.getUserProfile 都需要用户确认
- 获取的头像和昵称可能是默认值,需要引导用户填写真实信息
- session_key 有效期为 3 天,过期后需要重新登录

### 9.4 完整示例

```javascript
// 完整的用户信息获取流程
Page({
  data: {
    userInfo: null,
    hasUserInfo: false
  },
  
  onLoad() {
    // 检查是否已有用户信息
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        userInfo: userInfo,
        hasUserInfo: true
      });
    }
  },
  
  getUserProfile() {
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (res) => {
        // 发送到后端解密
        wx.request({
          url: 'https://api.example.com/oauth2/wechat_mini/userinfo',
          method: 'POST',
          data: {
            encryptedData: res.encryptedData,
            iv: res.iv,
            openid: wx.getStorageSync('openid')
          },
          success: (response) => {
            if (response.data.code === 200) {
              const userInfo = response.data.data;
              // 保存用户信息
              wx.setStorageSync('userInfo', userInfo);
              this.setData({
                userInfo: userInfo,
                hasUserInfo: true
              });
              wx.showToast({
                title: '授权成功',
                icon: 'success'
              });
            } else {
              wx.showToast({
                title: response.data.message,
                icon: 'none'
              });
            }
          },
          fail: (err) => {
            wx.showToast({
              title: '网络请求失败',
              icon: 'none'
            });
          }
        });
      },
      fail: (err) => {
        wx.showToast({
          title: '授权失败',
          icon: 'none'
        });
      }
    });
  }
});
```

## 10. 常见问题

### 10.1 code 已使用

**问题描述**:code 只能使用一次

**错误信息**:
```json
{
  "errcode": 40163,
  "errmsg": "code been used"
}
```

**原因**:code 只能使用一次,5分钟内有效

**解决方案**:
1. 确保 code 只使用一次
2. 如果需要重新登录,重新调用 wx.login() 获取新的 code
3. 不要在多个地方同时使用同一个 code

### 10.2 appid 不匹配

**问题描述**:AppID 配置错误

**错误信息**:
```json
{
  "errcode": 40013,
  "errmsg": "invalid appid"
}
```

**原因**:AppID 配置错误或小程序已被删除

**解决方案**:
1. 检查配置文件中的 `client-id` 是否正确
2. 登录微信公众平台,确认 AppID 是否正确
3. 确保复制 AppID 时没有多余的空格或换行
4. 注意区分微信开放平台、微信公众号、微信小程序的 AppID

### 10.3 appsecret 参数错误

**问题描述**:AppSecret 配置错误

**错误信息**:
```json
{
  "errcode": 40001,
  "errmsg": "invalid credential, access_token is invalid or not latest"
}
```

**原因**:AppSecret 配置错误或已重置

**解决方案**:
1. 检查配置文件中的 `client-secret` 是否正确
2. 登录微信公众平台,确认 AppSecret 是否已重置
3. 如果 AppSecret 已重置,需要使用新的 AppSecret
4. 确保复制时没有多余的空格或换行

### 10.4 服务器域名未配置

**问题描述**:request 合法域名未配置

**错误信息**:
```
request:fail url not in domain list
```

**原因**:小程序请求的域名不在配置的服务器域名列表中

**解决方案**:
1. 登录微信公众平台
2. 进入"开发" → "开发管理" → "开发设置"
3. 在"服务器域名"中添加后端服务域名
4. 确保域名使用 https:// 协议
5. 开发阶段可以在开发者工具中勾选"不校验合法域名"

### 10.5 code 过期

**问题描述**:code 超过 5 分钟未使用

**错误信息**:
```json
{
  "errcode": 40029,
  "errmsg": "invalid code"
}
```

**原因**:code 有效期为 5 分钟,超时后失效

**解决方案**:
1. 获取 code 后立即发送到后端
2. 不要在前端缓存 code
3. 如果 code 过期,重新调用 wx.login() 获取新的 code

### 10.6 频繁调用限制

**问题描述**:接口调用频率超限

**错误信息**:
```json
{
  "errcode": 45011,
  "errmsg": "api minute-quota reach limit"
}
```

**原因**:接口调用频率超过限制(100次/分钟)

**解决方案**:
1. 优化登录逻辑,避免频繁调用 wx.login()
2. 实现登录态缓存,避免重复登录
3. 使用 token 机制,延长登录态有效期

### 10.7 https 证书问题

**问题描述**:服务器 https 证书无效

**错误信息**:
```
request:fail ssl hand shake error
```

**原因**:服务器 https 证书无效或过期

**解决方案**:
1. 确保服务器使用有效的 https 证书
2. 证书必须由受信任的 CA 机构签发
3. 自签名证书无法在小程序中使用
4. 开发阶段可以在开发者工具中勾选"不校验合法域名"

## 11. 最佳实践

### 11.1 安全建议

1. **保护 AppSecret**
   - 不要将 AppSecret 提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息
   - 定期更换 AppSecret(在微信公众平台重置)

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS 协议
   - 使用有效的 SSL 证书
   - 避免在不安全的网络环境下传输用户信息

3. **Session Key 管理**
   - session_key 用于解密用户信息,请妥善保管
   - 不要将 session_key 返回给前端
   - session_key 有效期为 3 天,过期后需要重新登录

4. **Token 管理**
   - 生成自定义登录态(token)返回给小程序
   - Token 应该有合理的过期时间(如 7 天)
   - 实现 Token 刷新机制

### 11.2 用户体验优化

1. **静默登录**
   - 小程序启动时自动检查登录态
   - 如果 token 有效,直接进入首页
   - 如果 token 失效,自动调用 wx.login() 静默登录

2. **登录态管理**
   ```javascript
   // 检查登录态
   function checkLogin() {
     const token = wx.getStorageSync('token');
     if (token) {
       // 验证 token 是否有效
       wx.request({
         url: 'https://api.example.com/user/info',
         header: {
           'Authorization': 'Bearer ' + token
         },
         success: (res) => {
           if (res.statusCode === 200) {
             // token 有效,已登录
             return true;
           } else {
             // token 无效,需要重新登录
             doLogin();
           }
         }
       });
     } else {
       // 未登录,执行登录
       doLogin();
     }
   }
   ```

3. **错误处理**
   - 提供友好的错误提示
   - 对于常见错误,提供解决建议
   - 登录失败时提供重试按钮

4. **加载提示**
   ```javascript
   wx.showLoading({
     title: '登录中...',
     mask: true
   });
   
   // 登录完成后
   wx.hideLoading();
   ```

### 11.3 性能优化

1. **缓存登录态**
   - 使用 wx.setStorageSync() 缓存 token
   - 避免频繁调用 wx.login()
   - 实现 token 自动刷新机制

2. **异步处理**
   - 登录流程采用异步方式
   - 避免阻塞小程序启动

3. **监控告警**
   - 监控登录成功率
   - 监控 API 调用失败率
   - 设置异常告警

### 11.4 开发建议

1. **登录流程封装**
   ```javascript
   // utils/login.js
   function login() {
     return new Promise((resolve, reject) => {
       wx.login({
         success: (res) => {
           if (res.code) {
             wx.request({
               url: 'https://api.example.com/oauth2/login/wechat-mini',
               method: 'POST',
               data: { code: res.code },
               success: (response) => {
                 if (response.data.code === 200) {
                   wx.setStorageSync('token', response.data.data.token);
                   resolve(response.data.data);
                 } else {
                   reject(response.data.message);
                 }
               },
               fail: reject
             });
           } else {
             reject('获取 code 失败');
           }
         },
         fail: reject
       });
     });
   }
   
   module.exports = { login };
   ```

2. **请求拦截器**
   ```javascript
   // utils/request.js
   function request(options) {
     const token = wx.getStorageSync('token');
     
     return new Promise((resolve, reject) => {
       wx.request({
         ...options,
         header: {
           ...options.header,
           'Authorization': 'Bearer ' + token
         },
         success: (res) => {
           if (res.statusCode === 401) {
             // token 失效,重新登录
             login().then(() => {
               // 重新发起请求
               request(options).then(resolve).catch(reject);
             });
           } else {
             resolve(res);
           }
         },
         fail: reject
       });
     });
   }
   
   module.exports = { request };
   ```

## 12. 参考资料

- [微信小程序登录文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html)
- [微信小程序 API 文档](https://developers.weixin.qq.com/miniprogram/dev/api/)
- [微信公众平台](https://mp.weixin.qq.com/)
- [微信开发者工具下载](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
- [JustAuth 文档](https://justauth.wiki)

## 13. 更新日志

- 2026-03-09:初始版本
- 文档版本:v1.0.0

## 14. 技术支持

如果您在使用过程中遇到问题,可以通过以下方式获取帮助:

1. 查看 [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
2. 访问 [微信开放社区](https://developers.weixin.qq.com/community/develop)
3. 联系微信小程序客服

---

**提示**:本文档基于微信小程序登录开发指南编写,如有变更请以官方文档为准。
