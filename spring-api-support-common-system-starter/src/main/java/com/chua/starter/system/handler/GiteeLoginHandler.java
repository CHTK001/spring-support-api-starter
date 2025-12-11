package com.chua.starter.system.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
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
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGiteeRequest;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * Gitee 登录处理器
 * <p>
 * Gitee OAuth2.0 登录流程：
 * 1. 前端获取 Gitee 授权后的 code
 * 2. 后端通过 code 换取 access_token 和用户信息
 * 3. 使用 Gitee uuid 查找关联的系统用户
 * 4. 进行登录认证并返回 token
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("GITEE")
public class GiteeLoginHandler implements LoginHandler {

    /**
     * 第三方类型标识
     */
    private static final String THIRD_PARTY_TYPE = "gitee";

    @Override
    public ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData,
                                           HttpServletRequest request, HttpServletResponse response,
                                           String fingerprint) {
        log.info("Gitee登录开始");

        try {
            // 1. 获取登录参数
            // username: Gitee OAuth code
            // password: 可选，用于传递 state
            String code = loginData.getUsername();
            String state = loginData.getPassword();

            if (StringUtils.isBlank(code)) {
                log.warn("Gitee登录缺少 code 参数");
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "缺少Gitee授权码");
            }

            // 2. 获取 Gitee 配置
            String clientId = loginData.getWxEncryptedData();
            String clientSecret = loginData.getWxIv();
            String redirectUri = loginData.getWxSessionKey();

            if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
                log.error("Gitee配置不完整，请检查 clientId 和 clientSecret");
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "Gitee配置不完整");
            }

            // 3. 构建 JustAuth 请求
            AuthConfig authConfig = AuthConfig.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .build();

            AuthGiteeRequest authGiteeRequest = new AuthGiteeRequest(authConfig);

            // 4. 通过 code 获取用户信息
            AuthCallback authCallback = AuthCallback.builder()
                    .code(code)
                    .state(state)
                    .build();

            AuthResponse<AuthUser> authResponse = authGiteeRequest.login(authCallback);
            if (!authResponse.ok()) {
                log.warn("Gitee授权失败: {}", authResponse.getMsg());
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "Gitee授权失败: " + authResponse.getMsg());
            }

            AuthUser authUser = authResponse.getData();
            String giteeUuid = authUser.getUuid();
            log.debug("Gitee登录获取到用户: uuid={}, username={}", giteeUuid, authUser.getUsername());

            // 5. 查找关联的系统用户
            SysUserThirdPartyMapper mapper = SpringBeanUtils.getBean(SysUserThirdPartyMapper.class);
            if (mapper == null) {
                log.error("SysUserThirdPartyMapper 未找到");
                return ReturnResult.failure(REQUEST_PARAM_ERROR, "系统配置错误");
            }

            SysUserThirdParty thirdParty = mapper.selectOne(
                    Wrappers.<SysUserThirdParty>lambdaQuery()
                            .eq(SysUserThirdParty::getSysUserThirdPartyType, THIRD_PARTY_TYPE)
                            .eq(SysUserThirdParty::getSysUserThirdPartyUid, giteeUuid)
            );

            if (thirdParty == null || thirdParty.getSysUserId() == null) {
                log.warn("Gitee登录失败: 未找到关联用户 uuid={}", giteeUuid);
                return ReturnResult.failure(USERNAME_OR_PASSWORD_ERROR, "账号未绑定，请先绑定Gitee账号");
            }

            // 6. 使用用户ID进行登录认证
            String address = getIpAddress(request);
            AuthClientExecute clientExecute = AuthClientExecute.getInstance();
            long startTime = System.currentTimeMillis();

            // 获取租户ID
            String tenantId = loginData.getTenantId();

            LoginAuthResult accessToken = clientExecute.getAccessToken(
                    String.valueOf(thirdParty.getSysUserId()),
                    null,
                    AuthType.GITEE,
                    ImmutableBuilder.<String, Object>builderOfMap()
                            .put("address", address)
                            .put("fingerprint", fingerprint)
                            .put("ua", request.getHeader("User-Agent"))
                            .put("giteeUuid", giteeUuid)
                            .put("giteeUsername", authUser.getUsername())
                            .put("thirdPartyType", THIRD_PARTY_TYPE)
                            .put("tenantId", tenantId)
                            .build()
            );

            log.info("Gitee登录认证耗时: {}ms", System.currentTimeMillis() - startTime);

            if (accessToken == null) {
                return ReturnResult.failure(USERNAME_OR_PASSWORD_ERROR, "Gitee登录失败，用户不存在");
            }

            if (accessToken.getCode() != 200) {
                return ReturnResult.failure(REQUEST_PARAM_ERROR, accessToken.getMessage());
            }

            // 7. 更新第三方用户信息
            updateThirdPartyInfo(mapper, thirdParty, authUser);

            // 8. 构建登录结果
            LoginResult loginResult = LoginResult.builder()
                    .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                    .tokenType("Bearer")
                    .accessToken(accessToken.getToken())
                    .refreshToken(accessToken.getRefreshToken())
                    .build();

            CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
            log.info("Gitee登录成功，用户ID: {}", thirdParty.getSysUserId());
            return ReturnResult.success(loginResult);

        } catch (Exception e) {
            log.error("Gitee登录异常: {}", e.getMessage(), e);
            return ReturnResult.failure(REQUEST_PARAM_ERROR, "Gitee登录异常，请稍后重试");
        }
    }

    /**
     * 更新第三方用户信息
     *
     * @param mapper     Mapper
     * @param thirdParty 第三方绑定记录
     * @param authUser   Gitee用户信息
     */
    private void updateThirdPartyInfo(SysUserThirdPartyMapper mapper, SysUserThirdParty thirdParty, AuthUser authUser) {
        try {
            // 更新用户名和头像等信息
            boolean needUpdate = false;

            if (!StringUtils.equals(thirdParty.getSysUserThirdPartyName(), authUser.getUsername())) {
                thirdParty.setSysUserThirdPartyName(authUser.getUsername());
                needUpdate = true;
            }

            if (needUpdate) {
                mapper.updateById(thirdParty);
                log.debug("更新Gitee第三方信息: uuid={}", authUser.getUuid());
            }
        } catch (Exception e) {
            log.warn("更新第三方用户信息失败: {}", e.getMessage());
        }
    }
}
