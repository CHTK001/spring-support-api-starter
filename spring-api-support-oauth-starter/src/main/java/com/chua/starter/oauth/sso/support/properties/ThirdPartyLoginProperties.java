package com.chua.starter.oauth.sso.support.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 第三方 OAuth2 登录配置
 * 
 * <p>
 * 提供统一的第三方平台 OAuth2 登录配置管理，支持多种主流平台的快速集成。
 * 基于 <a href="https://justauth.wiki">JustAuth</a> 实现，简化第三方登录接入流程。
 * </p>
 * 
 * <h3>支持的平台（13个）</h3>
 * <ul>
 *   <li><b>Gitee</b> - 码云，中国最大的代码托管平台</li>
 *   <li><b>Github</b> - 全球最大的代码托管平台</li>
 *   <li><b>微信开放平台</b> - 网站应用扫码登录</li>
 *   <li><b>微信公众号</b> - 公众号网页授权登录</li>
 *   <li><b>微信小程序</b> - 小程序登录授权</li>
 *   <li><b>企业微信</b> - 企业内部应用登录</li>
 *   <li><b>QQ互联</b> - QQ账号登录</li>
 *   <li><b>支付宝</b> - 支付宝账号登录</li>
 *   <li><b>钉钉</b> - 钉钉企业登录</li>
 *   <li><b>新浪微博</b> - 微博账号登录</li>
 *   <li><b>百度</b> - 百度账号登录</li>
 *   <li><b>Google</b> - Google账号登录</li>
 *   <li><b>Microsoft</b> - 微软账号登录</li>
 * </ul>
 * 
 * <h3>配置示例</h3>
 * <pre>{@code
 * plugin:
 *   oauth:
 *     third-party:
 *       login:
 *         gitee:
 *           enabled: true
 *           client-id: your_client_id
 *           client-secret: your_client_secret
 *           redirect-uri: http://your-domain/oauth2/callback/gitee
 *         github:
 *           enabled: true
 *           client-id: your_client_id
 *           client-secret: your_client_secret
 *           redirect-uri: http://your-domain/oauth2/callback/github
 * }</pre>
 * 
 * <h3>使用流程</h3>
 * <ol>
 *   <li>在第三方平台申请应用，获取 Client ID 和 Client Secret</li>
 *   <li>配置回调地址为：http://your-domain/oauth2/callback/{platform}</li>
 *   <li>在 application.yml 中配置相应平台的信息</li>
 *   <li>启用平台：设置 enabled: true</li>
 *   <li>前端调用 /oauth2/authorize/{platform} 获取授权URL</li>
 *   <li>用户授权后自动回调并完成登录</li>
 * </ol>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li>回调地址必须与第三方平台配置完全一致（包括协议、域名、端口、路径）</li>
 *   <li>本地测试可使用 http://127.0.0.1 或 http://localhost</li>
 *   <li>生产环境建议使用 https:// 协议</li>
 *   <li>Client Secret 请妥善保管，不要泄露</li>
 * </ul>
 * 
 * <h3>相关文档</h3>
 * <ul>
 *   <li><a href="https://justauth.wiki">JustAuth 官方文档</a></li>
 *   <li>各平台申请指南请参考 docs/third-party-login/ 目录</li>
 * </ul>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ConfigurationProperties(prefix = ThirdPartyLoginProperties.PRE, ignoreUnknownFields = true)
public class ThirdPartyLoginProperties {

    /**
     * 配置前缀
     * <p>支持两种配置前缀（向后兼容）：</p>
     * <ul>
     *   <li>新前缀：plugin.oauth.third-party.login（推荐）</li>
     *   <li>旧前缀：plugin.oauth.third.login（兼容）</li>
     * </ul>
     */
    public static final String PRE = "plugin.oauth.third-party.login";

    /**
     * Gitee 码云配置
     */
    private Gitee gitee = new Gitee();

    /**
     * Github 配置
     */
    private Github github = new Github();

    /**
     * 微信开放平台配置（网站应用扫码登录）
     */
    private WechatOpen wechatOpen = new WechatOpen();

    /**
     * 微信公众号配置（网页授权登录）
     */
    private WechatMp wechatMp = new WechatMp();

