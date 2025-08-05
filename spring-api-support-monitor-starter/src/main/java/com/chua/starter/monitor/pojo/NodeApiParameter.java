package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 节点API参数
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Schema(description = "节点API参数")
public class NodeApiParameter {

    @Schema(description = "参数名称")
    private String name;

    @Schema(description = "参数位置", allowableValues = {"path", "query", "header", "body"})
    private String in;

    @Schema(description = "是否必需")
    private boolean required;

    @Schema(description = "参数类型")
    private String type;

    @Schema(description = "参数描述")
    private String description;

    @Schema(description = "默认值")
    private Object defaultValue;

    @Schema(description = "示例值")
    private Object example;

    public NodeApiParameter() {}

    public NodeApiParameter(String name, String in, boolean required, String type) {
        this.name = name;
        this.in = in;
        this.required = required;
        this.type = type;
    }

    public NodeApiParameter(String name, String in, boolean required, String type, String description) {
        this.name = name;
        this.in = in;
        this.required = required;
        this.type = type;
        this.description = description;
    }
}
