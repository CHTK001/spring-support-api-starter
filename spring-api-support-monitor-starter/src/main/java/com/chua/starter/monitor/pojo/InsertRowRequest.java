package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * 插入行请求
 *
 * @author CH
 * @since 2025-12-03
 */
@Data
@ApiModel("插入行请求")
public class InsertRowRequest {

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名", required = true)
    private String tableName;

    /**
     * 行数据
     */
    @ApiModelProperty(value = "行数据，key为列名，value为值", required = true)
    private Map<String, Object> data;
}
