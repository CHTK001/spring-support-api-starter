package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 名称值对。
 *
 * <p>流程节点的同名可重复参数（请求头、查询参数、Cookie、变量赋值）统一用它承载，
 * 避免以 Map 作为接口出入参。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@NoArgsConstructor
@Schema(description = "名称值对")
public class CollectParamDTO {

    /**
     * 参数名。
     */
    @Schema(description = "参数名")
    private String name;

    /**
     * 参数值，支持 ${} 表达式。
     */
    @Schema(description = "参数值")
    private String value;

    /**
     * 说明或表头名称，仅用于界面上列提示。
     */
    @Schema(description = "说明")
    private String description;

    /**
     * 构造名称值对。
     *
     * @param name  参数名
     * @param value 参数值
     */
    public CollectParamDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
