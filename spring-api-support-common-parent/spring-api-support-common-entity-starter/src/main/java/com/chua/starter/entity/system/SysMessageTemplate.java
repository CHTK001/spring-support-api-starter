package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 消息模板表
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "消息模板表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_message_template")
public class SysMessageTemplate extends SysBase implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
     * 模板标识
     */
    @TableId(value = "sys_message_template_id", type = IdType.AUTO)
    @Schema(description = "模板ID")
    @NotNull(message = "模板ID不能为空", groups = {UpdateGroup.class})
    /** sysmessagetemplateid */
    private Long sysMessageTemplateId;

    /**
     * 模板编码
     */
    @TableField(value = "sys_message_template_code")
    @Schema(description = "模板编码")
    @Size(max = 100, message = "模板编码最大长度需小于100")
    @NotEmpty(message = "模板编码不能为空", groups = {AddGroup.class})
    /** sysmessagetemplate代码 */
    private String sysMessageTemplateCode;

    /**
     * 模板名称
     */
    @TableField(value = "sys_message_template_name")
    @Schema(description = "模板名称")
    @Size(max = 100, message = "模板名称最大长度需小于100")
    /** sysmessagetemplatename */
    private String sysMessageTemplateName;

    /**
     * 模板标题(支持变量)
     */
    @TableField(value = "sys_message_template_title")
    @Schema(description = "模板标题(支持变量)")
    @Size(max = 255, message = "模板标题最大长度需小于255")
    /** sysmessagetemplatetitle */
    private String sysMessageTemplateTitle;

    /**
     * 模板内容(支持变量)
     */
    @TableField(value = "sys_message_template_content")
    @Schema(description = "模板内容(支持变量)")
    /** sysmessagetemplatecontent */
    private String sysMessageTemplateContent;

    /**
     * 状态: 0-禁用 1-启用
     */
    @TableField(value = "sys_message_template_status")
    @Schema(description = "状态: 0-禁用 1-启用")
    /** sysmessagetemplatestatus */
    private Integer sysMessageTemplateStatus = 1;
}
