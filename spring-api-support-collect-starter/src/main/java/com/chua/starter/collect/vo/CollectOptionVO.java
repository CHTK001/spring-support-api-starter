package com.chua.starter.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选项出参，用于分类、数据源等下拉框。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@NoArgsConstructor
@Schema(description = "数据采集-选项出参")
public class CollectOptionVO {

    /**
     * 选项标识。
     */
    @Schema(description = "选项标识")
    private String value;

    /**
     * 选项名称。
     */
    @Schema(description = "选项名称")
    private String label;

    /**
     * 构造选项。
     *
     * @param value 标识
     * @param label 名称
     */
    public CollectOptionVO(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
