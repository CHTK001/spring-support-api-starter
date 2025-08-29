package com.chua.starter.monitor.pojo.response;

import com.chua.common.support.lang.page.Page;
import com.chua.email.support.metadata.MailMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件拉取响应
 *
 * @author CH
 * @since 2025-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邮件拉取响应")
public class EmailFetchResponse {

    /**
     * 拉取的邮件数量
     */
    @Schema(description = "拉取的邮件数量")
    private Integer fetchedCount;

    /**
     * 新增的邮件数量
     */
    @Schema(description = "新增的邮件数量")
    private Integer newCount;

    /**
     * 重复的邮件数量
     */
    @Schema(description = "重复的邮件数量")
    private Integer duplicateCount;

    /**
     * 拉取状态
     */
    @Schema(description = "拉取状态")
    private String status;

    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message;

    /**
     * 分页数据
     */
    @Schema(description = "分页数据")
    private Page<MailMessage> record;

    /**
     * 邮件文件夹
     */
    @Schema(description = "邮件文件夹")
    private String folderName;

    /**
     * 数据源配置ID
     */
    @Schema(description = "数据源配置ID")
    private Integer settingId;
}