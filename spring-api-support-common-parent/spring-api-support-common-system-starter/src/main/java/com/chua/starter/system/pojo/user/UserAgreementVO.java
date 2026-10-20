package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 用户协议视图对象
 *
 * @param title       协议标题
 * @param label       协议标签
 * @param headline    协议主标题
 * @param description 协议描述
 * @param version     协议版本
 * @param updatedAt   更新时间
 * @param sections    协议章节
 * @since 2026/08/03
 * @author CH
 */
@Schema(description = "用户协议")
public record UserAgreementVO(
        @Schema(description = "协议标题") String title,
        @Schema(description = "协议标签") String label,
        @Schema(description = "协议主标题") String headline,
        @Schema(description = "协议描述") String description,
        @Schema(description = "协议版本") String version,
        @Schema(description = "更新时间") String updatedAt,
        @Schema(description = "协议章节") List<Section> sections) {

    /**
     * 协议章节
     *
     * @param title   章节标题
     * @param content 章节内容
     * @since 2026/08/03
     * @author CH
     */
    @Schema(description = "协议章节")
    public record Section(
            @Schema(description = "章节标题") String title,
            @Schema(description = "章节内容") String content) {
    }
}
