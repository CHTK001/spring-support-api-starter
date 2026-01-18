package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CH
 * @since 2025/9/4 22:47
 */

/**
 * 聊天内容
 */
@Schema(description = "聊天内容")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_group_message")
public class SysAiGroupMessage extends SysBase {
    @TableId(value = "sys_ai_group_message_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysAiGroupMessageId;

    /**
     * 分组ID
     */
    @TableField(value = "sys_ai_group_id")
    @Schema(description = "分组ID")
    private Integer sysAiGroupId;

    /**
     * 角色
     */
    @TableField(value = "sys_ai_group_message_role")
    @Schema(description = "角色")
    private String sysAiGroupMessageRole;

    /**
     * 聊天内容
     */
    @TableField(value = "sys_ai_group_message_content")
    @Schema(description = "聊天内容")
    private String sysAiGroupMessageContent;

    /**
     * 消息类型
     */
    @TableField(value = "sys_ai_group_message_type")
    @Schema(description = "消息类型")
    private String sysAiGroupMessageType;
}
