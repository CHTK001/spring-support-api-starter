package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信下发日志表
 * @author CH
 */
@Schema(description = "短信下发日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sms_log")
public class SysSmsLog extends SysBase {

    /**
     * 短信日志主键ID
     */
    @TableId(value = "sys_sms_log_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysSmsLogId;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    private Integer sysProjectId;

    /**
     * 手机号
     */
    @TableField(value = "sys_sms_log_phone")
    @Schema(description = "手机号")
    private String sysSmsLogPhone;

    /**
     * 短信内容
     */
    @TableField(value = "sys_sms_log_content")
    @Schema(description = "短信内容")
    private String sysSmsLogContent;

    /**
     * 发送状态
     */
    @TableField(value = "sys_sms_log_status")
    @Schema(description = "发送状态")
    private Integer sysSmsLogStatus;
}
