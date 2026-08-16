package com.chua.starter.achievement.service.support;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socket.support.util.Json;

import java.util.Map;

/**
 * Socket 推送公共支持工具，统一封装「协议事件 + module/event/data 消息体 + sendToUser」推送模板，
 * 供成就与签到等模块复用，避免重复实现。
 *
 * @author CH
 * @since 4.0.0.42
 */
public final class SocketPushSupport {

    /**
     * 推送消息使用的协议事件名称（前端 subscribe 监听 "message" 事件）
     */
    private static final String PROTOCOL_EVENT = "message";

    /**
     * 私有构造方法，禁止实例化
     */
    private SocketPushSupport() {
    }

    /**
     * 向指定用户推送一条 module/event/data 结构的消息。
     *
     * <p>发送格式：协议事件名固定为 {@code "message"}，消息体包含 {@code module}、{@code event} 和 {@code data}，
     * 前端 {@code subscribe(module, event, handler)} 匹配后分发。</p>
     *
     * @param socketTemplate Socket 会话操作模板
     * @param userId         用户ID
     * @param module         前端订阅模块名
     * @param event          前端订阅事件名
     * @param data           数据体（通常包含具体业务事件名与负载）
     */
    public static void push(SocketSessionTemplate socketTemplate, Long userId, String module, String event, Map<String, Object> data) {
        String body = Json.toJson(Map.of(
                "module", module,
                "event", event,
                "data", data
        ));
        socketTemplate.sendToUser(String.valueOf(userId), PROTOCOL_EVENT, body);
    }
}