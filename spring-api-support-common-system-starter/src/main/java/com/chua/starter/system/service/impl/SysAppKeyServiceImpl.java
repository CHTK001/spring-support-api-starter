package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.mapper.SysAppKeyMapper;
import com.chua.starter.system.service.SysAppKeyService;
import com.chua.starter.entity.system.SysAppKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 应用密钥 Service 实现
 *
 * @author CH
 * @since 2025/10/23
 */
@Slf4j
@Service
public class SysAppKeyServiceImpl extends ServiceImpl<SysAppKeyMapper, SysAppKey> implements SysAppKeyService {

    @Override
    @Cacheable(value = "appKey:info", key = "#appId", unless = "#result == null")
    public SysAppKey getByAppId(String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        return baseMapper.selectByAppId(appId);
    }

    @Override
    public boolean verify(String appId, String secret) {
        SysAppKey appKey = getByAppId(appId);
        if (appKey == null) {
            log.warn("AppKey不存在: appId={}", appId);
            return false;
        }

        // 检查状态
        if (appKey.getSysAppKeyStatus() != 0) {
            log.warn("AppKey已禁用: appId={}", appId);
            return false;
        }

        // 检查过期时间
        if (appKey.getSysAppKeyExpireTime() != null) {
            long currentTime = System.currentTimeMillis() / 1000;
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
        // 先验证基本信息
        if (!verify(appId, secret)) {
            return false;
        }

        // 检查IP白名单
        SysAppKey appKey = getByAppId(appId);
        String whitelist = appKey.getSysAppKeyIpWhitelist();
        
        if (StringUtils.isNotBlank(whitelist)) {
            String[] allowedIps = whitelist.split(",");
            boolean ipAllowed = Arrays.stream(allowedIps)
                    .map(String::trim)
                    .anyMatch(allowedIp -> allowedIp.equals(ip) || "*".equals(allowedIp));
            
            if (!ipAllowed) {
                log.warn("IP不在白名单中: appId={}, ip={}, whitelist={}", appId, ip, whitelist);
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateUseInfo(String appId) {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            baseMapper.updateUseInfo(appId, currentTime);
        } catch (Exception e) {
            log.error("更新AppKey使用信息失败: appId={}", appId, e);
        }
    }
}
