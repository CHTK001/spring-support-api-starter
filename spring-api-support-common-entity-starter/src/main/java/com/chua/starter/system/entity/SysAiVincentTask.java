package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文生图任务
 */
@ApiModel(description = "文生图任务")
@Schema(description = "文生图任务")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_vincent_task")
public class SysAiVincentTask extends SysBase {
    @TableId(value = "sys_ai_vincent_task_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysAiVincentTaskId;

    /**
     * 请求ID
     */
    @TableField(value = "sys_ai_vincent_task_uid")
    @ApiModelProperty(value = "请求ID")
    @Schema(description = "请求ID")
    private String sysAiVincentTaskUid;

    /**
     * 状态;0:未完成; 1: 完成; 2:失败
     */
    @TableField(value = "sys_ai_vincent_task_status")
    @ApiModelProperty(value = "状态;0:未完成; 1: 完成; 2:失败")
    @Schema(description = "状态;0:未完成; 1: 完成; 2:失败")
    private Integer sysAiVincentTaskStatus;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @ApiModelProperty(value = "项目ID")
    @Schema(description = "项目ID")
    private Integer sysProjectId;

    /**
     * 模型id
     */
    @TableField(value = "sys_ai_module_id")
    @ApiModelProperty(value = "模型id")
    @Schema(description = "模型id")
    private Integer sysAiModuleId;

    /**
     * 参数
     */
    @TableField(value = "sys_ai_vincent_task_condition")
    @ApiModelProperty(value = "参数")
    @Schema(description = "参数")
    private String sysAiVincentTaskCondition;
    /**
     * 链接
     */
    @TableField(value = "sys_ai_vincent_task_url")
    @ApiModelProperty(value = "链接")
    @Schema(description = "链接")
    private String sysAiVincentTaskUrl;
    /**
     * 请求ID
     */
    @TableField(value = "sys_ai_vincent_task_request_id")
    @ApiModelProperty(value = "请求ID")
    @Schema(description = "请求ID")
    private String sysAiVincentTaskRequestId;
}