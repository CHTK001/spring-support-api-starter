package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务器文件上传请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "服务器文件上传请求")
public class ServerFileUploadRequest {

    /**
     * 服务器ID
     */
    @Schema(description = "服务器ID（只支持SSH类型服务器）")
    private Integer serverId;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String taskName;

    /**
     * 目标路径
     */
    @Schema(description = "目标路径")
    private String targetPath;

    /**
     * 上传模式
     */
    @Schema(description = "上传模式(REALTIME:实时 SCHEDULED:定时)")
    private String uploadMode;

    /**
     * 定时执行时间
     */
    @Schema(description = "定时执行时间（仅定时模式需要）")
    private String scheduledTime;

    /**
     * 优先级
     */
    @Schema(description = "优先级(1-10，数字越小优先级越高)")
    private Integer priority;

    /**
     * 是否覆盖
     */
    @Schema(description = "是否覆盖已存在文件")
    private Boolean overwrite;

    /**
     * 是否备份
     */
    @Schema(description = "是否备份原文件")
    private Boolean backup;

    /**
     * 是否压缩传输
     */
    @Schema(description = "是否压缩传输")
    private Boolean compress;

    /**
     * 是否验证文件完整性
     */
    @Schema(description = "是否验证文件完整性")
    private Boolean verify;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String description;

    /**
     * 标签
     */
    @Schema(description = "标签（多个标签用逗号分隔）")
    private String tags;

    /**
     * 超时时间(秒)
     */
    @Schema(description = "超时时间(秒)")
    private Integer timeoutSeconds;

    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数")
    private Integer maxRetry;
}
