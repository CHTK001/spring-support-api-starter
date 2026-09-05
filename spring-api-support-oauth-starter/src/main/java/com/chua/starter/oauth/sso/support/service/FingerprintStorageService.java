package com.chua.starter.oauth.sso.support.service;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.support.entity.SysFingerprint;
import com.chua.starter.oauth.sso.support.mapper.SysFingerprintMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指纹/IP 映射存储服务
 * <p>
 * 登录时将指纹+IP+用户信息写入 MySQL 并缓存至 Redis，
 * 支持通过指纹或 IP 反查用户身份。
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
public class FingerprintStorageService {

    /** Redis key 前缀 - 按指纹查询 */
    private static final String FP_CACHE_PREFIX = "oauth:fingerprint:";
    /** Redis key 前缀 - 按IP查询 */
    private static final String IP_CACHE_PREFIX = "oauth:ip:";
    /** 缓存过期时间（秒）- 与 Token 过期时间对齐 */
    private static final int CACHE_TTL_SECONDS = 86_400 * 30;

    @Autowired(required = false)
    private SysFingerprintMapper fingerprintMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 保存或更新指纹映射
     * <p>同时写入 MySQL 和 Redis 缓存</p>
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param fingerprint 浏览器指纹（明文）
     * @param ip       客户端IP
     * @param loginType   登录类型
     * @param userAgent  User-Agent
     * @param browser    浏览器
     * @param os         操作系统
     */
    public void saveFingerprint(String userId, String username, String fingerprint,
                                String ip, String loginType,
                                String userAgent, String browser, String os) {
        if (StringUtils.isBlank(fingerprint) || StringUtils.isBlank(userId)) {
            return;
        }

        long now = System.currentTimeMillis();

        // 写入 MySQL
        if (fingerprintMapper != null) {
            try {
                SysFingerprint record = new SysFingerprint();
                record.setFingerprint(fingerprint);
                record.setUserId(userId);
                record.setUsername(username);
                record.setIp(ip);
                record.setLoginType(loginType);
                record.setUserAgent(userAgent);
                record.setBrowser(browser);
                record.setOs(os);
                record.setLoginTime(now);
                record.setLastLoginTime(now);
                record.setTokenCount(1);
                record.setStatus(1);

                SysFingerprint existing = findByFingerprint(fingerprint);
                if (existing == null) {
                    fingerprintMapper.insert(record);
                } else {
                    record.setId(existing.getId());
                    record.setUpdateTime(new java.util.Date());
                    fingerprintMapper.updateById(record);
                }
            } catch (Exception e) {
                log.warn("[Fingerprint] MySQL 写入失败: fingerprint={}", fingerprint, e);
            }
        }

        // 写入 Redis 缓存
        saveToCache(fingerprint, userId, username, ip, loginType, browser, os, now);
    }

