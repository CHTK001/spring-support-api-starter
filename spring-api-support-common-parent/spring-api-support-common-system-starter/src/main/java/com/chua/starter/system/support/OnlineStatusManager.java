package com.chua.starter.system.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 在线状态管理
 *
 * <p>static 本地认证模式下没有独立 OAuth 服务器维护在线列表，这里基于 Redis 记录用户在线状态：
 * 登录成功写入在线键并设置 TTL，登出删除；用户列表通过管道批量判断在线，用于头像呼吸灯。
 * 停止活动超过 TTL 后键自动过期，判定为离线。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineStatusManager {

    /**
     * 在线键前缀
     */
    private static final String KEY_PREFIX = "sys:online:";

    /**
     * 在线状态有效期（分钟）：超过该时长未重新登录视为离线
     */
    private static final long TTL_MINUTES = 30L;

    /**
     * Redis 模板
     */
    private final RedisTemplate redisTemplate;

    /**
     * 标记用户在线（登录成功时调用）。
     *
     * @param userId 用户主键，为空时忽略
     */
    public void markOnline(Integer userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key(userId), String.valueOf(System.currentTimeMillis()),
                    TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[Online] 标记在线失败: userId={}, {}", userId, e.getMessage());
        }
    }

    /**
     * 标记用户离线（登出时调用）。
     *
     * @param userId 用户主键，为空时忽略
     */
    public void markOffline(Integer userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(key(userId));
        } catch (Exception e) {
            log.warn("[Online] 标记离线失败: userId={}, {}", userId, e.getMessage());
        }
    }

    /**
     * 批量解析在线用户主键集合（管道批量 hasKey，避免逐个往返）。
     *
     * @param userIds 待判断的用户主键集合
     * @return 其中在线的用户主键集合
     */
    @SuppressWarnings("unchecked")
    public Set<Integer> resolveOnlineIds(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Integer> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptySet();
        }
        try {
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Integer id : ids) {
                    connection.keyCommands().exists(key(id).getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });
            Set<Integer> online = new HashSet<>();
            for (int i = 0; i < ids.size() && i < results.size(); i++) {
                Object r = results.get(i);
                if (r instanceof Boolean b && b) {
                    online.add(ids.get(i));
                } else if (r instanceof Number n && n.longValue() > 0) {
                    online.add(ids.get(i));
                }
            }
            return online;
        } catch (Exception e) {
            log.warn("[Online] 批量查询在线状态失败: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 拼接在线键。
     *
     * @param userId 用户主键
     * @return 完整键名
     */
    private String key(Integer userId) {
        return KEY_PREFIX + userId;
    }
}
