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
 * @since 4.0.0.42
 * @author CH
 */
@Data
@Schema(description = "站内消息视图")
public class SysMessageVO implements Serializable {

    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
      * 消息标识
     */
    @Schema(description = "消息ID")
    /** 标识 */
    private Long id;

    /**
     * 消息主题
     */
    @Schema(description = "消息主题")
    /** 主题 */
    private String subject;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    /** 内容 */
    private String content;

    /**
     * 发送时间（yyyy-MM-dd HH:mm:ss）
     */
    @Schema(description = "发送时间")
    /** 发送时间 */
    private LocalDateTime sendTime;

    /**
     * 已读状态：0-未读 / 1-已读
     */
    @Schema(description = "已读状态：0-未读 1-已读")
    /** 读取状态 */
    private Integer readStatus;

    /**
      * 消息类型：系统-系统通知 notice-公告 消息-私信 警告-警告 信息-信息
     */
    @Schema(description = "消息类型")
    /** 类型 */
    private String type;

    /**
     * 消息级别：low-低 normal-普通 high-高 urgent-紧急
     */
    @Schema(description = "消息级别")
    /** 级别 */
    private String level;

    /**
     * 跳转链接
     */
    @Schema(description = "跳转链接")
    /** URL */
    private String url;
}
