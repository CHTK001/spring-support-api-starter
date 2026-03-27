# OAuth 模块测试清单

## 适用范围

- 服务端：`spring-api-support-oauth-starter`
- 客户端联调应用：`spring-api-support-system-starter`
- 客户端基础能力模块：`spring-support-oauth-client-starter`

## 目标

- 验证 OAuth 服务端是否可编译、可提供基础管理/授权接口。
- 验证 OAuth 客户端是否可编译，并能完成鉴权包装、Principal 注入、Servlet 角色/权限判断。
- 验证 `spring-api-support-system-starter` 作为真实客户端时，能通过 OAuth 服务端完成登录、刷新、注销、升级。
- 验证正常登录、AppKey 鉴权、加密/非加密、多协议四类核心场景。
- 区分“可自动化执行”的真实请求测试与“依赖外部第三方平台配置”的人工联调项。

## 执行策略

- 自动化真实测试：
  - 使用 Maven 编译。
  - 使用 JUnit + MockMvc 执行真实 HTTP 控制器请求。
  - 使用 JUnit + MockHttpServletRequest/MockFilterChain 执行真实 Servlet 过滤链请求。
  - 对 `spring-api-support-system-starter` 的直连场景，优先用真实 HTTP 请求覆盖登录、刷新、注销、升级。
- 人工联调测试：
  - 仅在具备真实第三方 `client-id/client-secret/redirect-uri` 时执行。
  - 登录成功回调、第三方用户绑定、Cookie/前端跳转需在真实环境补测。

## A. 构建与依赖

- [x] A1 服务端模块可编译
- [x] A2 客户端模块可编译
- [x] A3 服务端测试依赖完整
- [x] A4 客户端测试依赖完整

## B. 服务端基础接口

- [x] B1 `/admin` 管理控制台 HTML 可访问
- [x] B2 `/admin/status` 返回运行时配置
- [x] B3 `/admin/sign-verify` 可热切换签名校验
- [x] B4 `/admin/online-mode` 可热切换在线模式
- [x] B5 `/admin/rate-limit` 可热更新限流配置

## C. 服务端 OAuth2 授权接口

- [x] C1 `/oauth2/platforms` 返回平台启用状态
- [x] C2 `/oauth2/authorize/{platform}` 可生成授权 URL
- [x] C3 未启用平台返回明确错误
- [x] C4 `POST /oauth2/callback/{platform}` 在 state 过期时返回明确错误
- [x] C5 `GET /oauth2/callback/{platform}` 在 state 过期时设置错误 Cookie 并重定向

## D. 服务端外部联调项

- [ ] D1 Gitee 真实授权跳转成功
- [ ] D2 GitHub 真实授权跳转成功
- [ ] D3 Google 真实授权跳转成功
- [ ] D4 第三方登录成功后能完成系统账号绑定/登录
- [ ] D5 登录失败时前端能读取 `Access-Message` Cookie

## E. 客户端鉴权链

- [x] E1 `static` 协议下可从 token 解析出 `UserResume`
- [x] E2 `AuthFilter` 可将请求包装为 `OAuthHttpServletRequestWrapper`
- [x] E3 请求包装器可返回 `Principal`
- [x] E4 `isUserInRole()` 角色判断生效
- [x] E5 权限判断生效
- [x] E6 `logout()` 可清理 Session

## F. 客户端外部联调项

- [x] F1 客户端通过 `http` 协议接入真实 OAuth 服务端鉴权成功
- [x] F2 Bearer Token 头传递可被正确识别
- [ ] F3 Cookie Token 可被正确识别
- [x] F4 指纹校验打开后，带 `@VerifyFingerprint` 的接口可正确拦截

## G. 服务端 + System 客户端直连联调

- [x] G1 `POST /system/api/v2/user/login` 正常登录成功
- [ ] G2 错误密码登录失败
- [x] G3 `GET /system/api/v2/user/me` 可通过 accessToken 获取当前用户
- [x] G4 `POST /system/api/v2/user/refresh-token` 刷新成功
- [x] G5 `DELETE /system/api/v2/user/logout` 注销成功
- [x] G6 `POST /system/api/v2/user/upgrade` VERSION 升级成功
- [x] G7 `POST /system/api/v2/user/temporary-token` 临时令牌生成成功

## H. AppKey / AppId 鉴权

