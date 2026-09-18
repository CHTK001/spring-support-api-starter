package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户协议视图对象
 *
 * @since 2026/08/03
 * @author CH
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户协议")
public class UserAgreementVO {

    /**
     * 协议标题
     */
    @Schema(description = "协议标题")
    /** 标题 */
    private String title;

    /**
    * 协议标签
     */
    @Schema(description = "协议标签")
    /** 标签 */
    private String label;

    /**
    * 协议主标题
     */
    @Schema(description = "协议主标题")
    /** Headline */
    private String headline;

    /**
    * 协议描述
     */
    @Schema(description = "协议描述")
    /** 描述 */
    private String description;

    /**
    * 协议版本
     */
    @Schema(description = "协议版本")
    /** 版本 */
    private String version;

    /**
    * 更新时间
     */
    @Schema(description = "更新时间")
    /** 更新时间AT */
    private String updatedAt;

    /**
    * 协议章节
     */
    @Schema(description = "协议章节")
    /** Sections */
    private List<Section> sections;

    /**
    * 协议章节
    *
    * @since 2026/08/03
    * @author CH
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "协议章节")
    public static class Section {
        /**
         * 章节标题
         */
        @Schema(description = "章节标题")
        /** 标题 */
        private String title;

        /**
        * 章节内容
         */
        @Schema(description = "章节内容")
        /** 内容 */
        private String content;
    }
}
