package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 打开表响应
 */
@Data
public class TableOpenResponse {
    @Schema(description = "列名列表")
    private List<String> columns;
    @Schema(description = "数据行")
    private List<Map<String, Object>> rows;
    @Schema(description = "列注释，key=列名")
    private Map<String, String> columnComments;
    @Schema(description = "表注释")
    private String tableComment;
}


