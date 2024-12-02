package com.chua.starter.service.query;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 城市查询
 * @author CH
 * @since 2024/12/2
 */
@Data
@Schema(description = "城市查询")
public class SysCityQuery {

    /**
     * 父级代码
     */
    @ApiModelProperty("父级代码")
    @Schema(description = "父级代码")
    private String parentCode;
    /**
     * 城市名称
     */
    @ApiModelProperty("城市名称;支持模糊查询")
    @Schema(description = "城市名称;支持模糊查询")
    private String cityName;

    /**
     * 返回类型
     */
    @ApiModelProperty("返回类型; TREE, LIST")
    @Schema(description = "返回类型; TREE, LIST")
    private String returnType;
}
