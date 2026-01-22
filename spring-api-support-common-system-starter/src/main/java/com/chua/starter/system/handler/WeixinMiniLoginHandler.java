package com.chua.starter.system.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.base.collection.ImmutableBuilder;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.spring.support.configuration.SpringBeanUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * 微信小程序登录处理器
 * <p>
 * 微信小程序登录流程：
 * 1. 前端调用 wx.login() 获取 code
 * 2. 前端调用 wx.getPhoneNumber() 获取手机号加密数据（或直接传 phoneCode）
 * 3. 后端通过 code 换取 session_key 和 openid
 * 4. 后端通过 phoneCode 获取手机号
 * 5. 使用手机号作为用户名进行登录认证
 * 6. 更新或创建 SysUserThirdParty 记录，保存 openid
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("WECHAT")
public class WeixinMiniLoginHandler implements LoginHandler {

    /**
     * 第三方类型标识
     */
    private static final String THIRD_PARTY_TYPE = "wx_mini_app";

    @Override
    public ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData,
                                           HttpServletRequest request, HttpServletResponse response,
                                           String fingerprint) {
        log.info("微信小程序登录开始，用户名: {}", loginData.getUsername());

        // 获取微信小程序处理器
        Object tencentMaHandler = getTencentMaHandler();
        if (tencentMaHandler == null) {
            log.error("微信小程序服务未配置，请检查 plugin.tencent.mini-app 配置");
            return ReturnResult.failure(REQUEST_PARAM_ERROR, "微信小程序服务未配置");
        }

        try {
            // 1. 从登录数据中获取微信 code 和 phoneCode
            // 微信小程序登录：username 传递 wx.login 的 code
            // password 传递 wx.getPhoneNumber 的 code（phoneCode）
            String wxCode = loginData.getUsername();
            String phoneCode = loginData.getPassword();

            if (StringUtils.isBlank(wxCode)) {
                log.warn("微信登录缺少 code 参数");
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "缺少微信授权码");
            }

            // 2. 通过 code 换取 session_key 和 openid（使用反射调用）
            Object sessionInfo = invokeMethod(tencentMaHandler, "getSessionInfo", wxCode);
            if (sessionInfo == null) {
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "微信授权码无效");
            }
            String openid = getPropertyValue(sessionInfo, "openid");
            String sessionKey = getPropertyValue(sessionInfo, "sessionKey");
            log.debug("微信登录获取到 openid: {}, sessionKey: {}", openid, sessionKey);

            // 3. 获取手机号
            String phoneNumber = getPhoneNumber(tencentMaHandler, phoneCode, loginData);
            if (StringUtils.isBlank(phoneNumber)) {
                log.warn("微信登录无法获取手机号");
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "无法获取手机号，请授权后重试");
            }
            log.info("微信登录获取到手机号: {}", phoneNumber);

            // 4. 使用手机号作为用户名进行登录认证
            String address = getIpAddress(request);
            AuthClientExecute clientExecute = AuthClientExecute.getInstance();
            long startTime = System.currentTimeMillis();

            // 获取租户ID
            String tenantId = loginData.getTenantId();

            LoginAuthResult accessToken = clientExecute.getAccessToken(
                    phoneNumber,
                    openid,
                    AuthType.WECHAT,
                    ImmutableBuilder.<String, Object>builderOfMap()
                            .put("address", address)
                            .put("fingerprint", fingerprint)
                            .put("ua", request.getHeader("User-Agent"))
                            .put("openid", openid)
                            .put("sessionKey", sessionKey)
                            .put("thirdPartyType", THIRD_PARTY_TYPE)
                            .put("tenantId", tenantId)
                            .build()
            );

            log.info("微信登录认证耗时: {}ms", System.currentTimeMillis() - startTime);

            if (accessToken == null) {
                return ReturnResult.failure(USERNAME_OR_PASSWORD_ERROR, "微信登录失败，用户不存在");
            }

            if (accessToken.getCode() != 200) {
                return ReturnResult.failure(REQUEST_PARAM_ERROR, accessToken.getMessage());
            }

            // 5. 更新或创建第三方绑定信息
            updateThirdPartyBinding(sysLoginService, phoneNumber, openid);

            // 6. 构建登录结果
            LoginResult loginResult = LoginResult.builder()
                    .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                    .tokenType("Bearer")
                    .accessToken(accessToken.getToken())
                    .refreshToken(accessToken.getRefreshToken())
                    .build();

            CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
            log.info("微信小程序登录成功，手机号: {}", phoneNumber);
            return ReturnResult.success(loginResult);

        } catch (Exception e) {
            log.error("微信小程序登录异常: {}", e.getMessage(), e);
            return ReturnResult.failure(REQUEST_PARAM_ERROR, "微信登录异常，请稍后重试");
        }
    }

    /**
     * 获取手机号
     *
     * @param tencentMaHandler 微信小程序处理器
     * @param phoneCode        手机号授权码
     * @param loginData        登录数据
     * @return 手机号
     */
    private String getPhoneNumber(Object tencentMaHandler, String phoneCode, LoginData loginData) {
        // 优先使用 phoneCode 获取手机号（新版微信接口）
        if (StringUtils.isNotBlank(phoneCode)) {
            try {
                Object phoneNumberInfo = invokeMethod(tencentMaHandler, "getPhoneNumber", phoneCode);
                if (phoneNumberInfo != null) {
                    String purePhoneNumber = getPropertyValue(phoneNumberInfo, "purePhoneNumber");
                    if (StringUtils.isNotBlank(purePhoneNumber)) {
                        return purePhoneNumber;
                    }
                    String phoneNumber = getPropertyValue(phoneNumberInfo, "phoneNumber");
                    if (StringUtils.isNotBlank(phoneNumber)) {
                        return phoneNumber;
                    }
                }
            } catch (Exception e) {
                log.warn("通过 phoneCode 获取手机号失败: {}", e.getMessage());
            }
        }

        // 兼容：直接传入的手机号（用于测试或已授权场景）
        if (StringUtils.isNotBlank(loginData.getUsername())) {
            return loginData.getUsername();
        }

        return null;
    }

    /**
     * 反射调用方法
     *
     * @param target     目标对象
     * @param methodName 方法名
     * @param args       参数
     * @return 方法返回值
     */
    private Object invokeMethod(Object target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
            }
            Method method = target.getClass().getMethod(methodName, paramTypes);
            return method.invoke(target, args);
        } catch (Exception e) {
            log.error("反射调用方法 {} 失败: {}", methodName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 反射获取对象属性值
     *
     * @param target       目标对象
     * @param propertyName 属性名
     * @return 属性值
     */
    private String getPropertyValue(Object target, String propertyName) {
        if (target == null) {
            return null;
        }
        try {
            // 尝试调用 getter 方法
            String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            Method method = target.getClass().getMethod(getterName);
            Object result = method.invoke(target);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            log.debug("获取属性 {} 失败: {}", propertyName, e.getMessage());
            return null;
        }
    }

    /**
     * 更新或创建第三方绑定信息
     *
     * @param sysLoginService 登录服务
     * @param phoneNumber     手机号
     * @param openid          微信 openid
     */
    private void updateThirdPartyBinding(SysLoginServiceImpl sysLoginService, String phoneNumber, String openid) {
        try {
            SysUserThirdPartyMapper mapper = SpringBeanUtils.getBean(SysUserThirdPartyMapper.class);
            if (mapper == null) {
                log.warn("SysUserThirdPartyMapper 未找到，跳过第三方绑定更新");
                return;
            }

            // 查找是否已存在绑定记录（通过 openid）
            SysUserThirdParty existingByOpenid = mapper.selectOne(
                    Wrappers.<SysUserThirdParty>lambdaQuery()
                            .eq(SysUserThirdParty::getSysUserThirdPartyType, THIRD_PARTY_TYPE)
                            .eq(SysUserThirdParty::getSysUserThirdPartyUid, openid)
            );

            if (existingByOpenid != null) {
                log.debug("微信 openid 已绑定，无需更新");
                return;
            }

            // 查找是否已存在该类型的绑定记录（通过手机号关联的用户）
            // 这里需要通过手机号找到用户ID，然后更新或创建绑定记录
            SysUserThirdParty existingByPhone = mapper.selectOne(
                    Wrappers.<SysUserThirdParty>lambdaQuery()
                            .eq(SysUserThirdParty::getSysUserThirdPartyType, THIRD_PARTY_TYPE)
                            .eq(SysUserThirdParty::getSysUserThirdPartyValue, phoneNumber)
            );

            if (existingByPhone != null) {
                // 更新现有记录的 openid
                existingByPhone.setSysUserThirdPartyUid(openid);
                mapper.updateById(existingByPhone);
                log.info("更新微信第三方绑定，手机号: {}, openid: {}", phoneNumber, openid);
            } else {
                // 创建新的绑定记录
                SysUserThirdParty newBinding = new SysUserThirdParty();
                newBinding.setSysUserThirdPartyType(THIRD_PARTY_TYPE);
                newBinding.setSysUserThirdPartyName("微信小程序");
                newBinding.setSysUserThirdPartyValue(phoneNumber);
                newBinding.setSysUserThirdPartyUid(openid);
                mapper.insert(newBinding);
                log.info("创建微信第三方绑定，手机号: {}, openid: {}", phoneNumber, openid);
            }
        } catch (Exception e) {
            log.error("更新第三方绑定信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取微信小程序处理器
     *
     * @return TencentMaHandler 实例，未配置时返回 null
     */
    private Object getTencentMaHandler() {
        try {
            // 通过类名获取 Bean，避免编译时依赖
            Class<?> clazz = Class.forName("com.chua.tencent.support.miniapp.TencentMaHandler");
            return SpringBeanUtils.getBean(clazz);
        } catch (ClassNotFoundException e) {
            log.debug("TencentMaHandler 类未找到，微信小程序功能不可用");
            return null;
        } catch (Exception e) {
            log.debug("TencentMaHandler 未配置: {}", e.getMessage());
            return null;
        }
    }
}
