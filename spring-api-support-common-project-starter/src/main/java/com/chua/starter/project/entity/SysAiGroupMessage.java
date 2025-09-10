package com.chua.starter.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "聊天内容")
@Schema(description = "聊天内容")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_group_message")
public class SysAiGroupMessage extends SysBase {
    @TableId(value = "sys_ai_group_message_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysAiGroupMessageId;

    /**
     * 分组ID
     */
    @TableField(value = "sys_ai_group_id")
    @ApiModelProperty(value = "分组ID")
    @Schema(description = "分组ID")
    private Integer sysAiGroupId;

    /**
     * 角色
     */
    @TableField(value = "sys_ai_group_message_role")
    @ApiModelProperty(value = "角色")
    @Schema(description = "角色")
    private String sysAiGroupMessageRole;

    /**
     * 聊天内容
     */
    @TableField(value = "sys_ai_group_message_content")
    @ApiModelProperty(value = "聊天内容")
    @Schema(description = "聊天内容")
    private String sysAiGroupMessageContent;

    /**
     * 消息类型
     */
    @TableField(value = "sys_ai_group_message_type")
    @ApiModelProperty(value = "消息类型")
    @Schema(description = "消息类型")
    private String sysAiGroupMessageType;
}