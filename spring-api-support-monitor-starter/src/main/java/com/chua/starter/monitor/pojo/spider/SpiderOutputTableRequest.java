package com.chua.starter.monitor.pojo.spider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 爬虫输出表请求
 *
 * @author CH
 * @since 2026/03/27
 */
@Data
@Schema(description = "爬虫输出表请求")
public class SpiderOutputTableRequest {

    @Schema(description = "数据源配置ID，空表示当前监控服务默认数据库")
    private Integer dataSourceSettingId;

    @Schema(description = "表注释")
    private String tableComment;

    @Schema(description = "字段定义列表")
    private List<SpiderOutputColumnDefinition> columns;
}
