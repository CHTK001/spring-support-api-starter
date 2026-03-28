package com.chua.starter.monitor.pojo.spider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 爬虫表状态
 *
 * @author CH
 * @since 2026/03/27
 */
@Data
@Schema(description = "爬虫数据库表状态")
public class SpiderTableStatus {

    @Schema(description = "表结构是否完整")
    private Boolean ready;

    @Schema(description = "表是否存在")
    private Map<String, Boolean> tables;

    @Schema(description = "缺失的表")
    private List<String> missingTables;

    @Schema(description = "缺失字段，key 为表名")
    private Map<String, List<String>> missingColumns;
}