    /**
     * 微信小程序配置
     */
    private WechatMini wechatMini = new WechatMini();

    /**
     * QQ互联配置
     */
    private Qq qq = new Qq();

    /**
     * 支付宝配置
     */
    private Alipay alipay = new Alipay();

    /**
     * 钉钉配置
     */
    private DingTalk dingtalk = new DingTalk();

    /**
     * 企业微信配置
     */
    private WechatEnterprise wechatEnterprise = new WechatEnterprise();

    /**
     * 新浪微博配置
     */
    private Weibo weibo = new Weibo();

    /**
     * 百度配置
     */
    private Baidu baidu = new Baidu();

    /**
     * Google 配置
     */
    private Google google = new Google();

    /**
     * Microsoft 配置
     */
    private Microsoft microsoft = new Microsoft();

    // ==================== 平台配置类 ====================

    /**
     * Gitee（码云）OAuth2 登录配置
     * 
     * <h3>申请流程</h3>
     * <ol>
     *   <li>访问 <a href="https://gitee.com/oauth/applications">Gitee 第三方应用</a></li>
     *   <li>点击"创建应用"按钮</li>
     *   <li>填写应用信息：
     *     <ul>
     *       <li>应用名称：您的应用名称</li>
     *       <li>应用主页：您的应用主页地址</li>
     *       <li>应用描述：应用的简要描述</li>
     *       <li>应用回调地址：http://your-domain/oauth2/callback/gitee</li>
     *     </ul>
     *   </li>
     *   <li>提交后获取 Client ID 和 Client Secret</li>
     *   <li>配置权限范围（默认：user_info、emails）</li>
     * </ol>
     * 
     * <h3>所需材料</h3>
     * <ul>
     *   <li>Gitee 账号（已实名认证）</li>
     *   <li>应用图标（可选，建议尺寸：200x200）</li>
     *   <li>应用主页地址</li>
     * </ul>
     * 
     * <h3>配置参数</h3>
     * <ul>
     *   <li><b>enabled</b> - 是否启用 Gitee 登录，默认 false</li>
     *   <li><b>client-id</b> - Gitee 应用的 Client ID</li>
     *   <li><b>client-secret</b> - Gitee 应用的 Client Secret</li>
     *   <li><b>redirect-uri</b> - 授权回调地址，必须与 Gitee 应用配置一致</li>
     *   <li><b>scope</b> - 权限范围，默认：user_info</li>
     * </ul>
     * 
     * <h3>权限范围（Scope）</h3>
     * <ul>
     *   <li><b>user_info</b> - 访问用户的个人信息（必选）</li>
     *   <li><b>projects</b> - 访问用户的项目信息</li>
     *   <li><b>pull_requests</b> - 访问用户的 Pull Request</li>
     *   <li><b>issues</b> - 访问用户的 Issue</li>
     *   <li><b>notes</b> - 访问用户的评论</li>
     *   <li><b>keys</b> - 访问用户的 SSH 公钥</li>
     *   <li><b>hook</b> - 访问用户的 Webhook</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>回调地址必须使用 http:// 或 https:// 开头</li>
     *   <li>本地测试可以使用 http://127.0.0.1 或 http://localhost</li>
     *   <li>生产环境建议使用 https:// 协议</li>
     *   <li>回调地址必须与 Gitee 应用配置完全一致（包括端口号）</li>
     * </ul>
     * 
     * <h3>常见问题</h3>
     * <ul>
     *   <li><b>redirect_uri_mismatch</b>：回调地址不匹配<br>
     *       解决方案：检查配置的 redirect-uri 是否与 Gitee 应用配置一致</li>
     *   <li><b>invalid_client</b>：Client ID 或 Client Secret 错误<br>
     *       解决方案：检查配置的 client-id 和 client-secret 是否正确</li>
     *   <li><b>access_denied</b>：用户拒绝授权<br>
     *       解决方案：用户需要同意授权才能完成登录</li>
     * </ul>
     * 
     * <h3>配置示例</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         gitee:
     *           enabled: true
     *           client-id: f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943
     *           client-secret: 158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7
     *           redirect-uri: http://127.0.0.1:19180/oauth2/callback/gitee
     *           scope: user_info
     * }</pre>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://gitee.com/api/v5/oauth_doc">Gitee OAuth2 文档</a></li>
     *   <li><a href="https://gitee.com/oauth/applications">Gitee 第三方应用管理</a></li>
     * </ul>
     * 
     * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Gitee extends Base {
        public Gitee() {
            this.setClientId("f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943");
            this.setClientSecret("158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7");
            this.setRedirectUri("http://127.0.0.1:19180/oauth/gitee/login/callback");
        }
    }

    /**
     * Github OAuth2 登录配置
     * 
     * <h3>申请流程</h3>
     * <ol>
     *   <li>访问 <a href="https://github.com/settings/developers">Github Developer Settings</a></li>
     *   <li>点击 "OAuth Apps" → "New OAuth App"</li>
     *   <li>填写应用信息：
     *     <ul>
     *       <li>Application name：您的应用名称</li>
     *       <li>Homepage URL：您的应用主页地址</li>
     *       <li>Application description：应用描述（可选）</li>
     *       <li>Authorization callback URL：http://your-domain/oauth2/callback/github</li>
     *     </ul>
     *   </li>
     *   <li>点击 "Register application"</li>
     *   <li>获取 Client ID，点击 "Generate a new client secret" 生成 Client Secret</li>
     * </ol>
     * 
     * <h3>所需材料</h3>
     * <ul>
     *   <li>Github 账号</li>
     *   <li>应用主页地址</li>
     *   <li>应用图标（可选）</li>
     * </ul>
     * 
     * <h3>配置参数</h3>
     * <ul>
     *   <li><b>enabled</b> - 是否启用 Github 登录，默认 false</li>
     *   <li><b>client-id</b> - Github 应用的 Client ID</li>
     *   <li><b>client-secret</b> - Github 应用的 Client Secret</li>
     *   <li><b>redirect-uri</b> - 授权回调地址，必须与 Github 应用配置一致</li>
     *   <li><b>scope</b> - 权限范围，默认：user</li>
     * </ul>
     * 
     * <h3>权限范围（Scope）</h3>
     * <ul>
     *   <li><b>user</b> - 访问用户的个人信息（推荐）</li>
     *   <li><b>user:email</b> - 访问用户的邮箱地址</li>
     *   <li><b>read:user</b> - 只读访问用户信息</li>
     *   <li><b>repo</b> - 访问用户的仓库信息</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>回调地址必须使用 http:// 或 https:// 开头</li>
     *   <li>本地测试可以使用 http://127.0.0.1 或 http://localhost</li>
     *   <li>生产环境建议使用 https:// 协议</li>
     *   <li>Client Secret 只显示一次，请妥善保管</li>
     * </ul>
     * 
     * <h3>常见问题</h3>
     * <ul>
     *   <li><b>redirect_uri_mismatch</b>：回调地址不匹配<br>
     *       解决方案：检查配置的 redirect-uri 是否与 Github 应用配置一致</li>
     *   <li><b>incorrect_client_credentials</b>：Client ID 或 Client Secret 错误<br>
     *       解决方案：检查配置的 client-id 和 client-secret 是否正确</li>
     * </ul>
     * 
     * <h3>配置示例</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         github:
     *           enabled: true
     *           client-id: Iv1.a629723000000000
     *           client-secret: 3d7a6d8c4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b
     *           redirect-uri: http://127.0.0.1:19180/oauth2/callback/github
     *           scope: user
     * }</pre>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://docs.github.com/en/developers/apps/building-oauth-apps">Github OAuth Apps 文档</a></li>
     *   <li><a href="https://github.com/settings/developers">Github Developer Settings</a></li>
     * </ul>
     * 
     * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Github extends Base {
    }

    /**
     * 微信开放平台 OAuth2 登录配置（网站应用扫码登录）
     * 
     * <h3>申请流程</h3>
     * <ol>
     *   <li>访问 <a href="https://open.weixin.qq.com/">微信开放平台</a></li>
     *   <li>注册开发者账号并完成开发者资质认证（需要营业执照）</li>
     *   <li>创建网站应用：
     *     <ul>
     *       <li>应用名称：您的应用名称</li>
     *       <li>应用简介：应用的简要描述</li>
     *       <li>应用官网：您的应用官网地址</li>
     *       <li>授权回调域：your-domain.com（不包含http://和路径）</li>
     *     </ul>
     *   </li>
     *   <li>提交审核，审核通过后获取 AppID 和 AppSecret</li>
     * </ol>
     * 
     * <h3>所需材料</h3>
     * <ul>
     *   <li>企业营业执照或个体工商户营业执照</li>
     *   <li>对公账户信息（用于认证）</li>
     *   <li>应用图标（108x108）</li>
     *   <li>应用官网地址</li>
     *   <li>已备案的域名</li>
     * </ul>
     * 
     * <h3>配置参数</h3>
     * <ul>
     *   <li><b>enabled</b> - 是否启用微信开放平台登录，默认 false</li>
     *   <li><b>client-id</b> - 微信开放平台应用的 AppID</li>
     *   <li><b>client-secret</b> - 微信开放平台应用的 AppSecret</li>
     *   <li><b>redirect-uri</b> - 授权回调地址，域名必须与开放平台配置一致</li>
     * </ul>
     * 
     * <h3>权限范围（Scope）</h3>
     * <ul>
     *   <li><b>snsapi_login</b> - 网站应用微信登录（默认）</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>需要企业资质认证，个人无法申请</li>
     *   <li>回调域名必须已备案</li>
     *   <li>回调域名不包含 http:// 和路径，只填写域名</li>
     *   <li>本地测试需要配置 hosts 文件映射域名到 127.0.0.1</li>
     *   <li>AppSecret 请妥善保管，不要泄露</li>
     * </ul>
     * 
     * <h3>常见问题</h3>
     * <ul>
     *   <li><b>redirect_uri 参数错误</b>：回调地址域名不匹配<br>
     *       解决方案：检查回调地址的域名是否与开放平台配置的授权回调域一致</li>
     *   <li><b>appid 参数错误</b>：AppID 配置错误<br>
     *       解决方案：检查配置的 client-id 是否正确</li>
     * </ul>
     * 
     * <h3>配置示例</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         wechat-open:
     *           enabled: true
     *           client-id: wx1234567890abcdef
     *           client-secret: 1234567890abcdef1234567890abcdef
     *           redirect-uri: http://your-domain.com/oauth2/callback/wechat-open
     * }</pre>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html">微信开放平台网站应用开发文档</a></li>
     *   <li><a href="https://open.weixin.qq.com/">微信开放平台</a></li>
     * </ul>
     * 
     * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WechatOpen extends Base {
    }

    /**
     * 微信公众号 OAuth2 登录配置（网页授权登录）
     * 
     * <h3>申请流程</h3>
     * <ol>
     *   <li>访问 <a href="https://mp.weixin.qq.com/">微信公众平台</a></li>
     *   <li>注册公众号（服务号或订阅号）</li>
     *   <li>完成微信认证（需要营业执照，300元/年）</li>
     *   <li>在"开发"-"基本配置"中获取 AppID 和 AppSecret</li>
     *   <li>在"设置与开发"-"公众号设置"-"功能设置"中配置"网页授权域名"</li>
     * </ol>
     * 
     * <h3>所需材料</h3>
     * <ul>
     *   <li>企业营业执照或个体工商户营业执照（认证用）</li>
     *   <li>对公账户信息（认证用）</li>
     *   <li>已备案的域名</li>
     *   <li>认证费用：300元/年</li>
     * </ul>
     * 
     * <h3>配置参数</h3>
     * <ul>
     *   <li><b>enabled</b> - 是否启用微信公众号登录，默认 false</li>
     *   <li><b>client-id</b> - 微信公众号的 AppID</li>
     *   <li><b>client-secret</b> - 微信公众号的 AppSecret</li>
     *   <li><b>redirect-uri</b> - 授权回调地址，域名必须与公众号配置一致</li>
     * </ul>
     * 
     * <h3>权限范围（Scope）</h3>
     * <ul>
     *   <li><b>snsapi_base</b> - 静默授权，只能获取用户 openid</li>
     *   <li><b>snsapi_userinfo</b> - 需要用户确认，可获取用户详细信息（默认）</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>只有认证的服务号才支持网页授权</li>
     *   <li>网页授权域名必须已备案</li>
     *   <li>网页授权域名不包含 http:// 和路径，只填写域名</li>
     *   <li>本地测试需要配置 hosts 文件映射域名到 127.0.0.1</li>
     *   <li>需要在微信内置浏览器中打开</li>
     * </ul>
     * 
     * <h3>常见问题</h3>
     * <ul>
     *   <li><b>redirect_uri 参数错误</b>：回调地址域名不匹配<br>
     *       解决方案：检查回调地址的域名是否与公众号配置的网页授权域名一致</li>
     *   <li><b>scope 参数错误</b>：权限范围配置错误<br>
     *       解决方案：检查 scope 是否为 snsapi_base 或 snsapi_userinfo</li>
     * </ul>
     * 
     * <h3>配置示例</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         wechat-mp:
     *           enabled: true
     *           client-id: wx1234567890abcdef
     *           client-secret: 1234567890abcdef1234567890abcdef
     *           redirect-uri: http://your-domain.com/oauth2/callback/wechat-mp
     *           scope: snsapi_userinfo
     * }</pre>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html">微信公众号网页授权文档</a></li>
     *   <li><a href="https://mp.weixin.qq.com/">微信公众平台</a></li>
     * </ul>
     * 
     * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WechatMp extends Base {
    }

    /**
     * 微信小程序 OAuth2 登录配置
     * 
     * <h3>申请流程</h3>
     * <ol>
     *   <li>访问 <a href="https://mp.weixin.qq.com/">微信公众平台</a></li>
     *   <li>注册小程序账号</li>
     *   <li>完成小程序信息填写</li>
     *   <li>在"开发"-"开发管理"-"开发设置"中获取 AppID 和 AppSecret</li>
     *   <li>配置服务器域名（request 合法域名）</li>
     * </ol>
     * 
     * <h3>所需材料</h3>
     * <ul>
     *   <li>微信账号</li>
     *   <li>小程序名称、简介、图标</li>
     *   <li>服务器域名（需要已备案且支持 https）</li>
     * </ul>
     * 
     * <h3>配置参数</h3>
     * <ul>
     *   <li><b>enabled</b> - 是否启用微信小程序登录，默认 false</li>
     *   <li><b>client-id</b> - 微信小程序的 AppID</li>
     *   <li><b>client-secret</b> - 微信小程序的 AppSecret</li>
     * </ul>
     * 
     * <h3>登录流程</h3>
     * <ol>
     *   <li>小程序端调用 wx.login() 获取 code</li>
     *   <li>将 code 发送到后端</li>
     *   <li>后端使用 code 换取 session_key 和 openid</li>
     *   <li>生成自定义登录态（token）返回给小程序</li>
     * </ol>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>小程序登录不需要配置回调地址</li>
     *   <li>code 只能使用一次，5分钟内有效</li>
     *   <li>session_key 用于解密用户信息，请妥善保管</li>
     *   <li>AppSecret 请妥善保管，不要泄露</li>
     *   <li>服务器域名必须支持 https</li>
     * </ul>
     * 
     * <h3>常见问题</h3>
     * <ul>
     *   <li><b>code 已使用</b>：code 只能使用一次<br>
     *       解决方案：重新调用 wx.login() 获取新的 code</li>
     *   <li><b>appid 不匹配</b>：AppID 配置错误<br>
     *       解决方案：检查配置的 client-id 是否正确</li>
     * </ul>
     * 
     * <h3>配置示例</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         wechat-mini:
     *           enabled: true
     *           client-id: wx1234567890abcdef
     *           client-secret: 1234567890abcdef1234567890abcdef
     * }</pre>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html">微信小程序登录文档</a></li>
     *   <li><a href="https://mp.weixin.qq.com/">微信公众平台</a></li>
     * </ul>
     * 
     * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WechatMini extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Qq extends Base {
        /**
         * 是否需要获取 unionId
         */
        private boolean unionId = false;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Alipay extends Base {
        /**
         * 支付宝公钥
         */
        private String alipayPublicKey;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DingTalk extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WechatEnterprise extends Base {
        /**
         * 企业微信应用 agentId
         */
        private String agentId;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Weibo extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Baidu extends Base {
    }

    /**
     * Google OAuth2 登录配置
     * 
     * <h3>申请流程</h3>
     * <ol>
     *   <li>访问 <a href="https://console.cloud.google.com/">Google Cloud Console</a></li>
     *   <li>创建新项目或选择现有项目</li>
     *   <li>启用 Google+ API：
     *     <ul>
     *       <li>在左侧菜单选择"API和服务" → "库"</li>
     *       <li>搜索"Google+ API"并启用</li>
     *     </ul>
     *   </li>
     *   <li>创建 OAuth 2.0 凭据：
     *     <ul>
     *       <li>在左侧菜单选择"API和服务" → "凭据"</li>
     *       <li>点击"创建凭据" → "OAuth 客户端 ID"</li>
     *       <li>应用类型选择"Web 应用"</li>
     *       <li>添加授权的重定向 URI：http://your-domain/oauth2/callback/google</li>
     *     </ul>
     *   </li>
     *   <li>获取客户端 ID 和客户端密钥</li>
     * </ol>
     * 
     * <h3>所需材料</h3>
     * <ul>
     *   <li>Google 账号</li>
     *   <li>应用名称</li>
     *   <li>应用主页地址（可选）</li>
     *   <li>隐私政策链接（可选）</li>
     * </ul>
     * 
     * <h3>配置参数</h3>
     * <ul>
     *   <li><b>enabled</b> - 是否启用 Google 登录，默认 false</li>
     *   <li><b>client-id</b> - Google 应用的客户端 ID</li>
     *   <li><b>client-secret</b> - Google 应用的客户端密钥</li>
     *   <li><b>redirect-uri</b> - 授权回调地址，必须与 Google 应用配置一致</li>
     * </ul>
     * 
     * <h3>权限范围（Scope）</h3>
     * <ul>
     *   <li><b>openid</b> - OpenID Connect 登录（必选）</li>
     *   <li><b>profile</b> - 访问用户的基本资料信息</li>
     *   <li><b>email</b> - 访问用户的邮箱地址</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>回调地址必须使用 http:// 或 https:// 开头</li>
     *   <li>本地测试可以使用 http://localhost 或 http://127.0.0.1</li>
     *   <li>生产环境建议使用 https:// 协议</li>
     *   <li>需要在 Google Cloud Console 中配置 OAuth 同意屏幕</li>
     * </ul>
     * 
     * <h3>常见问题</h3>
     * <ul>
     *   <li><b>redirect_uri_mismatch</b>：回调地址不匹配<br>
     *       解决方案：检查配置的 redirect-uri 是否与 Google 应用配置一致</li>
     *   <li><b>invalid_client</b>：客户端 ID 或密钥错误<br>
     *       解决方案：检查配置的 client-id 和 client-secret 是否正确</li>
     *   <li><b>access_denied</b>：用户拒绝授权<br>
     *       解决方案：用户需要同意授权才能完成登录</li>
     * </ul>
     * 
     * <h3>配置示例</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         google:
     *           enabled: true
     *           client-id: 123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com
     *           client-secret: GOCSPX-abcdefghijklmnopqrstuvwxyz
     *           redirect-uri: http://127.0.0.1:19180/oauth2/callback/google
     * }</pre>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.google.com/identity/protocols/oauth2">Google OAuth 2.0 文档</a></li>
     *   <li><a href="https://console.cloud.google.com/">Google Cloud Console</a></li>
     * </ul>
     * 
     * @see com.chua.starter.oauth.sso.support.protocol.OAuth2LoginController
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Google extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Microsoft extends Base {
    }

    // ==================== 基础配置类 ====================

    @Data
    public static class Base {

        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 客户端ID (appKey/appId)
         */
        private String clientId;

        /**
         * 客户端密钥 (appSecret)
         */
        private String clientSecret;

        /**
         * 登录成功后的回调地址
         */
        private String redirectUri;

        /**
         * 自定义授权 scope
         */
        private String scope;
    }
}
