package com.chua.starter.signin.service;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socket.support.util.Json;
import com.chua.starter.signin.dto.SignInResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 签到推送服务
 *
 * <p>通过统一的 {@link SocketSessionTemplate} 将签到成功消息实时推送给前端。
 * 该模板支持 WebSocket / Socket.IO / RSocket / SSE 等多种协议，
 * 由 spring-support-socket-starter 提供自动装配。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignInPushService {

    /**
     * 签到成功事件名称
     */
    private static final String EVENT_SIGN_IN_DONE = "signin_done";

    /**
     * 统一 Socket 会话操作模板，支持 WebSocket / Socket.IO / RSocket / SSE
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 推送签到成功消息到前端。
     *
     * <p>前端通过 {@code subscribe("signin", "done", handler)} 监听此事件。</p>
     *
     * @param userId 用户ID
     * @param result 签到结果
     */
    public void pushSignInDone(Long userId, SignInResultDTO result) {
        String message = Json.toJson(Map.of(
                "event", EVENT_SIGN_IN_DONE,
                "data", result
        ));
        socketSessionTemplate.sendToUser(String.valueOf(userId), EVENT_SIGN_IN_DONE, message);
        log.info("用户 {} 签到成功，获得 {} 积分，连续 {} 天", userId, result.getTodayPoints(), result.getStreak());
    }
}