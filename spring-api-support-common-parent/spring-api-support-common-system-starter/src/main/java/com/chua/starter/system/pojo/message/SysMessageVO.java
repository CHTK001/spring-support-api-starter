package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息视图对象
 *
 * <p>前端站内消息中心使用的消息结构，字段名与前端约定一致。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "站内消息视图")
public class SysMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID")
    private Long id;

    /**
     * 消息主题
     */
    @Schema(description = "消息主题")
    private String subject;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 发送时间（yyyy-MM-dd HH:mm:ss）
     */
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    /**
     * 已读状态：0-未读 / 1-已读
     */
    @Schema(description = "已读状态：0-未读 1-已读")
    private Integer readStatus;

    /**
     * 消息类型：system-系统通知 notice-公告 message-私信 warning-警告 info-信息
     */
    @Schema(description = "消息类型")
    private String type;

    /**
     * 消息级别：low-低 normal-普通 high-高 urgent-紧急
     */
    @Schema(description = "消息级别")
    private String level;

    /**
     * 跳转链接
     */
    @Schema(description = "跳转链接")
    private String url;
}