- [x] H1 AppKey 鉴权成功
- [ ] H2 AppKey 不存在时失败
- [ ] H3 AppKey 禁用时失败
- [ ] H4 AppKey 过期时失败
- [ ] H5 AppKey 缺少 `code` 参数时失败
- [ ] H6 AppId 鉴权成功
- [ ] H7 AppId 禁用或过期时失败

## I. 加密 / 非加密覆盖

- [x] I1 非加密 + 无 AK/SK
- [ ] I2 非加密 + 启用 AK/SK
- [x] I3 加密 + 无 AK/SK
- [ ] I4 加密 + 启用 AK/SK

## J. 多协议覆盖

- [x] J1 HTTP 协议
- [x] J2 Armeria 协议
- [x] J3 RSocket 协议
- [ ] J4 OAuth2 协议
- [x] J5 多协议返回的核心身份字段一致

## 当前自动化执行结果

- [x] 服务端编译
- [x] 客户端编译
- [x] 服务端管理控制台 HTTP 测试
- [x] 服务端 OAuth2 授权控制器 HTTP 测试
- [x] 客户端 `static` 协议鉴权测试
- [x] 客户端过滤器/包装器 Servlet 链路测试
- [x] `system` 真实联调矩阵
  - `armeria + 加密`
  - `armeria + 非加密`
  - `rsocket + 加密`
  - `rsocket + 非加密`
- [x] `http` / `rsocket` 直连 OAuth 服务端 `AppKey` 真实鉴权

## 本轮真实联调结果（2026-03-23）

- OAuth 服务端真实启动成功
  - `GET /oauth/admin/status` 返回协议 `armeria, rsocket`
- `spring-api-support-system-starter`
  - `POST /system/api/v2/user/login`
  - `GET /system/api/v2/user/me`
  - `GET /system/api/v2/user/menu`
  - `POST /system/api/v2/user/refresh-token`
  - `POST /system/api/v2/user/upgrade`
  - `POST /system/api/v2/user/temporary-token`
  - `DELETE /system/api/v2/user/logout`
  - 在 `armeria/rsocket × 加密/非加密` 4 组下全部真实通过
- `spring-support-oauth-client-starter`
  - 修复后 `http`、`rsocket` 协议的 `AppKey` 真实鉴权通过
  - 补齐 `RSocketProtocol#createTemporaryToken` 后，`system + rsocket` 的临时令牌链路真实通过

## 本轮已验证用例

- `spring-api-support-oauth-starter`
  - `AdminControllerStandaloneTest`
    - `/admin` HTML 管理页可返回
    - `/admin/status` 可返回运行时状态
    - `/admin/sign-verify`、`/admin/online-mode`、`/admin/rate-limit` 可热更新
  - `OAuth2LoginControllerStandaloneTest`
    - `/oauth2/platforms` 返回平台启用状态
    - `getAuthorizeUrl("gitee")` 可生成授权 URL
    - `getAuthorizeUrl("github")` 在平台未启用时返回明确错误
    - `POST /oauth2/callback/{platform}` 在 state 过期时返回错误
    - `GET /oauth2/callback/{platform}` 在 state 过期时设置错误 Cookie 并重定向
- `spring-support-oauth-client-starter`
  - `WebRequestStaticProtocolTest`
    - `static` 协议可从 token 解析 `UserResume`
    - `Authorization: Bearer <token>` 可被识别
  - `AuthFilterStaticIntegrationTest`
    - `AuthFilter` 可完成鉴权并将请求包装为 `OAuthHttpServletRequestWrapper`
    - 标记 `@VerifyFingerprint` 的接口在指纹不匹配时可返回 40301
  - `OAuthHttpServletRequestWrapperTest`
    - Principal、角色判断、权限判断、Session 登出链路可用

## 当前已知限制

- 真实第三方登录依赖有效的第三方平台配置，本地无凭证时无法自动化完成成功授权回调。
- `spring-api-support-system-starter` 默认是 `dev-static`，做真实联调时必须显式切到 `dev`，否则会走静态协议而不是本地 OAuth 服务端。
- `spring-support-oauth-client-starter` 单模块独立运行 `armeria` 真联调时，仍存在 Netty 版本冲突：
  - `NoClassDefFoundError: io/netty/channel/IoEventLoopGroup`
- `AK/SK` 两组真实联调尚未执行。
