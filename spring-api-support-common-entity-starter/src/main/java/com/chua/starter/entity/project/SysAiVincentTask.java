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
 * 文生图任务
 */
@Schema(description = "文生图任务")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_vincent_task")
public class SysAiVincentTask extends SysBase {
    @TableId(value = "sys_ai_vincent_task_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysAiVincentTaskId;

    /**
     * 请求ID
     */
    @TableField(value = "sys_ai_vincent_task_uid")
    @Schema(description = "请求ID")
    private String sysAiVincentTaskUid;

    /**
     * 状态;0:未完成; 1: 完成; 2:失败
     */
    @TableField(value = "sys_ai_vincent_task_status")
    @Schema(description = "状态;0:未完成; 1: 完成; 2:失败")
    private Integer sysAiVincentTaskStatus;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    private Integer sysProjectId;

    /**
     * 模型id
     */
    @TableField(value = "sys_ai_module_id")
    @Schema(description = "模型id")
    private Integer sysAiModuleId;

    /**
     * 参数
     */
    @TableField(value = "sys_ai_vincent_task_condition")
    @Schema(description = "参数")
    private String sysAiVincentTaskCondition;
    /**
     * 类型
     */
    @TableField(value = "sys_ai_vincent_task_type")
    @Schema(description = "类型")
    private String sysAiVincentTaskType;
    /**
     * 链接
     */
    @TableField(value = "sys_ai_vincent_task_url")
    @Schema(description = "链接")
    private String sysAiVincentTaskUrl;
    /**
     * 本地链接
     */
    @TableField(value = "sys_ai_vincent_task_local_url")
    @Schema(description = "本地链接")
    private String sysAiVincentTaskLocalUrl;
    /**
     * 请求ID
     */
    @TableField(value = "sys_ai_vincent_task_request_id")
    @Schema(description = "请求ID")
    private String sysAiVincentTaskRequestId;
}
