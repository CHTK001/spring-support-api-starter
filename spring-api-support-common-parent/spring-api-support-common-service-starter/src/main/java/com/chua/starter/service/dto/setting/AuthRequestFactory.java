package com.chua.starter.service.dto.setting;

import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.*;

/**
 * just认证 认证请求 工厂
 *
 * <p>
 * 根据平台标识创建对应的 just认证 {@link AuthRequest} 实例，
 * 集中管理各平台 {@code Auth*Request} 的构造逻辑，避免多处重复。
 * </p>
 *
 * <h3>支持的平台及对应的 AuthRequest</h3>
 * <ul>
 *   <li>gitee → AuthGiteeRequest</li>
 *   <li>github → AuthGithubRequest</li>
 *   <li>wechat/wechat_open → AuthWeChatOpenRequest（微信开放平台）</li>
 *   <li>wechat_mp → AuthWeChatMpRequest（微信公众号）</li>
 *   <li>wechat_enterprise → AuthWeChatEnterpriseQrcodeRequest（企业微信）</li>
 *   <li>qq → AuthQqRequest</li>
 *   <li>alipay → AuthAlipayRequest</li>
 *   <li>dingtalk → AuthDingTalkRequest</li>
 *   <li>weibo → AuthWeiboRequest</li>
 *   <li>baidu → AuthBaiduRequest</li>
 *   <li>google → AuthGoogleRequest</li>
 *   <li>microsoft → AuthMicrosoftRequest</li>
 *   <li>taobao → AuthTaobaoRequest</li>
 *   <li>douyin → AuthDouyinRequest</li>
 *   <li>feishu → AuthFeishuRequest</li>
 * </ul>
 *
 * @author CH
 * @since 2024/12/18
 */
public final class AuthRequestFactory {

    /**
     * 认证请求工厂。
     */
    private AuthRequestFactory() {
    }

    /**
     * 创建指定平台的 认证请求 实例
     *
     * @param platform 平台标识（不区分大小写）
     * @param authConfig 认证配置（包含 客户端id、客户端secret、redirecturi）
     * @param authStateCache 认证状态缓存
     * @return 对应平台的 认证请求 实例，若平台不支持则返回 空
     */
    public static AuthRequest createAuthRequest(String platform, AuthConfig authConfig, AuthStateCache authStateCache) {
        return switch (platform.toLowerCase()) {
            case "gitee" -> new AuthGiteeRequest(authConfig, authStateCache);
            case "github" -> new AuthGithubRequest(authConfig, authStateCache);
            case "wechat" -> new AuthWeChatOpenRequest(authConfig, authStateCache);
            case "wechat_mp" -> new AuthWeChatMpRequest(authConfig, authStateCache);
            case "wechat_enterprise" -> new AuthWeChatEnterpriseQrcodeRequest(authConfig, authStateCache);
            case "qq" -> new AuthQqRequest(authConfig, authStateCache);
            case "alipay" -> new AuthAlipayRequest(authConfig, authStateCache);
            case "dingtalk" -> new AuthDingTalkRequest(authConfig, authStateCache);
            case "weibo" -> new AuthWeiboRequest(authConfig, authStateCache);
            case "baidu" -> new AuthBaiduRequest(authConfig, authStateCache);
            case "google" -> new AuthGoogleRequest(authConfig, authStateCache);
            case "microsoft" -> new AuthMicrosoftRequest(authConfig, authStateCache);
            case "taobao" -> new AuthTaobaoRequest(authConfig, authStateCache);
            case "douyin" -> new AuthDouyinRequest(authConfig, authStateCache);
            case "feishu" -> new AuthFeishuRequest(authConfig, authStateCache);
            default -> null;
        };
    }

}
