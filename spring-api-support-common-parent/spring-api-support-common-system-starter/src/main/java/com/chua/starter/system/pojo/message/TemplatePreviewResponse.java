package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 模板渲染预览响应
 *
 * @param sysMessageTemplateId 模板标识
 * @param title                渲染后的标题
 * @param content              渲染后的内容
 * @param unresolvedParams     未填充的占位符
 * @param complete             是否所有占位符均已填充
 * @param builtInVars          内置变量清单(供前端 tip 展示)
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "模板渲染预览响应")
public record TemplatePreviewResponse(
        @Schema(description = "模板标识") Long sysMessageTemplateId,
        @Schema(description = "渲染后的标题") String title,
        @Schema(description = "渲染后的内容") String content,
        @Schema(description = "未填充的占位符") List<String> unresolvedParams,
        @Schema(description = "是否所有占位符均已填充") Boolean complete,
        @Schema(description = "内置变量清单") List<String> builtInVars) {
}
