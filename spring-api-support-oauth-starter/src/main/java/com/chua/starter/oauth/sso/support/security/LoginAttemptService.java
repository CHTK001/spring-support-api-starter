package com.chua.starter.oauth.sso.support.security;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 * <p>
 * 用于跟踪用户登录尝试，防止暴力破解攻击。
 * 当用户连续登录失败超过阈值时，账户将被临时锁定。
 * </p>
 * <p>
 * 锁定阈值与时长优先读取 {@link OauthSecurityProperties#getLoginLock()}，
 * 未配置时回退到默认值（5 次 / 15 分钟）。
 * </p>
 *
 * @since 2025-12-28
 * @author CH
 */
@Slf4j
@Service
public class LoginAttemptService {

    /**
     * Redis 键前缀 - 登录失败次数
     */
    private static final String LOGIN_ATTEMPT_PREFIX = "oauth:login:attempt:";

    /**
     * Redis 键前缀 - 账户锁定
     */
    private static final String ACCOUNT_LOCKED_PREFIX = "oauth:login:locked:";

    /**
     * Redis 键前缀 - 按来源 IP 的登录失败次数
     */
    private static final String LOGIN_IP_FAILURE_PREFIX = "oauth:login:ip-failure:";

    /**
     * 来源 IP 失败计数窗口（秒）
     */
    private static final long IP_FAILURE_WINDOW_SECONDS = 60L;

    /**
     * 默认最大失败次数
     */
    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    /**
     * 默认锁定时间（分钟）
     */
    private static final int DEFAULT_LOCK_DURATION_MINUTES = 15;

    /**
     * 失败记录过期时间（分钟）
     */
    private static final int ATTEMPT_EXPIRE_MINUTES = 30;

    /**
     * 字符串Redis模板
    */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 安全配置（可选，用于覆盖默认锁定参数）
     */
    private OauthSecurityProperties securityProperties;

    /**
     * 创建 login尝试服务 实例
     * @param stringRedisTemplate 字符串redistemplate
     */
    public LoginAttemptService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 注入安全配置（可选）
     *
     * @param securityProperties 安全配置
     */
    @Autowired(required = false)
    public void setSecurityProperties(OauthSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @return 剩余尝试次数，0表示已锁定
     */
    public int recordFailedAttempt(String username) {
        return recordFailedAttempt(username, resolveMaxAttempts(), resolveLockDurationMinutes());
    }

    /**
     * 记录登录失败
     *
     * @param username            用户名
     * @param maxAttempts         最大尝试次数
     * @param lockDurationMinutes 锁定时长（分钟）
     * @return 剩余尝试次数，0表示已锁定
     */
    public int recordFailedAttempt(String username, int maxAttempts, int lockDurationMinutes) {
        if (StringUtils.isBlank(username)) {
            return maxAttempts;
        }

        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        String lockKey = ACCOUNT_LOCKED_PREFIX + username;

        try {
            // 增加失败次数
            Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey);
            if (attempts == null) {
                attempts = 1L;
            }

            // 设置过期时间
            stringRedisTemplate.expire(attemptKey, Duration.ofMinutes(resolveAttemptExpireMinutes()));

            int remaining = maxAttempts - attempts.intValue();
            log.debug("登录失败记录: username={}, attempts={}, remaining={}", username, attempts, remaining);

            // 检查是否需要锁定账户
            if (attempts >= maxAttempts) {
                // 锁定账户
                stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(System.currentTimeMillis()),
                        Duration.ofMinutes(lockDurationMinutes));
                log.warn("账户已锁定: username={}, lockDuration={}分钟", username, lockDurationMinutes);
                return 0;
            }

            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("记录登录失败异常: username={}, error={}", username, e.getMessage());
            return maxAttempts;
        }
    }

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @return true-已锁定，false-未锁定
     */
    public boolean isAccountLocked(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        String lockKey = ACCOUNT_LOCKED_PREFIX + username;
        try {
            Boolean exists = stringRedisTemplate.hasKey(lockKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("检查账户锁定状态异常: username={}, error={}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 获取账户锁定剩余时间（秒）
     *
     * @param username 用户名
     * @return 剩余锁定时间（秒），-1表示未锁定
     */
    public long getLockRemainingSeconds(String username) {
        if (StringUtils.isBlank(username)) {
            return -1;
        }

        String lockKey = ACCOUNT_LOCKED_PREFIX + username;
        try {
            Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : -1;
        } catch (Exception e) {
            log.error("获取锁定剩余时间异常: username={}, error={}", username, e.getMessage());
            return -1;
        }
    }

    /**
     * 登录成功后清除失败记录
     *
     * @param username 用户名
     */
    public void clearFailedAttempts(String username) {
        if (StringUtils.isBlank(username)) {
            return;
        }

        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        try {
            stringRedisTemplate.delete(attemptKey);
            log.debug("清除登录失败记录: username={}", username);
        } catch (Exception e) {
            log.error("清除登录失败记录异常: username={}, error={}", username, e.getMessage());
        }
    }

    /**
     * 手动解锁账户
     *
     * @param username 用户名
     * @return 是否解锁成功
     */
    public boolean unlockAccount(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        String lockKey = ACCOUNT_LOCKED_PREFIX + username;
        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        try {
            stringRedisTemplate.delete(lockKey);
            stringRedisTemplate.delete(attemptKey);
            log.info("账户已解锁: username={}", username);
            return true;
        } catch (Exception e) {
            log.error("解锁账户异常: username={}, error={}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getFailedAttempts(String username) {
        if (StringUtils.isBlank(username)) {
            return 0;
        }

        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        try {
            String value = stringRedisTemplate.opsForValue().get(attemptKey);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            log.error("获取失败次数异常: username={}, error={}", username, e.getMessage());
            return 0;
        }
    }

    /**
     * 获取锁定信息
     *
     * @param username 用户名
     * @return 锁定信息
     */
    public LockInfo getLockInfo(String username) {
        boolean locked = isAccountLocked(username);
        long remainingSeconds = locked ? getLockRemainingSeconds(username) : -1;
        int failedAttempts = getFailedAttempts(username);
        return new LockInfo(locked, remainingSeconds, failedAttempts, resolveMaxAttempts());
    }

    /**
     * 解析最大值尝试
     *
     * @return resolve最大尝试的结果
     */
    private int resolveMaxAttempts() {
        if (securityProperties != null && securityProperties.getLoginLock() != null) {
            return securityProperties.getLoginLock().getMaxAttempts();
        }
        return DEFAULT_MAX_ATTEMPTS;
    }

    /**
     * 解析锁持续时间minutes
     *
     * @return resolve锁持续时间minutes的结果
     */
    private int resolveLockDurationMinutes() {
        if (securityProperties != null && securityProperties.getLoginLock() != null) {
            return securityProperties.getLoginLock().getLockDurationMinutes();
        }
        return DEFAULT_LOCK_DURATION_MINUTES;
    }

    /**
     * 解析尝试expireminutes
     *
     * @return resolve尝试expireminutes的结果
     */
    private int resolveAttemptExpireMinutes() {
        if (securityProperties != null && securityProperties.getLoginLock() != null) {
            return securityProperties.getLoginLock().getAttemptExpireMinutes();
        }
        return ATTEMPT_EXPIRE_MINUTES;
    }

    /**
     * 锁定信息
     *
     * <p>由 {@link #getLockInfo(String)} 组装的登录防暴力破解快照，四个分量全部取自 Redis
     * 或本地配置，读取失败时各方法各自回落到安全值（未锁定 / 0 次），因此本记录不会抛异常。
     * {@link #remainingAttempts()} 与 {@link #formattedRemainingTime()} 是基于分量的派生视图，
     * 分量本身由外部按各自口径归一。</p>
     *
     * @param locked           账户当前是否处于锁定期，依据 Redis 键 {@code oauth:login:locked:<username>} 是否存在；用户名空白时视为 false
     * @param remainingSeconds 锁定剩余时长，单位秒，取自该键的 TTL；<strong>未锁定时为 -1</strong>（而非 0），用于与「已锁定但查询失败」区分
     * @param failedAttempts   当前统计窗口内已累计的登录失败次数，取自 Redis 计数键 {@code oauth:login:attempt:<username>}
     *                         （由 {@link #recordFailedAttempt(String)} 递增，窗口过期后自动清零），非负；无记录时为 0
     * @param maxAttempts      触发锁定的失败次数阈值（正数），优先取 {@link OauthSecurityProperties#getLoginLock()} 的配置，未装配安全配置时回落到默认 5
     */
    public record LockInfo(
            boolean locked,
            long remainingSeconds,
            int failedAttempts,
            int maxAttempts
    ) {
        /**
         * 获取剩余尝试次数
         * @return remaining尝试的结果
         */
        public int remainingAttempts() {
            return Math.max(0, maxAttempts - failedAttempts);
        }

        /**
         * 获取格式化的锁定剩余时间
         * @return formattedremaining时间的结果
         */
        public String formattedRemainingTime() {
            if (remainingSeconds <= 0) {
                return "";
            }
            // [P3C A20 豁免]
            long minutes = remainingSeconds / 60;
            // [P3C A20 豁免]
            long seconds = remainingSeconds % 60;
            if (minutes > 0) {
                return minutes + "分" + seconds + "秒";
            }
            return seconds + "秒";
        }
    }

    /**
     * 记录一次「来源 IP」维度的登录失败。
     *
     * <p>按账号锁定挡不住跨账号的密码喷洒，这里用 60 秒滑动窗口给每个可信来源 IP
     * 单独计数。地址为空（消息型协议拿不到对端 IP）时直接跳过。</p>
     *
     * @param ip 可信来源地址，允许为空
     * @return 当前窗口内的失败次数
     */
    public long recordIpFailure(String ip) {
        if (StringUtils.isBlank(ip)) {
            return 0L;
        }
        try {
            String key = LOGIN_IP_FAILURE_PREFIX + ip;
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(IP_FAILURE_WINDOW_SECONDS));
            }
            return count == null ? 0L : count;
        } catch (Exception e) {
            log.error("记录来源 IP 登录失败异常: ip={}, error={}", ip, e.getMessage());
            return 0L;
        }
    }

    /**
     * 来源 IP 是否已达到失败次数上限。
     *
     * <p>Redis 异常时返回 {@code false}（fail-open）：登录限流是防喷洒的辅助手段，
     * 不能因为缓存故障把所有正常用户挡在门外；限流过滤器另有 fail-closed 兜底。</p>
     *
     * @param ip           可信来源地址，允许为空
     * @param maxPerMinute 每分钟失败上限，{@code <=0} 表示不限制
     * @return 达到上限返回 true
     */
    public boolean isIpBlocked(String ip, int maxPerMinute) {
        if (StringUtils.isBlank(ip) || maxPerMinute <= 0) {
            return false;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(LOGIN_IP_FAILURE_PREFIX + ip);
            if (StringUtils.isBlank(value)) {
                return false;
            }
            return Long.parseLong(value) >= maxPerMinute;
        } catch (Exception e) {
            log.error("检查来源 IP 登录失败次数异常: ip={}, error={}", ip, e.getMessage());
            return false;
        }
    }

    /**
     * 登录成功后清空来源 IP 的失败计数。
     *
     * @param ip 可信来源地址，允许为空
     */
    public void clearIpFailures(String ip) {
        if (StringUtils.isBlank(ip)) {
            return;
        }
        try {
            stringRedisTemplate.delete(LOGIN_IP_FAILURE_PREFIX + ip);
        } catch (Exception e) {
            log.error("清空来源 IP 登录失败次数异常: ip={}, error={}", ip, e.getMessage());
        }
    }
}
