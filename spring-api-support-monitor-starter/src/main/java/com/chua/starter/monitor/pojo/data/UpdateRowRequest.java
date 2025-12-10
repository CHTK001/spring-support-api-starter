package com.chua.starter.monitor.pojo.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新行请求
 *
 * @author CH
 * @since 2025-12-03
 */
@Data
@ApiModel("更新行请求")
public class UpdateRowRequest {

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名", required = true)
    private String tableName;

    /**
     * 主键列名
     */
    @ApiModelProperty(value = "主键列名", required = true)
    private String primaryKey;

    /**
     * 主键值
     */
    @ApiModelProperty(value = "主键值", required = true)
    private Object primaryValue;

    /**
     * 要更新的列名
     */
    @ApiModelProperty(value = "要更新的列名", required = true)
    private String columnName;

    /**
     * 新值
     */
    @ApiModelProperty(value = "新值")
    private Object newValue;
}
