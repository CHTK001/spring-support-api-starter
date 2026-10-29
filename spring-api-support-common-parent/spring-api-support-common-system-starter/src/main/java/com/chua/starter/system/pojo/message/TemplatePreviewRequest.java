package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * 模板渲染预览请求
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "模板渲染预览请求")
public record TemplatePreviewRequest(
        /**
         * 模板标识
         */
        @Schema(description = "模板标识")
        Long sysMessageTemplateId,
        /**
         * 模板变量
         */
        @Schema(description = "模板变量")
        Map<String, Object> params
) {
}
