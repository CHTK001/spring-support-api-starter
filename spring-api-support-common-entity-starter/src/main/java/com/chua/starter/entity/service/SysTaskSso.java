package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SSO任务表
 *
 * @author CH
 * @since 2024/12/4
 */
@ApiModel(description = "SSO任务表")
@Schema(description = "SSO任务表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_task_sso")
public class SysTaskSso extends SysBase {

    /**
     * 任务主键ID
     */
    @TableId(value = "sys_task_sso_id", type = IdType.AUTO)
    @ApiModelProperty(value = "任务主键ID")
    @Schema(description = "任务主键ID")
    private Integer sysTaskSsoId;

    /**
     * 序号
     */
    @TableField(value = "sys_task_sso_seq")
    @ApiModelProperty(value = "序号")
    @Schema(description = "序号")
    private Integer sysTaskSsoSeq;

    /**
     * 任务ID
     */
    @TableField(value = "sys_task_sso_tid")
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    private String sysTaskSsoTid;

    /**
     * 存储桶
     */
    @TableField(value = "sys_task_bucket")
    @ApiModelProperty(value = "存储桶")
    @Schema(description = "存储桶")
    private String sysTaskBucket;

    /**
     * 任务名称
     */
    @TableField(value = "sys_task_sso_name")
    @ApiModelProperty(value = "任务名称")
    @Schema(description = "任务名称")
    private String sysTaskSsoName;

    /**
     * 任务类型: 0:文件 1:目录
     */
    @TableField(value = "sys_task_sso_type")
    @ApiModelProperty(value = "任务类型: 0:文件 1:目录")
    @Schema(description = "任务类型: 0:文件 1:目录")
    private String sysTaskSsoType;

    /**
     * 分片数量
     */
    @TableField(value = "sys_task_sso_number")
    @ApiModelProperty(value = "分片数量")
    @Schema(description = "分片数量")
    private Integer sysTaskSsoNumber;

    /**
     * 版本号(乐观锁)
     */
    @TableField(value = "sys_task_sso_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    @Version
    private Integer sysTaskSsoVersion;

    /**
     * 任务状态: 0:未开始 1:正在传输 2:完成 -1:处理异常
     */
    @TableField(value = "sys_task_sso_status")
    @ApiModelProperty(value = "任务状态: 0:未开始 1:正在传输 2:完成 -1:处理异常")
    @Schema(description = "任务状态: 0:未开始 1:正在传输 2:完成 -1:处理异常")
    private Integer sysTaskSsoStatus;

    /**
     * 当前文件大小(字节)
     */
    @TableField(value = "sys_task_sso_chuck")
    @ApiModelProperty(value = "当前文件大小(字节)")
    @Schema(description = "当前文件大小(字节)")
    private Long sysTaskSsoChuck;

    /**
     * 任务描述
     */
    @TableField(value = "sys_task_sso_summary")
    @ApiModelProperty(value = "任务描述")
    @Schema(description = "任务描述")
    private String sysTaskSsoSummary;

    /**
     * 任务MD5校验值
     */
    @TableField(value = "sys_task_sso_md5")
    @ApiModelProperty(value = "任务MD5校验值")
    @Schema(description = "任务MD5校验值")
    private String sysTaskSsoMd5;

    /**
     * 任务总大小(字节)
     */
    @TableField(value = "sys_task_sso_total")
    @ApiModelProperty(value = "任务总大小(字节)")
    @Schema(description = "任务总大小(字节)")
    private Long sysTaskSsoTotal;

    /**
     * 子任务本地路径
     */
    @TableField(value = "sys_task_sso_local_path")
    @ApiModelProperty(value = "子任务本地路径")
    @Schema(description = "子任务本地路径")
    private String sysTaskSsoLocalPath;
}
