package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户协议视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户协议")
public class UserAgreementVO {

    @Schema(description = "协议标题")
    private String title;

    @Schema(description = "协议标签")
    private String label;

    @Schema(description = "协议主标题")
    private String headline;

    @Schema(description = "协议描述")
    private String description;

    @Schema(description = "协议版本")
    private String version;

    @Schema(description = "更新时间")
    private String updatedAt;

    @Schema(description = "协议章节")
    private List<Section> sections;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "协议章节")
    public static class Section {
        @Schema(description = "章节标题")
        private String title;

        @Schema(description = "章节内容")
        private String content;
    }
}
