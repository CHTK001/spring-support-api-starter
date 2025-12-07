package com.chua.starter.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @since 2024/12/4
 * @author CH    
 */

/**
 * sso任务表
 */
@Schema(description="sso任务表")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_task_sso")
public class SysTaskSso extends SysBase {
    @TableId(value = "sys_task_sso_id", type = IdType.AUTO)
    @Schema(description="")
    private Integer sysTaskSsoId;
    /**
     * 顺序
     */
    @TableField(value = "sys_task_sso_seq")
    @Schema(description="序号")
    private Integer sysTaskSsoSeq;

    /**
     * 任务ID
     */
    @TableField(value = "sys_task_sso_tid")
    @Schema(description="任务ID")
    private String sysTaskSsoTid;
    /**
     * 存储桶
     */
    @TableField(value = "sys_task_bucket")
    @Schema(description="存储桶")
    private String sysTaskBucket;

    /**
     * 任务名称
     */
    @TableField(value = "sys_task_sso_name")
    @Schema(description="任务名称")
    private String sysTaskSsoName;

    /**
     * 任务类型; 0: 文件; 1: 目录
     */
    @TableField(value = "sys_task_sso_type")
    @Schema(description="任务类型")
    private String sysTaskSsoType;

    /**
     * 分片数量
     */
    @TableField(value = "sys_task_sso_number")
    @Schema(description="分片数量")
    private Integer sysTaskSsoNumber;
    /**
     * 版本
     */
    @TableField(value = "sys_task_sso_version")
    @Schema(description="版本")
    @Version
    private Integer sysTaskSsoVersion;

    /**
     * 任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常
     */
    @TableField(value = "sys_task_sso_status")
    @Schema(description="任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常")
    private Integer sysTaskSsoStatus;

    /**
     * 当前文件大小
     */
    @TableField(value = "sys_task_sso_chuck")
    @Schema(description="当前文件大小")
    private Long sysTaskSsoChuck;

    /**
     * 任务描述
     */
    @TableField(value = "sys_task_sso_summary")
    @Schema(description="任务描述")
    private String sysTaskSsoSummary;

    /**
     * 任务md5
     */
    @TableField(value = "sys_task_sso_md5")
    private String sysTaskSsoMd5;

    /**
     * 任务总大小
     */
    @TableField(value = "sys_task_sso_total")
    @Schema(description="任务总大小")
    private Long sysTaskSsoTotal;

    /**
     * 子任务本地路径
     */
    @TableField(value = "sys_task_sso_local_path")
    @Schema(description="子任务本地路径")
    private String sysTaskSsoLocalPath;
}