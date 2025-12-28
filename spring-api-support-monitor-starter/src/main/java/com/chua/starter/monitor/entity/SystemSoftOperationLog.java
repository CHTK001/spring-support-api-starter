package com.chua.starter.monitor.entity;

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

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 系统软件操作日志实体类
 * <p>
 * 用于记录 Docker 容器、镜像等操作的详细日志信息
 *
 * @author CH
 * @since 2025/12/28
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件操作日志")
@Schema(description = "系统软件操作日志")
@Data
@TableName(value = "system_soft_operation_log")
public class SystemSoftOperationLog extends SysBase {

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    @Schema(description = "日志ID")
    private Long logId;

    /**
     * 操作ID（UUID，用于关联同一操作的多条日志）
     */
    @TableField(value = "operation_id")
    @ApiModelProperty(value = "操作ID")
    @Schema(description = "操作ID")
    @Size(max = 64, message = "操作ID最大长度要小于 64")
    private String operationId;

    /**
     * 操作类型（PULL_IMAGE/CREATE_CONTAINER/START_CONTAINER/STOP_CONTAINER/REMOVE_CONTAINER等）
     */
    @TableField(value = "operation_type")
    @ApiModelProperty(value = "操作类型")
    @Schema(description = "操作类型")
    @Size(max = 50, message = "操作类型最大长度要小于 50")
    private String operationType;

    /**
     * 操作目标（镜像名/容器名）
     */
    @TableField(value = "operation_target")
    @ApiModelProperty(value = "操作目标")
    @Schema(description = "操作目标")
    @Size(max = 255, message = "操作目标最大长度要小于 255")
    private String operationTarget;

    /**
     * 关联的容器ID
     */
    @TableField(value = "container_id")
    @ApiModelProperty(value = "关联的容器ID")
    @Schema(description = "关联的容器ID")
    private Integer containerId;

    /**
     * 关联的镜像ID
     */
    @TableField(value = "image_id")
    @ApiModelProperty(value = "关联的镜像ID")
    @Schema(description = "关联的镜像ID")
    private Integer imageId;

    /**
     * 关联的软件ID
     */
    @TableField(value = "soft_id")
    @ApiModelProperty(value = "关联的软件ID")
    @Schema(description = "关联的软件ID")
    private Integer softId;

    /**
     * 关联的服务器ID
     */
    @TableField(value = "server_id")
    @ApiModelProperty(value = "关联的服务器ID")
    @Schema(description = "关联的服务器ID")
    private Integer serverId;

    /**
     * 关联的操作记录ID
     */
    @TableField(value = "record_id")
    @ApiModelProperty(value = "关联的操作记录ID")
    @Schema(description = "关联的操作记录ID")
    private Integer recordId;

    /**
     * 日志级别（INFO/WARN/ERROR/DEBUG）
     */
    @TableField(value = "log_level")
    @ApiModelProperty(value = "日志级别")
    @Schema(description = "日志级别")
    @Size(max = 20, message = "日志级别最大长度要小于 20")
    private String logLevel;

    /**
     * 日志消息
     */
    @TableField(value = "log_message")
    @ApiModelProperty(value = "日志消息")
    @Schema(description = "日志消息")
    @Size(max = 5000, message = "日志消息最大长度要小于 5000")
    private String logMessage;

    /**
     * 日志详情（JSON格式，存储进度、状态等详细信息）
     */
    @TableField(value = "log_detail")
    @ApiModelProperty(value = "日志详情")
    @Schema(description = "日志详情")
    private String logDetail;

    /**
     * 操作进度（0-100）
     */
    @TableField(value = "progress")
    @ApiModelProperty(value = "操作进度")
    @Schema(description = "操作进度")
    private Integer progress;

    /**
     * 操作状态（PENDING/RUNNING/SUCCESS/FAILED/CANCELLED）
     */
    @TableField(value = "status")
    @ApiModelProperty(value = "操作状态")
    @Schema(description = "操作状态")
    @Size(max = 20, message = "操作状态最大长度要小于 20")
    private String status;

    /**
     * Docker 响应状态
     */
    @TableField(value = "docker_status")
    @ApiModelProperty(value = "Docker响应状态")
    @Schema(description = "Docker响应状态")
    @Size(max = 100, message = "Docker响应状态最大长度要小于 100")
    private String dockerStatus;

    /**
     * Docker 响应进度信息
     */
    @TableField(value = "docker_progress")
    @ApiModelProperty(value = "Docker响应进度")
    @Schema(description = "Docker响应进度")
    @Size(max = 500, message = "Docker响应进度最大长度要小于 500")
    private String dockerProgress;

    /**
     * 错误信息
     */
    @TableField(value = "error_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    @Size(max = 2000, message = "错误信息最大长度要小于 2000")
    private String errorMessage;

    /**
     * 错误堆栈
     */
    @TableField(value = "error_stack")
    @ApiModelProperty(value = "错误堆栈")
    @Schema(description = "错误堆栈")
    private String errorStack;

    /**
     * 日志时间
     */
    @TableField(value = "log_time")
    @ApiModelProperty(value = "日志时间")
    @Schema(description = "日志时间")
    private LocalDateTime logTime;

    /**
     * 操作开始时间
     */
    @TableField(value = "start_time")
    @ApiModelProperty(value = "操作开始时间")
    @Schema(description = "操作开始时间")
    private LocalDateTime startTime;

    /**
     * 操作结束时间
     */
    @TableField(value = "end_time")
    @ApiModelProperty(value = "操作结束时间")
    @Schema(description = "操作结束时间")
    private LocalDateTime endTime;

    /**
     * 操作耗时（毫秒）
     */
    @TableField(value = "duration")
    @ApiModelProperty(value = "操作耗时")
    @Schema(description = "操作耗时")
    private Long duration;

    /**
     * 操作用户
     */
    @TableField(value = "operator")
    @ApiModelProperty(value = "操作用户")
    @Schema(description = "操作用户")
    @Size(max = 100, message = "操作用户最大长度要小于 100")
    private String operator;

    /**
     * 客户端IP
     */
    @TableField(value = "client_ip")
    @ApiModelProperty(value = "客户端IP")
    @Schema(description = "客户端IP")
    @Size(max = 50, message = "客户端IP最大长度要小于 50")
    private String clientIp;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField(value = "ext_data")
    @ApiModelProperty(value = "扩展字段")
    @Schema(description = "扩展字段")
    private String extData;
}
