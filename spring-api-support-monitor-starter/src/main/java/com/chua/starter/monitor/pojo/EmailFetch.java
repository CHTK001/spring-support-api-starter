package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮件配置
 *
 * @author CH
 * @since 2023-04-05
 */
@Data
@Schema(description = "邮件配置")
public class EmailFetch {

    /**
     * 文件夹名称
     * 
     * @example "INBOX"
     */
    @Schema(description = "文件夹名称")
    private String folderName;

    /**
     * 命令类型
     * 
     * @example "FETCH"
     */
    @Schema(description = "命令类型")
    private String command;

    /**
     * 页码，默认为第1页
     * 
     * @example 1
     */
    @Schema(description = "页码，默认为第1页")
    private Integer pageNumber = 1;

    /**
     * 每页限制数量，默认50条
     * 
     * @example 50
     */
    @Schema(description = "每页限制数量，默认50条")
    private Integer limit = 50;
    /**
     * 消息ID
     *
     * @example "1234567890"
     */
    @Schema(description = "消息ID")
    private String messageId;
}
