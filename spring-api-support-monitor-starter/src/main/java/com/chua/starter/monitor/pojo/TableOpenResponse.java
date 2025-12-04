package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 打开表响应
 *
 * @author CH
 * @since 2025-11-30
 */
@Data
public class TableOpenResponse {
    /**
     * 列信息列表
     */
    @Schema(description = "列信息列表")
    private List<ColumnInfo> columns;
    
    /**
     * 数据行
     */
    @Schema(description = "数据行")
    private List<Map<String, Object>> rows;
    
    /**
     * 列注释，key=列名
     */
    @Schema(description = "列注释，key=列名")
    private Map<String, String> columnComments;
    
    /**
     * 表注释
     */
    @Schema(description = "表注释")
    private String tableComment;
}


