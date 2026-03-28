package com.chua.starter.monitor.pojo.spider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 爬虫数据库数据源选项
 *
 * @author CH
 * @since 2026/03/27
 */
@Data
@Schema(description = "爬虫数据库数据源选项")
public class SpiderDataSourceOption {

    @Schema(description = "是否为内置数据库")
    private Boolean builtin;

    @Schema(description = "数据源配置ID")
    private Integer settingId;

    @Schema(description = "显示名称")
    private String name;

    @Schema(description = "数据源类型")
    private String type;

    @Schema(description = "连接地址")
    private String url;

    @Schema(description = "数据库名称")
    private String database;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
