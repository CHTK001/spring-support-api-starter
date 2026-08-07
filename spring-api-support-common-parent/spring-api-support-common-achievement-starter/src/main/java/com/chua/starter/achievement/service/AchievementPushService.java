package com.chua.starter.achievement.service;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
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
 * <p>推送消息格式为 JSON 字符串，包含事件类型和成就数据：</p>
 * <pre>{@code
 * {"event":"achievement_unlocked","data":{...}}
 * }</pre>
 *
 * @author CH
 * @since 2026-08-07
 * @see SocketSessionTemplate#sendToUser(String, String, String)
 * @see SocketSessionTemplate#sendToUserObject(String, String, Object)
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
     * 推送消息的事件类型
     */
    private static final String EVENT_ACHIEVEMENT_UNLOCKED = "achievement_unlocked";

    /**
     * 将新解锁的成就推送到前端 WebSocket 连接。
     *
     * <p>使用 {@link SocketSessionTemplate#sendToUser(String, String, String)}
     * 向指定用户发送事件消息。底层协议（WebSocket/SSE/Socket.IO）
     * 由 socket-starter 的 SPI 机制自动选择。</p>
     *
     * @param userId       用户ID
     * @param achievements 新解锁的成就列表
     */
    public void pushAchievements(Long userId, List<UnlockedAchievementDTO> achievements) {
        String message = com.chua.socket.support.util.Json.toJson(Map.of(
                "event", EVENT_ACHIEVEMENT_UNLOCKED,
                "data", achievements
        ));
        socketSessionTemplate.sendToUser(String.valueOf(userId), EVENT_ACHIEVEMENT_UNLOCKED, message);
        log.info("[成就推送] 用户 {} 解锁 {} 个成就", userId, achievements.size());
    }
}
