package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 模板渲染预览响应
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "模板渲染预览响应")
public class TemplatePreviewResponse {

    /**
     * 模板标识
    */
    @Schema(description = "模板标识")
    private Long sysMessageTemplateId;

    /**
     * 渲染后的标题
    */
    @Schema(description = "渲染后的标题")
    private String title;

    /**
     * 渲染后的内容
    */
    @Schema(description = "渲染后的内容")
    private String content;

    /**
     * 未填充的占位符
    */
    @Schema(description = "未填充的占位符")
    private List<String> unresolvedParams;

    /**
     * 是否所有占位符均已填充
    */
    @Schema(description = "是否所有占位符均已填充")
    private Boolean complete;

    /**
     * 内置变量清单(供前端 tip 展示)
     */
    @Schema(description = "内置变量清单")
    private List<String> builtInVars;
}
