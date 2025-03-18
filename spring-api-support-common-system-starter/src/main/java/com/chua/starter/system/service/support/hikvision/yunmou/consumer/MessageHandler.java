package com.chua.starter.system.service.support.hikvision.yunmou.consumer;

import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.yunmou.pojo.YunMouCardEventMessage;

/**
 * 消息处理器
 *
 * @author CH
 */
public interface MessageHandler {
    /**
     * 处理消息
     *
     * @param yunMouCardEventMessage 消息
     * @param client                 客户端
     */
    void handle(YunMouCardEventMessage yunMouCardEventMessage, HikvisionClient client);
}
