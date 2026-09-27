package com.chua.starter.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据表列元数据出参，供流程设计器配置字段映射时选择列。
 *
 * <p>数据来自 {@link java.sql.DatabaseMetaData#getColumns}，因此不依赖任何具体数据库方言；
 * 取不到的注释、精度等一律给空值或默认值，不让元数据缺失变成一次失败的配置。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@NoArgsConstructor
@Schema(description = "数据采集-表列元数据出参")
public class CollectColumnVO {

    /**
     * 列名，写库时的目标列。
     */
    @Schema(description = "列名")
    private String name;

    /**
     * 列注释，取不到时为空。
     */
    @Schema(description = "列注释")
    private String label;

    /**
     * 列序号，从 1 开始，按表内定义顺序排列。
     */
    @Schema(description = "列序号")
    private Integer position;

    /**
     * 数据库原生类型名，例如 {@code varchar(255)}、{@code bigint}。
     */
    @Schema(description = "数据库原生类型")
    private String typeName;

    /**
     * 类型精度，例如字符串长度、数字总位数。
     */
    @Schema(description = "类型精度")
    private Integer size;

    /**
     * 是否可空。
     */
    @Schema(description = "是否可空")
    private Boolean nullable;

    /**
     * 是否主键列，供界面提示「去重/覆盖」可用的自然键。
     */
    @Schema(description = "是否主键列")
    private Boolean primaryKey;

    /**
     * 构造列元数据。
     *
     * @param name     列名
     * @param label    列注释
     * @param position 列序号
     * @param typeName 原生类型
     * @param size     精度
     * @param nullable 是否可空
     */
    public CollectColumnVO(String name, String label, Integer position,
                            String typeName, Integer size, Boolean nullable) {
        this.name = name;
        this.label = label;
        this.position = position;
        this.typeName = typeName;
        this.size = size;
        this.nullable = nullable;
        this.primaryKey = Boolean.FALSE;
    }
}
