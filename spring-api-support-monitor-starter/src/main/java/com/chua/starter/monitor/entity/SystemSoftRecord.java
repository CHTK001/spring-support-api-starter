package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 软件安装记录
 *
 * 记录安装/卸载操作及状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_soft_record")
@ApiModel("软件安装记录")
@Schema(name = "SystemSoftRecord")
public class SystemSoftRecord extends SysBase {

    @TableId(value = "system_soft_record_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键不能为空", groups = { UpdateGroup.class })
    private Integer systemSoftRecordId;

    /** 软件ID */
    @TableField("system_soft_id")
    private Integer systemSoftId;

    /** 版本ID */
    @TableField("system_soft_version_id")
    private Integer systemSoftVersionId;

    /** 目标服务器ID（多选逐条记录） */
    @TableField("system_server_id")
    private Integer systemServerId;

    /** 操作类型：INSTALL/UNINSTALL */
    @TableField("operation_type")
    private String operationType;

    /** 状态：PENDING/RUNNING/SUCCESS/FAILED */
    @TableField("status")
    private String status;

    /** 结果/日志 */
    @TableField("message")
    private String message;
}
