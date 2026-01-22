package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.core.utils.StringUtils;
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
    @Cacheable(value = "appKey:secret", key = "#appKey", unless = "#result == null")
    public SysAppKey getByAppKey(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            return null;
        }
        return baseMapper.selectByAppKey(appKey);
    }

    @Override
    public boolean verify(String appKey, String secret) {
        SysAppKey dbAppKey = getByAppKey(appKey);
        if (dbAppKey == null) {
            log.warn("AppKey不存在: appKey={}", maskAppKey(appKey));
            return false;
        }

        // 检查状态（使用Integer.equals避免NPE）
        if (!Integer.valueOf(STATUS_ENABLED).equals(dbAppKey.getSysAppKeyStatus())) {
            log.warn("AppKey已禁用: appKey={}", maskAppKey(appKey));
            return false;
        }

        // 检查过期时间
        if (dbAppKey.getSysAppKeyExpireTime() != null) {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            if (currentTime > dbAppKey.getSysAppKeyExpireTime()) {
                log.warn("AppKey已过期: appKey={}, expireTime={}", maskAppKey(appKey), dbAppKey.getSysAppKeyExpireTime());
                return false;
            }
        }

        // 验证密钥（appKey和secret应该相同，或者secret是appKey对应的密钥）
        boolean valid = appKey.equals(dbAppKey.getSysAppKeySecret()) || secret.equals(dbAppKey.getSysAppKeySecret());
        if (!valid) {
            log.warn("AppKey密钥验证失败: appKey={}", maskAppKey(appKey));
        }

        return valid;
    }

    @Override
    public boolean verify(String appKey, String secret, String ip) {
        SysAppKey dbAppKey = getByAppKey(appKey);
        if (dbAppKey == null) {
            log.warn("AppKey不存在: appKey={}", maskAppKey(appKey));
            return false;
        }

        // 检查状态（使用Integer.equals避免NPE）
        if (!Integer.valueOf(STATUS_ENABLED).equals(dbAppKey.getSysAppKeyStatus())) {
            log.warn("AppKey已禁用: appKey={}", maskAppKey(appKey));
            return false;
        }

        // 检查过期时间
        if (dbAppKey.getSysAppKeyExpireTime() != null) {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            if (currentTime > dbAppKey.getSysAppKeyExpireTime()) {
                log.warn("AppKey已过期: appKey={}, expireTime={}", maskAppKey(appKey), dbAppKey.getSysAppKeyExpireTime());
                return false;
            }
        }

        // 验证密钥（appKey和secret应该相同，或者secret是appKey对应的密钥）
        if (!appKey.equals(dbAppKey.getSysAppKeySecret()) && !secret.equals(dbAppKey.getSysAppKeySecret())) {
            log.warn("AppKey密钥验证失败: appKey={}", maskAppKey(appKey));
            return false;
        }

        // 检查IP白名单
        String whitelist = dbAppKey.getSysAppKeyIpWhitelist();
        if (StringUtils.isNotBlank(whitelist)) {
            String[] allowedIps = whitelist.split(IP_WHITELIST_SEPARATOR);
            boolean ipAllowed = Arrays.stream(allowedIps)
                    .map(String::trim)
                    .anyMatch(allowedIp -> allowedIp.equals(ip) || IP_WILDCARD.equals(allowedIp));
            
            if (!ipAllowed) {
                log.warn("IP不在白名单中: appKey={}, ip={}, whitelist={}", maskAppKey(appKey), ip, whitelist);
                return false;
            }
        }

        return true;
    }

    @Override
    @Async("taskExecutor")
    public void updateUseInfoByAppKey(String appKey) {
        try {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            baseMapper.updateUseInfoByAppKey(appKey, currentTime);
            log.debug("AppKey使用信息已异步更新: appKey={}", maskAppKey(appKey));
        } catch (Exception e) {
            log.error("更新AppKey使用信息失败: appKey={}", maskAppKey(appKey), e);
        }
    }

    /**
     * 脱敏AppKey
     */
    private String maskAppKey(String appKey) {
        if (appKey == null || appKey.length() < 8) {
            return "***";
        }
        return appKey.substring(0, 4) + "..." + appKey.substring(appKey.length() - 4);
    }

}
