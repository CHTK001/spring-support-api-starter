package com.chua.starter.project.service.support.hikvision;

import lombok.Data;

/**
 * 海康云眸消息实体类
 *
 * @author CH
 * @since 2023-04-05
 */
@Data
public class HiYunMouMessage {

    /**
     * 消息ID，用于唯一标识一条消息
     * <p>示例：msg-1234567890</p>
     */
    private String msgId;

    /**
     * 消息类型，表示消息的分类或用途
     * <p>示例：alarm、notification、event等</p>
     */
    private String msgType;

    /**
     * 消息内容，包含具体的消息信息
     * <p>示例：设备报警信息、通知内容等</p>
     */
    private String content;

    public HiYunMouMessage() {
    }

    public String toString() {
        return "Message [msgId=" + this.msgId + ", msgType=" + this.msgType + ", content=" + this.content + "]";
    }
}
