package com.chua.starter.monitor.pojo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段信息
 *
 * @author CH
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段信息")
public class ColumnInfo {

    /**
     * 字段名
     */
    @Schema(description = "字段名")
    private String name;

    /**
     * 数据类型
     */
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 完整类型（包含长度等）
     */
    @Schema(description = "完整类型")
    private String fullType;

    /**
     * 长度
     */
    @Schema(description = "长度")
    private Integer length;

    /**
     * 精度
     */
    @Schema(description = "精度")
    private Integer precision;

    /**
     * 小数位数
     */
    @Schema(description = "小数位数")
    private Integer scale;

    /**
     * 是否可空
     */
    @Schema(description = "是否可空")
    private Boolean nullable;

    /**
     * 是否主键
     */
    @Schema(description = "是否主键")
    private Boolean primaryKey;

    /**
     * 是否自增
     */
    @Schema(description = "是否自增")
    private Boolean autoIncrement;

    /**
     * 默认值
     */
    @Schema(description = "默认值")
    private String defaultValue;

    /**
     * 注释
     */
    @Schema(description = "注释")
    private String comment;

    /**
     * 字段位置（从1开始）
     */
    @Schema(description = "字段位置")
    private Integer ordinalPosition;
}
