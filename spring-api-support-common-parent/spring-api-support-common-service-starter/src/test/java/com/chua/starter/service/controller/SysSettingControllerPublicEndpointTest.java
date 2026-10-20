package com.chua.starter.service.controller;

import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 登录页公开接口的令牌豁免约束。
 *
 * <p>登录页在拿到令牌之前就要读取品牌、三方登录、API 基础路径与文件服务器地址，
 * 因此这些接口必须标注 {@link TokenForIgnore}，否则会被 OAuth 客户端过滤器拦成
 * {@code 403 A0403S0000 用户信息不存在，请重新登录}，表现为「登录页配置全部拉不到」。</p>
 *
 * <p>这里用反射守住这条约定：谁把注解删了，单测直接红。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class SysSettingControllerPublicEndpointTest {

    /**
     * 默认设置列表必须免令牌（登录页渲染依赖）。
     *
     * @throws Exception 反射取方法失败
     */
    @Test
    void defaultSettingsEndpointIgnoresToken() throws Exception {
        Method method = SysSettingController.class.getMethod("listForDefault");
        assertTrue(method.isAnnotationPresent(TokenForIgnore.class),
                "GET /v2/setting/default 必须标注 @TokenForIgnore（登录页未登录时读取配置）");
    }

    /**
     * 三方登录来源列表必须免令牌（登录页按钮依赖）。
     *
     * @throws Exception 反射取方法失败
     */
    @Test
    void ssoProvidersEndpointIgnoresToken() throws Exception {
        Method method = SysSettingController.class.getMethod("listEnabledThirdPartyProviders");
        assertTrue(method.isAnnotationPresent(TokenForIgnore.class),
                "GET /v2/setting/sso/providers 必须标注 @TokenForIgnore（登录页第三方登录按钮）");
    }

    /**
     * 默认嵌入式：不下发端口，下发应用内相对路径（context + path-prefix）。
     */
    @Test
    void embeddedModeReturnsRelativeServerPath() {
        assertEquals("/fs", SysSettingController.resolveFileStorageServer("embedded", "", "/fs", "18080", null));
        assertEquals("/system/api/fs",
                SysSettingController.resolveFileStorageServer("embedded", "/system/api", "/fs", "18080", null));
        assertEquals("/system/api", SysSettingController.resolveFileStorageServer("embedded", "/system/api", "/", "18080", null));
    }

    /**
     * 独立模式：必须下发完整 http://host:port（含端口）。
     */
    @Test
    void standaloneModeReturnsAbsoluteServerWithPort() {
        assertEquals("http://192.168.50.198:18080",
                SysSettingController.resolveFileStorageServer("standalone", "", "/fs", "18080", "192.168.50.198"));
        assertEquals("http://127.0.0.1:18080",
                SysSettingController.resolveFileStorageServer("standalone", "", "/fs", "18080", "0.0.0.0"));
    }

    /**
     * 独立模式缺少端口时不产生地址。
     */
    @Test
    void standaloneModeWithoutPortReturnsEmpty() {
        assertEquals("", SysSettingController.resolveFileStorageServer("standalone", "", "/fs", " ", "192.168.50.198"));
    }
}
