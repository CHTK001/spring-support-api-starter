package com.chua.starter.service.impl;

import com.chua.common.support.text.json.Json;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.starter.entity.service.SysMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统消息Socket.IO推送服务
 * 仅在引入socket.io依赖时生效
 *
 * @author CH
 * @since 2024/12/04
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(name = "com.chua.socket.support.session.SocketSessionTemplate")
public class SysMessagePushService {

    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 推送消息
     *
     * @param message 消息对象
     */
    public void pushMessage(SysMessage message) {
        try {
            Map<String, Object> data = new HashMap<>(15);
            data.put("messageId", message.getSysMessageId());
            data.put("title", message.getSysMessageTitle());
            data.put("content", message.getSysMessageContent());
            data.put("type", message.getSysMessageType());
            data.put("level", message.getSysMessageLevel());
            data.put("senderId", message.getSysMessageSenderId());
            data.put("senderName", message.getSysMessageSenderName());
            data.put("receiverId", message.getSysMessageReceiverId());
            data.put("bizType", message.getSysMessageBizType());
            data.put("bizId", message.getSysMessageBizId());
            data.put("url", message.getSysMessageUrl());
            data.put("sendTime", message.getSysMessageSendTime());
            data.put("dataId", message.getSysMessageReceiverId() != null ?
                    String.valueOf(message.getSysMessageReceiverId()) : "0");

            // 推送主题格式: service:message:push@{receiverId}
            // 广播消息(receiverId=0)使用 service:message:push@0
            String topic = SocketTopics.Service.Message.PUSH + "@" + 
                    (message.getSysMessageReceiverId() != null ? message.getSysMessageReceiverId() : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.debug("消息推送成功: messageId={}", message.getSysMessageId());
        } catch (Exception e) {
            log.error("消息推送失败: messageId={}", message.getSysMessageId(), e);
        }
    }

    /**
     * 推送撤回通知
     *
     * @param message 消息对象
     */
    public void pushRecallNotice(SysMessage message) {
        try {
            Map<String, Object> data = new HashMap<>(4);
            data.put("messageId", message.getSysMessageId());
            data.put("action", "recall");
            data.put("receiverId", message.getSysMessageReceiverId());
            data.put("dataId", message.getSysMessageReceiverId() != null ?
                    String.valueOf(message.getSysMessageReceiverId()) : "0");

            socketSessionTemplate.send(SocketTopics.Service.Message.DELETE, Json.toJson(data));
        } catch (Exception e) {
            log.error("撤回通知推送失败: messageId={}", message.getSysMessageId(), e);
        }
    }
}
