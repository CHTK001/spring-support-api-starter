package com.chua.starter.system.query;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "父级代码不能为空", groups = {SelectGroup.class})
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
