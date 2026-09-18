package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体，记录系统关键操作的审计信息。
 *
 * @since 2026-08-11
 * @author CH
 */
@Data
@TableName("ms_operation_log")
@ApiModel(description = "操作日志")
@Schema(description = "操作日志")
public class MsOperationLog {

    /**
     * 主键标识，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Long id; // id

    /**
     * 操作用户标识
     */
    @TableField("operator")
    @ApiModelProperty("操作用户")
    @Schema(description = "操作用户")
    private String operator; // 操作符

    /**
     * 操作类型（创建/更新/删除/ENABLE/DISABLE/Move.com/设置/删除）
     */
    @TableField("operation")
    @ApiModelProperty("操作类型")
    @Schema(description = "操作类型（CREATE/UPDATE/DELETE/ENABLE/DISABLE/MOVE/SET/DEL）")
    private String operation; // operation

    /**
     * 目标模块（主机/任务/Redis/配置）
     */
    @TableField("target_module")
    @ApiModelProperty("目标模块")
    @Schema(description = "目标模块（HOST/TASK/REDIS/CONFIG）")
    private String targetModule; // Targetmodule

    /**
     * 目标对象标识
     */
    @TableField("target_id")
    @ApiModelProperty("目标对象ID")
    @Schema(description = "目标对象ID")
    private String targetId; // Targetid

    /**
     * 操作描述
     */
    @TableField("description")
    @ApiModelProperty("操作描述")
    @Schema(description = "操作描述")
    private String description; // description

    /**
     * 创建时间
     */
    @TableField("create_time")
    @ApiModelProperty("创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime; // 创建时间
}
