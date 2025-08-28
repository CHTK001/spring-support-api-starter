package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件备份响应
 *
 * @author CH
 * @since 2025-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邮件备份响应")
public class EmailBackupResponse {

    /**
     * 备份状态
     */
    @Schema(description = "备份状态")
    private String status;

    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message;

    /**
     * 邮件ID
     */
    @Schema(description = "邮件ID")
    private Integer emailId;

    /**
     * 邮件主题
     */
    @Schema(description = "邮件主题")
    private String subject;

    /**
     * 发件人
     */
    @Schema(description = "发件人")
    private String sender;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 数据源配置ID
     */
    @Schema(description = "数据源配置ID")
    private Integer settingId;
}