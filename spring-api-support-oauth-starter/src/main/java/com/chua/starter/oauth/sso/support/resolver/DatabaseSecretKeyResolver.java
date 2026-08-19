package com.chua.starter.oauth.sso.support.resolver;

import com.chua.common.support.spi.annotations.Extension;
import com.chua.common.support.spi.annotations.SpiDefault;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.oauth.sso.support.token.TokenBlacklistService;
import com.chua.starter.system.service.SysAppKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;

/**
 * 基于数据库的 SecretKey 解析器
 * <p>
 * 从 sys_app_key 表查询 SK，带缓存
 * </p>
 *
 * @version 1.0.0
 * @since 2024/12/17
 * @author CH
 */
@Slf4j
@Extension("database")
@SpiDefault
public class DatabaseSecretKeyResolver implements SecretKeyResolver {

    /** Status_enabled */
    private static final int STATUS_ENABLED = 1;

    @AutoInject
    /** SYSAPP密钥服务 */
    private SysAppKeyService sysAppKeyService;

    @AutoInject
    /** 令牌blacklist服务 */
    private TokenBlacklistService tokenBlacklistService;

    @Override
    @Cacheable(value = "aksk:secret", key = "#accessKey", unless = "#result == null")
    public String getSecretKey(String accessKey) {
        if (accessKey == null || accessKey.isEmpty()) {
            return null;
        }
        
        try {
            // 先尝试通过 AppKey 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(accessKey);
            if (appKey == null) {
                log.warn("AK不存在: ak={}", maskKey(accessKey));
                return null;
            }
            
            // 检查状态
            if (!Integer.valueOf(STATUS_ENABLED).equals(appKey.getSysAppKeyStatus())) {
                log.warn("AK已禁用: ak={}", maskKey(accessKey));
                return null;
            }
            
            // 检查过期时间
            if (sysAppKeyService.isExpired(appKey)) {
                log.warn("AK已过期: ak={}, expireTime={}, validDays={}",
                        maskKey(accessKey),
                        sysAppKeyService.resolveExpireTime(appKey),
                        appKey.getSysAppKeyValidDays());
                return null;
            }

            String userId = String.valueOf(appKey.getSysUserId());
            if (tokenBlacklistService == null || tokenBlacklistService.isUserBlacklisted(userId)) {
                log.warn("AK关联用户在黑名单中或黑名单服务不可用: ak={}, userId={}",
                        maskKey(accessKey), userId);
                return null;
            }
            
            return appKey.getSysAppKeySecret();
        } catch (Exception e) {
            log.error("查询SK失败: ak={}", maskKey(accessKey), e);
            return null;
        }
    }
    
    /**
     * 脱敏密钥
     */
    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}
