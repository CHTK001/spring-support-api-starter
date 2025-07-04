package com.chua.starter.oauth.sso.support.service.impl;

import com.chua.starter.oauth.sso.support.entity.LoginRecord;
import com.chua.starter.oauth.sso.support.entity.LoginStatistics;
import com.chua.starter.oauth.sso.support.service.LoginTrackingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis OAuth认证登录追踪服务实现
 * 
 * @author CH
 * @version 4.0.0.32
 */
@Slf4j
@Service
public class RedisLoginTrackingServiceImpl implements LoginTrackingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis key 前缀
    private static final String LOGIN_RECORD_KEY = "oauth:login:record:";
    private static final String SESSION_KEY = "oauth:session:";
    private static final String IP_STATS_KEY = "oauth:ip:stats:";
    private static final String USER_STATS_KEY = "oauth:user:stats:";
    private static final String DAILY_STATS_KEY = "oauth:daily:";
    private static final String HOURLY_STATS_KEY = "oauth:hourly:";
    private static final String PROTOCOL_STATS_KEY = "oauth:protocol:";
    private static final String AUTH_METHOD_STATS_KEY = "oauth:auth:method:";
    private static final String IP_LOCK_KEY = "oauth:lock:ip:";
    private static final String FAILED_ATTEMPTS_KEY = "oauth:failed:";
    private static final String USER_FAILED_ATTEMPTS_KEY = "oauth:user:failed:";

    // 数据保留天数
    private static final int RETENTION_DAYS = 30;

    public RedisLoginTrackingServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void recordLogin(String ip, String username, String userId, boolean success, 
                           String protocol, String authMethod, String userAgent, String clientId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String recordId = UUID.randomUUID().toString();
            
            // 创建认证记录
            LoginRecord record = LoginRecord.builder()
                    .id(recordId)
                    .ip(ip)
                    .username(username)
                    .userId(userId)
                    .success(success)
                    .loginTime(now)
                    .userAgent(userAgent)
                    .loginType(protocol)
                    .authMethod(authMethod)
                    .clientId(clientId)
                    .active(success)
                    .build();

            // 保存认证记录
            String recordKey = LOGIN_RECORD_KEY + recordId;
            redisTemplate.opsForValue().set(recordKey, toJson(record), RETENTION_DAYS, TimeUnit.DAYS);

            // 更新统计信息
            updateStatistics(ip, username, userId, success, protocol, authMethod, now);

            // 处理失败认证
            if (!success) {
                handleFailedLogin(ip, userId);
            } else {
                // 清除失败计数
                clearFailedLoginCount(ip, userId);
            }

            log.debug("记录OAuth认证信息: IP={}, 用户={}, 成功={}, 协议={}", ip, username, success, protocol);
            
        } catch (Exception e) {
            log.error("记录OAuth认证信息失败", e);
        }
    }

    @Override
    public void recordSessionStart(String sessionId, String ip, String username, String userId, 
                                  String token, String refreshToken) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            LoginRecord session = LoginRecord.builder()
                    .sessionId(sessionId)
                    .ip(ip)
                    .username(username)
                    .userId(userId)
                    .loginTime(now)
                    .token(token)
                    .refreshToken(refreshToken)
                    .active(true)
                    .build();

            String sessionKey = SESSION_KEY + sessionId;
            redisTemplate.opsForValue().set(sessionKey, toJson(session), 24, TimeUnit.HOURS);
            
            log.debug("记录OAuth会话开始: SessionId={}, IP={}, 用户={}", sessionId, ip, username);
            
        } catch (Exception e) {
            log.error("记录OAuth会话开始失败", e);
        }
    }

    @Override
    public void recordSessionEnd(String sessionId) {
        try {
            String sessionKey = SESSION_KEY + sessionId;
            String sessionData = (String) redisTemplate.opsForValue().get(sessionKey);
            
            if (sessionData != null) {
                LoginRecord session = fromJson(sessionData, LoginRecord.class);
                LocalDateTime now = LocalDateTime.now();
                
                session.setLogoutTime(now);
                session.setActive(false);
                session.setSessionDuration(ChronoUnit.SECONDS.between(session.getLoginTime(), now));
                
                // 更新会话记录
                redisTemplate.opsForValue().set(sessionKey, toJson(session), RETENTION_DAYS, TimeUnit.DAYS);
                
                log.debug("记录OAuth会话结束: SessionId={}, 持续时间={}秒", sessionId, session.getSessionDuration());
            }
            
        } catch (Exception e) {
            log.error("记录OAuth会话结束失败", e);
        }
    }

    @Override
    public void recordTokenRefresh(String sessionId, String oldToken, String newToken) {
        try {
            String sessionKey = SESSION_KEY + sessionId;
            String sessionData = (String) redisTemplate.opsForValue().get(sessionKey);
            
            if (sessionData != null) {
                LoginRecord session = fromJson(sessionData, LoginRecord.class);
                session.setToken(newToken);
                
                // 更新会话记录
                redisTemplate.opsForValue().set(sessionKey, toJson(session), 24, TimeUnit.HOURS);
                
                log.debug("记录OAuth令牌刷新: SessionId={}, 新令牌={}", sessionId, newToken);
            }
            
        } catch (Exception e) {
            log.error("记录OAuth令牌刷新失败", e);
        }
    }

    @Override
    public LoginStatistics getLoginStatistics(int days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            // 获取所有认证记录
            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            List<LoginRecord> records = getRecordsInTimeRange(recordKeys, startTime, endTime);
            
            return buildStatistics(records, days, startTime, endTime);
            
        } catch (Exception e) {
            log.error("获取OAuth认证统计失败", e);
            return LoginStatistics.builder().days(days).build();
        }
    }

    @Override
    public LoginStatistics getLoginStatisticsByIp(String ip, int days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            List<LoginRecord> records = getRecordsInTimeRange(recordKeys, startTime, endTime)
                    .stream()
                    .filter(record -> ip.equals(record.getIp()))
                    .collect(Collectors.toList());
            
            return buildStatistics(records, days, startTime, endTime);
            
        } catch (Exception e) {
            log.error("获取IP OAuth认证统计失败", e);
            return LoginStatistics.builder().days(days).build();
        }
    }

    @Override
    public LoginStatistics getLoginStatisticsByUser(String userId, int days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            List<LoginRecord> records = getRecordsInTimeRange(recordKeys, startTime, endTime)
                    .stream()
                    .filter(record -> userId.equals(record.getUserId()))
                    .collect(Collectors.toList());
            
            return buildStatistics(records, days, startTime, endTime);
            
        } catch (Exception e) {
            log.error("获取用户OAuth认证统计失败", e);
            return LoginStatistics.builder().days(days).build();
        }
    }

    @Override
    public List<LoginRecord> getLoginHistory(int days, int limit) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            return getRecordsInTimeRange(recordKeys, startTime, endTime)
                    .stream()
                    .sorted((r1, r2) -> r2.getLoginTime().compareTo(r1.getLoginTime()))
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取OAuth认证历史失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<LoginRecord> getLoginHistoryByIp(String ip, int days, int limit) {
        return getLoginHistory(days, Integer.MAX_VALUE)
                .stream()
                .filter(record -> ip.equals(record.getIp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoginRecord> getLoginHistoryByUser(String userId, int days, int limit) {
        return getLoginHistory(days, Integer.MAX_VALUE)
                .stream()
                .filter(record -> userId.equals(record.getUserId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoginRecord> getActiveSessions() {
        try {
            Set<String> sessionKeys = redisTemplate.keys(SESSION_KEY + "*");
            List<LoginRecord> activeSessions = new ArrayList<>();
            
            for (String key : sessionKeys) {
                String sessionData = (String) redisTemplate.opsForValue().get(key);
                if (sessionData != null) {
                    LoginRecord session = fromJson(sessionData, LoginRecord.class);
                    if (session.isActive()) {
                        activeSessions.add(session);
                    }
                }
            }
            
            return activeSessions.stream()
                    .sorted((s1, s2) -> s2.getLoginTime().compareTo(s1.getLoginTime()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取活跃OAuth会话失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<LoginStatistics.IpStatistic> getTopLoginIps(int days, int limit) {
        try {
            LoginStatistics stats = getLoginStatistics(days);
            return stats.getIpStatistics()
                    .stream()
                    .sorted((ip1, ip2) -> Long.compare(ip2.getLoginCount(), ip1.getLoginCount()))
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取热门认证IP失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<LoginStatistics.UserStatistic> getTopActiveUsers(int days, int limit) {
        try {
            LoginStatistics stats = getLoginStatistics(days);
            return stats.getUserStatistics()
                    .stream()
                    .sorted((u1, u2) -> Long.compare(u2.getLoginCount(), u1.getLoginCount()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取最活跃用户失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isIpLocked(String ip) {
        String lockKey = IP_LOCK_KEY + ip;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    @Override
    public void lockIp(String ip, int duration, String reason) {
        String lockKey = IP_LOCK_KEY + ip;
        Map<String, Object> lockInfo = new HashMap<>();
        lockInfo.put("lockTime", LocalDateTime.now().toString());
        lockInfo.put("reason", reason);
        lockInfo.put("duration", duration);

        redisTemplate.opsForValue().set(lockKey, toJson(lockInfo), duration, TimeUnit.MINUTES);
        log.warn("IP已被锁定: {}, 持续时间: {}分钟, 原因: {}", ip, duration, reason);
    }

    @Override
    public void unlockIp(String ip) {
        String lockKey = IP_LOCK_KEY + ip;
        redisTemplate.delete(lockKey);
        log.info("IP已解锁: {}", ip);
    }

    @Override
    public int getFailedLoginCount(String ip, int timeWindow) {
        String failedKey = FAILED_ATTEMPTS_KEY + ip;
        String count = (String) redisTemplate.opsForValue().get(failedKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    @Override
    public int getFailedLoginCountByUser(String userId, int timeWindow) {
        String failedKey = USER_FAILED_ATTEMPTS_KEY + userId;
        String count = (String) redisTemplate.opsForValue().get(failedKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    @Override
    public void cleanupExpiredData() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(RETENTION_DAYS);

            // 清理过期的认证记录
            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            for (String key : recordKeys) {
                String recordData = (String) redisTemplate.opsForValue().get(key);
                if (recordData != null) {
                    LoginRecord record = fromJson(recordData, LoginRecord.class);
                    if (record.getLoginTime().isBefore(cutoffTime)) {
                        redisTemplate.delete(key);
                    }
                }
            }

            log.info("清理过期OAuth认证数据完成");

        } catch (Exception e) {
            log.error("清理过期OAuth认证数据失败", e);
        }
    }

    @Override
    public Map<String, Long> getProtocolUsageStats(int days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);

            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            List<LoginRecord> records = getRecordsInTimeRange(recordKeys, startTime, endTime);

            return records.stream()
                    .filter(record -> record.getLoginType() != null)
                    .collect(Collectors.groupingBy(
                            LoginRecord::getLoginType,
                            Collectors.counting()
                    ));

        } catch (Exception e) {
            log.error("获取协议使用统计失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, Long> getAuthMethodUsageStats(int days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);

            Set<String> recordKeys = redisTemplate.keys(LOGIN_RECORD_KEY + "*");
            List<LoginRecord> records = getRecordsInTimeRange(recordKeys, startTime, endTime);

            return records.stream()
                    .filter(record -> record.getAuthMethod() != null)
                    .collect(Collectors.groupingBy(
                            LoginRecord::getAuthMethod,
                            Collectors.counting()
                    ));

        } catch (Exception e) {
            log.error("获取认证方式使用统计失败", e);
            return Collections.emptyMap();
        }
    }
