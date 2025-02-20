package com.chua.starter.system.service.support.hikvision;

/**
 * @author CH
 */
public class HiYunMouMessage {
    private String msgId;
    private String msgType;
    private String content;

    public HiYunMouMessage() {
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgType() {
        return this.msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return "Message [msgId=" + this.msgId + ", msgType=" + this.msgType + ", content=" + this.content + "]";
    }
}
