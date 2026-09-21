package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽取字段声明。
 *
 * <p>抽取节点的每个输出字段一条，描述"从哪个来源、用什么解析器、取哪一段"。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "抽取字段声明")
public class CollectExtractFieldDTO {

    /**
     * 字段名，结果 payload 的 key。
     */
    @Schema(description = "字段名")
    private String name;

    /**
     * 解析器类型：CSS/REGEX/JSON/TEXT/HTML，留空取节点默认。
     */
    @Schema(description = "解析器类型 CSS/REGEX/JSON/TEXT/HTML")
    private String parserType;

    /**
     * 表达式：CSS 选择器、正则、JSON 路径或文本模板。
     */
    @Schema(description = "表达式")
    private String expression;

    /**
     * 取值方式：text/attr/html/outer，仅 CSS 生效。
     */
    @Schema(description = "取值方式 text/attr/html/outer")
    private String attribute;

    /**
     * 当 attribute 为 attr 时的属性名。
     */
    @Schema(description = "属性名")
    private String attrName;

    /**
     * 正则捕获组序号，0 表示整段匹配。
     */
    @Schema(description = "正则捕获组序号")
    private Integer group;

    /**
     * 未命中时的默认值，支持 ${} 表达式。
     */
    @Schema(description = "默认值")
    private String defaultValue;
}
