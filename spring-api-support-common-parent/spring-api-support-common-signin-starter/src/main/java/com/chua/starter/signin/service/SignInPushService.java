package com.chua.starter.signin.service;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.achievement.service.support.SocketPushSupport;
import com.chua.starter.signin.dto.SignInResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 签到推送服务
 *
 * <p>通过统一的 {@link SocketSessionTemplate} 将签到成功消息实时推送给前端。
 * 该模板支持 WebSocket / 套接字.IO / r套接字 / SSE 等多种协议，
 * 由 spring-支持-套接字-启动 提供自动装配。</p>
 *
 * <p>推送消息格式：</p>
 * <pre>{@code
 * {
 *   "module": "signin",
 *   "event": "done",
 *   "data": { "event": "signin_done", "result": { ... } }
 * }
 * }</pre>}
 * }</pre>
 * <p>前端通过 {@code subscribe("signin", "done", handler)} 监听。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignInPushService {

    /**
     * 统一 套接字 会话操作模板，支持 WebSocket / 套接字.IO / r套接字 / SSE
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 前端订阅模块名
     */
    private static final String MODULE = "signin";

    /**
     * 前端订阅事件名
     */
    private static final String EVENT = "done";

    /**
     * 推送签到成功消息到前端。
     *
     * <p>发送格式：协议事件名固定为 {@code "message"}，消息体包含 {@code module}、{@code event} 和 {@code data}，
     * 前端 {@code subscribe("signin", "done", handler)} 匹配后分发。</p>
     *
     * @param userId 用户标识
     * @param result 签到结果
     */
    public void pushSignInDone(Long userId, SignInResultDTO result) {
        SocketPushSupport.push(socketSessionTemplate, userId, MODULE, EVENT,
                Map.of("event", "signin_done", "result", result));
        log.info("用户 {} 签到成功，获得 {} 积分，连续 {} 天", userId, result.getTodayPoints(), result.getStreak());
    }
}