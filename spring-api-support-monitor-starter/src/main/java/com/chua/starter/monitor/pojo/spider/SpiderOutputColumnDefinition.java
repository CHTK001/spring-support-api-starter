package com.chua.starter.monitor.pojo.spider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 爬虫输出表字段定义
 *
 * @author CH
 * @since 2026/03/27
 */
@Data
@Schema(description = "爬虫输出表字段定义")
public class SpiderOutputColumnDefinition {

    @Schema(description = "字段名")
    private String name;

    @Schema(description = "字段类型，例如 VARCHAR、BIGINT、TEXT、DECIMAL")
    private String type;

    @Schema(description = "字段长度")
    private Integer length;

    @Schema(description = "小数位数")
    private Integer scale;

    @Schema(description = "是否可空")
    private Boolean nullable = true;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否主键")
    private Boolean primaryKey = false;

    @Schema(description = "是否自增")
    private Boolean autoIncrement = false;

    @Schema(description = "字段注释")
    private String comment;

    @Schema(description = "来源字段名")
    private String sourceField;

    @Schema(description = "来源 XPath")
    private String xpath;
}
