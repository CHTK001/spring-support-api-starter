package com.chua.starter.achievement.service;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.service.support.SocketPushSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 成就推送服务
 *
 * <p>通过统一的 {@link SocketSessionTemplate} 将新解锁的成就实时推送给前端。
 * 该模板支持 WebSocket / Socket.IO / RSocket / SSE 等多种协议，
 * 由 spring-support-socket-starter 提供自动装配，无需关心底层协议细节。</p>
 *
 * <p>推送消息格式：</p>
 * <pre>{@code
 * {
 *   "module": "achievement",
 *   "event": "unlock",
 *   "data": { "event": "achievement_unlocked", "list": [...] }
 * }
 * }</pre>
 * <p>前端通过 {@code subscribe("achievement", "unlock", handler)} 监听。</p>
 *
 * @since 2026-08-07
 * @see SocketSessionTemplate#sendToUser(String, String, String)
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementPushService {

    /**
     * 统一 Socket 会话操作模板，支持 WebSocket / Socket.IO / RSocket / SSE
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 前端订阅模块名
     */
    private static final String MODULE = "achievement";

    /**
     * 前端订阅事件名
     */
    private static final String EVENT = "unlock";

    /**
     * 将新解锁的成就推送到前端。
     *
     * <p>发送格式：协议事件名固定为 {@code "message"}，消息体包含 {@code module}、{@code event} 和 {@code data}，
     * 前端 {@code subscribe("achievement", "unlock", handler)} 匹配后分发。</p>
     *
     * @param userId       用户ID
     * @param achievements 新解锁的成就列表
     */
    public void pushAchievements(Long userId, List<UnlockedAchievementDTO> achievements) {
        SocketPushSupport.push(socketSessionTemplate, userId, MODULE, EVENT,
                Map.of("event", "achievement_unlocked", "list", achievements));
        log.info("[成就推送] 用户 {} 解锁 {} 个成就", userId, achievements.size());
    }
}
