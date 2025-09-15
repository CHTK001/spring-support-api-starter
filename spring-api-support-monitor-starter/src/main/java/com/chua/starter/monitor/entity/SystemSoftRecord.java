package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 系统软件操作记录实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件操作记录")
@Schema(description = "系统软件操作记录")
@Data
@TableName(value = "system_soft_record")
public class SystemSoftRecord extends SysBase {

    /**
     * 系统软件记录ID
     */
    @TableId(value = "system_soft_record_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统软件记录ID")
    @Schema(description = "系统软件记录ID")
    @NotNull(message = "系统软件记录ID不能为空", groups = {UpdateGroup.class})
    private Integer systemSoftRecordId;

    /**
     * 关联的软件ID
     */
    @TableField(value = "system_soft_id")
    @ApiModelProperty(value = "关联的软件ID")
    @Schema(description = "关联的软件ID")
    private Integer systemSoftId;

    /**
     * 关联的软件版本ID
     */
    @TableField(value = "system_soft_version_id")
    @ApiModelProperty(value = "关联的软件版本ID")
    @Schema(description = "关联的软件版本ID")
    private Integer systemSoftVersionId;

    /**
     * 关联的服务器ID
     */
    @TableField(value = "system_server_id")
    @ApiModelProperty(value = "关联的服务器ID")
    @Schema(description = "关联的服务器ID")
    private Integer systemServerId;

    /**
     * 操作类型
     */
    @TableField(value = "system_soft_record_operation_type")
    @ApiModelProperty(value = "操作类型")
    @Schema(description = "操作类型")
    @NotBlank(message = "操作类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "操作类型最大长度要小于 50")
    private String systemSoftRecordOperationType;

    /**
     * 操作消息
     */
    @TableField(value = "system_soft_record_message")
    @ApiModelProperty(value = "操作消息")
    @Schema(description = "操作消息")
    @Size(max = 2000, message = "操作消息最大长度要小于 2000")
    private String systemSoftRecordMessage;

    /**
     * 操作方法
     */
    @TableField(value = "system_soft_record_method")
    @ApiModelProperty(value = "操作方法")
    @Schema(description = "操作方法")
    @Size(max = 100, message = "操作方法最大长度要小于 100")
    private String systemSoftRecordMethod;

    /**
     * 环境参数
     */
    @TableField(value = "system_soft_record_env_params")
    @ApiModelProperty(value = "环境参数")
    @Schema(description = "环境参数")
    @Size(max = 2000, message = "环境参数最大长度要小于 2000")
    private String systemSoftRecordEnvParams;

    /**
     * 操作参数
     */
    @TableField(value = "system_soft_record_params")
    @ApiModelProperty(value = "操作参数")
    @Schema(description = "操作参数")
    @Size(max = 2000, message = "操作参数最大长度要小于 2000")
    private String systemSoftRecordParams;

    /**
     * 记录时间
     */
    @TableField(value = "system_soft_record_time")
    @ApiModelProperty(value = "记录时间")
    @Schema(description = "记录时间")
    private LocalDateTime systemSoftRecordTime;

    /**
     * 操作状态
     */
    @TableField(value = "system_soft_record_status")
    @ApiModelProperty(value = "操作状态")
    @Schema(description = "操作状态")
    private Integer systemSoftRecordStatus;

    /**
     * 操作用户
     */
    @TableField(value = "system_soft_record_user")
    @ApiModelProperty(value = "操作用户")
    @Schema(description = "操作用户")
    @Size(max = 100, message = "操作用户最大长度要小于 100")
    private String systemSoftRecordUser;

    /**
     * 关联的容器ID
     */
    @TableField(value = "system_soft_record_container_id")
    @ApiModelProperty(value = "关联的容器ID")
    @Schema(description = "关联的容器ID")
    @Size(max = 100, message = "关联的容器ID最大长度要小于 100")
    private String systemSoftRecordContainerId;

    /**
     * 操作开始时间
     */
    @TableField(value = "system_soft_record_start_time")
    @ApiModelProperty(value = "操作开始时间")
    @Schema(description = "操作开始时间")
    private LocalDateTime systemSoftRecordStartTime;

    /**
     * 操作结束时间
     */
    @TableField(value = "system_soft_record_end_time")
    @ApiModelProperty(value = "操作结束时间")
    @Schema(description = "操作结束时间")
    private LocalDateTime systemSoftRecordEndTime;

    /**
     * 操作耗时（毫秒）
     */
    @TableField(value = "system_soft_record_duration")
    @ApiModelProperty(value = "操作耗时")
    @Schema(description = "操作耗时")
    private Long systemSoftRecordDuration;

    /**
     * 错误信息
     */
    @TableField(value = "system_soft_record_error_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    @Size(max = 2000, message = "错误信息最大长度要小于 2000")
    private String systemSoftRecordErrorMessage;

    /**
     * 操作结果
     */
    @TableField(value = "system_soft_record_result")
    @ApiModelProperty(value = "操作结果")
    @Schema(description = "操作结果")
    @Size(max = 5000, message = "操作结果最大长度要小于 5000")
    private String systemSoftRecordResult;

    /**
     * 记录备注
     */
    @TableField(value = "system_soft_record_remark")
    @ApiModelProperty(value = "记录备注")
    @Schema(description = "记录备注")
    @Size(max = 500, message = "记录备注最大长度要小于 500")
    private String systemSoftRecordRemark;

    /**
     * 扩展字段1
     */
    @TableField(value = "system_soft_record_ext_field1")
    @ApiModelProperty(value = "扩展字段1")
    @Schema(description = "扩展字段1")
    @Size(max = 255, message = "扩展字段1最大长度要小于 255")
    private String systemSoftRecordExtField1;

    /**
     * 扩展字段2
     */
    @TableField(value = "system_soft_record_ext_field2")
    @ApiModelProperty(value = "扩展字段2")
    @Schema(description = "扩展字段2")
    @Size(max = 255, message = "扩展字段2最大长度要小于 255")
    private String systemSoftRecordExtField2;

    /**
     * 扩展字段3
     */
    @TableField(value = "system_soft_record_ext_field3")
    @ApiModelProperty(value = "扩展字段3")
    @Schema(description = "扩展字段3")
    @Size(max = 255, message = "扩展字段3最大长度要小于 255")
    private String systemSoftRecordExtField3;
}