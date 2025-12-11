package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.crypto.digest.HMac;
import com.chua.common.support.datasource.JdbcEngine;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.oauth.client.support.entity.AppKeySecret;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.entity.SysToken;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;

/**
 * AppKey签名认证处理器
 * <p>
 * 处理基于AppID/AppSecret签名的认证请求，使用JdbcEngine查询数据库
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("appkey")
public class AppKeyAuthorizationHandler implements AuthorizationHandler {

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation,
                                            String token,
                                            String cookie,
                                            String userCode) {
        log.info("使用AppKey签名认证方式");

        try {
            // 1. 解析AppKey参数
            AppKeySecret appKeySecret = Json.fromJson(userCode, AppKeySecret.class);
            if (appKeySecret == null) {
                return ReturnResult.error("请求参数格式错误");
            }

            String appKey = appKeySecret.getAppId();
            String xTime = appKeySecret.getXTime();
            String xRandom = appKeySecret.getXRandom();
            String xSign = appKeySecret.getXSign();
            String body = StringUtils.defaultString(appKeySecret.getBody(), "");

            log.info("=== AppKey认证开始 ===");
            log.info("AppKey: {}", appKey);
            log.info("时间戳: {}", xTime);
            log.info("随机数: {}", xRandom);

            // 2. 参数验证
            if (StringUtils.isBlank(appKey) || StringUtils.isBlank(xTime) ||
                    StringUtils.isBlank(xRandom) || StringUtils.isBlank(xSign)) {
                return ReturnResult.error("必需参数缺失");
            }

            // 3. 时间戳验证（防重放攻击，5分钟内有效）
            long timestamp = Long.parseLong(xTime);
            long currentTime = System.currentTimeMillis() / 1000;
            long timeDiff = Math.abs(currentTime - timestamp);
            if (timeDiff > 300) {
                log.warn("时间戳已过期: timestamp={}, current={}, diff={}秒",
                        timestamp, currentTime, timeDiff);
                return ReturnResult.error("时间戳已过期，请求可能被重放");
            }

            // 4. 使用JdbcEngine查询AppKey
            DataSource dataSource = SpringBeanUtils.getBean(DataSource.class);
            JdbcEngine jdbcEngine = JdbcEngine.of(dataSource);

            String sql = """
                    SELECT sys_token_app_key, sys_token_app_secret, sys_token_user_id, 
                           sys_token_status, sys_token_expire_time
                    FROM sys_token
                    WHERE sys_token_app_key = ?
                    """;

            List<SysToken> tokens = jdbcEngine.queryForList(sql, SysToken.class, appKey);
            if (tokens.isEmpty()) {
                log.warn("AppKey不存在: appKey={}", appKey);
                return ReturnResult.error("AppKey不存在");
            }

            SysToken sysToken = tokens.get(0);

            // 5. 状态检查
            if (sysToken.getSysTokenStatus() == null || sysToken.getSysTokenStatus() != 1) {
                log.warn("AppKey已禁用: appKey={}", appKey);
                return ReturnResult.error("AppKey已禁用");
            }

            // 6. 过期时间检查
            if (sysToken.getSysTokenExpireTime() != null &&
                    sysToken.getSysTokenExpireTime().isBefore(java.time.LocalDateTime.now())) {
                log.warn("AppKey已过期: appKey={}", appKey);
                return ReturnResult.error("AppKey已过期");
            }

            // 7. 签名验证
            String signContent = appKey + xTime + xRandom + body;
            String expectedSign = HMac.sha256(signContent, sysToken.getSysTokenAppSecret());

            log.debug("签名内容: {}", signContent);
            log.debug("期望签名: {}", expectedSign);
            log.debug("实际签名: {}", xSign);

            if (!expectedSign.equals(xSign)) {
                log.warn("签名验证失败: appKey={}", appKey);
                return ReturnResult.error("签名验证失败");
            }

            // 8. 构建用户信息
            UserResume userResume = new UserResume();
            userResume.setUserId(sysToken.getSysTokenUserId());
            userResume.setUsername(sysToken.getSysTokenAppName());
            userResume.setAppId(appKey);

            // 9. 生成Token
            TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class)
                    .getNewExtension("redis");
            String newToken = tokenResolver.createToken(userResume);

            log.info("AppKey认证成功: appKey={}, userId={}", appKey, sysToken.getSysTokenUserId());
            return ReturnResult.ok(newToken);

        } catch (NumberFormatException e) {
            log.error("时间戳格式错误", e);
            return ReturnResult.error("时间戳格式错误");
        } catch (Exception e) {
            log.error("AppKey认证失败", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // AppKey认证：有userCode参数，或者subProtocol为appkey
        return StringUtils.isNotBlank(userCode) || "appkey".equalsIgnoreCase(subProtocol);
    }
}
