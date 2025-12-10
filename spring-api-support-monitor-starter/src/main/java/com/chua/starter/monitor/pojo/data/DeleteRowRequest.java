package com.chua.starter.monitor.pojo.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 删除行请求
 *
 * @author CH
 * @since 2025-12-03
 */
@Data
@ApiModel("删除行请求")
public class DeleteRowRequest {

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
}
