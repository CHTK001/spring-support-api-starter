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

/**
   * 应用密钥 服务 实现
 *
 * @since 2025/10/23
 * @author CH
 */
@Slf4j
@Service
public class SysAppKeyServiceImpl extends ServiceImpl<SysAppKeyMapper, SysAppKey> implements SysAppKeyService {

    /**
      * app键启用状态（1=启用，0=禁用）
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * 毫秒转秒的除数
     */
    private static final long MILLIS_PER_SECOND = 1000L;

    /**
     * 一天的秒数
     */
    private static final long SECONDS_PER_DAY = 24L * 60L * 60L;

    /**
     * 默认有效天数
     */
    private static final int DEFAULT_VALID_DAYS = 30;

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
    /**
     * 获取byapp键
     *
     * @param appKey app键
     * @return 获取byapp键的结果
     */
    public SysAppKey getByAppKey(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            return null;
        }
        return baseMapper.selectByAppKey(appKey);
    }

    @Override
    /** 验证 */
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
        if (isExpired(dbAppKey)) {
            log.warn("AppKey已过期: appKey={}, expireTime={}, validDays={}",
                    maskAppKey(appKey),
                    resolveExpireTime(dbAppKey),
                    dbAppKey.getSysAppKeyValidDays());
            return false;
        }

 // 验证密钥（app键和secret应该相同，或者secret是app键对应的密钥）
        boolean valid = appKey.equals(dbAppKey.getSysAppKeySecret()) || secret.equals(dbAppKey.getSysAppKeySecret());
        if (!valid) {
            log.warn("AppKey密钥验证失败: appKey={}", maskAppKey(appKey));
        }

        return valid;
    }

    @Override
    /** 验证 */
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
        if (isExpired(dbAppKey)) {
            log.warn("AppKey已过期: appKey={}, expireTime={}, validDays={}",
                    maskAppKey(appKey),
                    resolveExpireTime(dbAppKey),
                    dbAppKey.getSysAppKeyValidDays());
            return false;
        }

 // 验证密钥（app键和secret应该相同，或者secret是app键对应的密钥）
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
    /**
    * 更新use信息byapp键
    *
    * @param appKey app键
     */
    public void updateUseInfoByAppKey(String appKey) {
        try {
            long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
            baseMapper.updateUseInfoByAppKey(appKey, currentTime);
            log.debug("AppKey使用信息已异步更新: appKey={}", maskAppKey(appKey));
        } catch (Exception e) {
            log.error("更新AppKey使用信息失败: appKey={}", maskAppKey(appKey), e);
        }
    }

    @Override
    /** 是否Expired */
    public boolean isExpired(SysAppKey appKey) {
        Long expireTime = resolveExpireTime(appKey);
        if (expireTime == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis() / MILLIS_PER_SECOND;
        return currentTime > expireTime;
    }

    @Override
    /** 解析expire时间 */
    public Long resolveExpireTime(SysAppKey appKey) {
        if (appKey == null) {
            return null;
        }

        Integer validDays = appKey.getSysAppKeyValidDays();
        if (Integer.valueOf(0).equals(validDays)) {
            return null;
        }

        if (appKey.getSysAppKeyExpireTime() != null) {
            return appKey.getSysAppKeyExpireTime();
        }

        if (appKey.getCreateTime() == null) {
            return null;
        }

        int effectiveValidDays = validDays == null ? DEFAULT_VALID_DAYS : validDays;
        if (effectiveValidDays <= 0) {
            return null;
        }

        return (appKey.getCreateTime().getTime() / MILLIS_PER_SECOND) + effectiveValidDays * SECONDS_PER_DAY;
    }

    /**
    * 脱敏app键
    * @param appKey app键
    * @return maskapp键的结果
     */
    private String maskAppKey(String appKey) {
        if (appKey == null || appKey.length() < 8) {
            return "***";
        }
        return appKey.substring(0, 4) + "..." + appKey.substring(appKey.length() - 4);
    }

}