    /**
     * 更新 Token 计数（登录/登出时调用）
     */
    public void updateTokenCount(String fingerprint, int delta) {
        if (StringUtils.isBlank(fingerprint)) {
            return;
        }
        String redisKey = FP_CACHE_PREFIX + fingerprint;
        String val = stringRedisTemplate.opsForValue().get(redisKey);
        if (val == null) {
            return;
        }
        try {
            Map<String, String> map = com.chua.common.support.lang.json.Json.fromJson(val, Map.class);
            if (map != null) {
                int count = Integer.parseInt(map.getOrDefault("tokenCount", "1"));
                count = Math.max(0, count + delta);
                map.put("tokenCount", String.valueOf(count));
                map.put("lastLoginTime", String.valueOf(System.currentTimeMillis()));
                if (count == 0) {
                    map.put("status", "0");
                }
                stringRedisTemplate.opsForValue().set(redisKey, com.chua.common.support.lang.json.Json.toJson(map),
                        CACHE_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("[Fingerprint] tokenCount 更新失败", e);
        }
    }

    /**
     * 按指纹查询用户信息
     * <p>优先从 Redis 缓存读取，未命中则查 MySQL</p>
     *
     * @param fingerprint 浏览器指纹
     * @return 用户信息 Map，不存在返回 null
     */
    @Cacheable(value = "oauth:fingerprint", key = "#fingerprint", unless = "#result == null")
    public Map<String, Object> findByFingerprint(String fingerprint) {
        if (StringUtils.isBlank(fingerprint)) {
            return null;
        }
        // 先查 Redis
        String redisKey = FP_CACHE_PREFIX + fingerprint;
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotBlank(cached)) {
            try {
                return com.chua.common.support.lang.json.Json.fromJson(cached, Map.class);
            } catch (Exception e) {
                log.warn("[Fingerprint] Redis 解析失败", e);
            }
        }
        // 查 MySQL
        if (fingerprintMapper != null) {
            try {
                SysFingerprint fp = fingerprintMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysFingerprint>()
                                .eq(SysFingerprint::getFingerprint, fingerprint));
                if (fp != null) {
                    Map<String, Object> result = toMap(fp);
                    stringRedisTemplate.opsForValue().set(redisKey,
                            com.chua.common.support.lang.json.Json.toJson(result),
                            CACHE_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
                    return result;
                }
            } catch (Exception e) {
                log.warn("[Fingerprint] MySQL 查询失败: fingerprint={}", fingerprint, e);
            }
        }
        return null;
    }

    /**
     * 按 IP 查询登录过的用户列表
     * <p>优先从 Redis 缓存读取</p>
     *
     * @param ip 客户端IP
     * @return 用户信息列表，不存在返回空列表
     */
    @Cacheable(value = "oauth:ip", key = "#ip", unless = "#result == null")
    public java.util.List<Map<String, Object>> findByIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return java.util.Collections.emptyList();
        }
        // 先查 Redis
        String redisKey = IP_CACHE_PREFIX + ip;
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotBlank(cached)) {
            try {
                return com.chua.common.support.lang.json.Json.fromJson(cached,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Map<String, Object>>>() {});
            } catch (Exception e) {
                log.warn("[Fingerprint] Redis 解析失败", e);
            }
        }
        // 查 MySQL
        if (fingerprintMapper != null) {
            try {
                var list = fingerprintMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysFingerprint>()
                                .eq(SysFingerprint::getIp, ip)
                                .orderByDesc(SysFingerprint::getLastLoginTime));
                if (!list.isEmpty()) {
                    java.util.List<Map<String, Object>> results = list.stream()
                            .map(this::toMap)
                            .collect(java.util.stream.Collectors.toList());
                    stringRedisTemplate.opsForValue().set(redisKey,
                            com.chua.common.support.lang.json.Json.toJson(results),
                            CACHE_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
                    return results;
                }
            } catch (Exception e) {
                log.warn("[Fingerprint] MySQL 查询失败: ip={}", ip, e);
            }
        }
        return java.util.Collections.emptyList();
    }

    /**
     * 清除指定指纹的缓存
     */
    @CacheEvict(value = "oauth:fingerprint", key = "#fingerprint")
    public void evictFingerprintCache(String fingerprint) {
        stringRedisTemplate.delete(FP_CACHE_PREFIX + fingerprint);
    }

    /**
     * 清除指定 IP 的缓存
     */
    @CacheEvict(value = "oauth:ip", key = "#ip")
    public void evictIpCache(String ip) {
        stringRedisTemplate.delete(IP_CACHE_PREFIX + ip);
    }

    // ==================== 内部方法 ====================

    private Map<String, Object> toMap(SysFingerprint fp) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", fp.getId());
        map.put("fingerprint", fp.getFingerprint());
        map.put("userId", fp.getUserId());
        map.put("username", fp.getUsername());
        map.put("ip", fp.getIp());
        map.put("loginType", fp.getLoginType());
        map.put("browser", fp.getBrowser());
        map.put("os", fp.getOs());
        map.put("loginTime", fp.getLoginTime());
        map.put("lastLoginTime", fp.getLastLoginTime());
        map.put("tokenCount", fp.getTokenCount());
        map.put("status", fp.getStatus());
        return map;
    }

    private void saveToCache(String fingerprint, String userId, String username,
                             String ip, String loginType, String browser, String os, long now) {
        try {
            Map<String, String> cache = new LinkedHashMap<>();
            cache.put("userId", userId);
            cache.put("username", username);
            cache.put("ip", ip != null ? ip : "");
            cache.put("loginType", loginType != null ? loginType : "");
            cache.put("browser", browser != null ? browser : "");
            cache.put("os", os != null ? os : "");
            cache.put("loginTime", String.valueOf(now));
            cache.put("lastLoginTime", String.valueOf(now));
            cache.put("tokenCount", "1");
            cache.put("status", "1");
            stringRedisTemplate.opsForValue().set(FP_CACHE_PREFIX + fingerprint,
                    com.chua.common.support.lang.json.Json.toJson(cache),
                    CACHE_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Fingerprint] Redis 写入失败: fingerprint={}", fingerprint, e);
        }
    }
}
