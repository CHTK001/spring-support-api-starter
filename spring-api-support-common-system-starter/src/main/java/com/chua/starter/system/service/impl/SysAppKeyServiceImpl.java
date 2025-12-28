package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.mapper.SysAppKeyMapper;
import com.chua.starter.system.service.SysAppKeyService;
import com.chua.starter.entity.system.SysAppKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * 应用密钥 Service 实现
 *
 * @author CH
 * @since 2025/10/23
 */
@Slf4j
@Service
public class SysAppKeyServiceImpl extends ServiceImpl<SysAppKeyMapper, SysAppKey> implements SysAppKeyService {

    /**
     * AppKey启用状态
     */
    private static final int STATUS_ENABLED = 0;

    /**
     * 毫秒转秒的除数
     */
    private static final long MILLIS_PER_SECOND = 1000L;

    /**
     * IP白名单分隔符
     */
    private static final String IP_WHITELIST_SEPARATOR = ",";

    /**
     * IP白名单通配符
     */
    private static final String IP_WILDCARD = "*";

    @Override
    @Cacheable(value = "appKey:info", key = "#appId", unless = "#result == null")
    public SysAppKey getByAppId(String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        return baseMapper.selectByAppId(appId);
    }

    @Override
    @Cacheable(value = "appKey:secret", key = "#appKey", unless = "#result == null")
    public SysAppKey getByAppKey(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            return null;
        }
        return baseMapper.selectByAppKey(appKey);
    }

    @Override
    public boolean verify(String appId, String secret) {
        SysAppKey appKey = getByAppId(appId);
        if (appKey == null) {
            log.warn("AppKey不存在: appId={}", appId);
            return false;
        }

        // 检查状态（使用Integer.equals避免NPE）
        if (!Integer.valueOf(STATUS_ENABLED).equals(appKey.getSysAppKeyStatus())) {
            log.warn("AppKey已禁用: appId={}", appId);
            return false;
        }

        // 检查过期时间
        if (appKey.getSysAppKeyExpireTime() != null) {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            if (currentTime > appKey.getSysAppKeyExpireTime()) {
                log.warn("AppKey已过期: appId={}, expireTime={}", appId, appKey.getSysAppKeyExpireTime());
                return false;
            }
        }

        // 验证密钥
        boolean valid = secret.equals(appKey.getSysAppKeySecret());
        if (!valid) {
            log.warn("AppKey密钥验证失败: appId={}", appId);
        }

        return valid;
    }

    @Override
    public boolean verify(String appId, String secret, String ip) {
        // 优化：只查询一次，避免重复调用getByAppId
        SysAppKey appKey = getByAppId(appId);
        if (appKey == null) {
            log.warn("AppKey不存在: appId={}", appId);
            return false;
        }

        // 检查状态（使用Integer.equals避免NPE）
        if (!Integer.valueOf(STATUS_ENABLED).equals(appKey.getSysAppKeyStatus())) {
            log.warn("AppKey已禁用: appId={}", appId);
            return false;
        }

        // 检查过期时间
        if (appKey.getSysAppKeyExpireTime() != null) {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            if (currentTime > appKey.getSysAppKeyExpireTime()) {
                log.warn("AppKey已过期: appId={}, expireTime={}", appId, appKey.getSysAppKeyExpireTime());
                return false;
            }
        }

        // 验证密钥
        if (!secret.equals(appKey.getSysAppKeySecret())) {
            log.warn("AppKey密钥验证失败: appId={}", appId);
            return false;
        }

        // 检查IP白名单
        String whitelist = appKey.getSysAppKeyIpWhitelist();
        if (StringUtils.isNotBlank(whitelist)) {
            String[] allowedIps = whitelist.split(IP_WHITELIST_SEPARATOR);
            boolean ipAllowed = Arrays.stream(allowedIps)
                    .map(String::trim)
                    .anyMatch(allowedIp -> allowedIp.equals(ip) || IP_WILDCARD.equals(allowedIp));
            
            if (!ipAllowed) {
                log.warn("IP不在白名单中: appId={}, ip={}, whitelist={}", appId, ip, whitelist);
                return false;
            }
        }

        return true;
    }

    @Override
    @Async("taskExecutor")
    public void updateUseInfo(String appId) {
        try {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            baseMapper.updateUseInfo(appId, currentTime);
            log.debug("AppKey使用信息已异步更新: appId={}", appId);
        } catch (Exception e) {
            log.error("更新AppKey使用信息失败: appId={}", appId, e);
        }
    }

    /**
     * 异步更新使用信息（返回Future）
     * <p>用于需要确认更新结果的场景</p>
     *
     * @param appId 应用ID
     * @return 异步结果
     */
    @Async("taskExecutor")
    public CompletableFuture<Boolean> updateUseInfoAsync(String appId) {
        try {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            baseMapper.updateUseInfo(appId, currentTime);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("异步更新AppKey使用信息失败: appId={}", appId, e);
            return CompletableFuture.completedFuture(false);
        }
    }
}
