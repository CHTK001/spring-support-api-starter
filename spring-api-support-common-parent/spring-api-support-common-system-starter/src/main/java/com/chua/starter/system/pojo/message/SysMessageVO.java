package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息视图对象
 *
 * <p>前端站内消息中心使用的消息结构，字段名与前端约定一致。</p>
 *
 * @param id         消息标识
 * @param subject    消息主题
 * @param content    消息内容
 * @param sendTime   发送时间（yyyy-MM-dd HH:mm:ss）
 * @param readStatus 已读状态：0-未读 / 1-已读
 * @param type       消息类型：系统-系统通知 notice-公告 消息-私信 警告-警告 信息-信息
 * @param level      消息级别：low-低 normal-普通 high-高 urgent-紧急
 * @param url        跳转链接
 * @since 4.0.0.42
 * @author CH
 */
@Schema(description = "站内消息视图")
public record SysMessageVO(
        @Schema(description = "消息ID") Long id,
        @Schema(description = "消息主题") String subject,
        @Schema(description = "消息内容") String content,
        @Schema(description = "发送时间") LocalDateTime sendTime,
        @Schema(description = "已读状态：0-未读 1-已读") Integer readStatus,
        @Schema(description = "消息类型") String type,
        @Schema(description = "消息级别") String level,
        @Schema(description = "跳转链接") String url) implements Serializable {

    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;
}
