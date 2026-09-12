package com.chua.starter.tenant.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务模块统计信息
 *
   * @版本 1.0.0
 * @since 2024/12/20
 * @author CH
 */
@ApiModel(description = "服务模块统计信息")
@Schema(description = "服务模块统计信息")
@Data
public class SysServiceModuleStats {

    /**
     * 总数
     */
    @ApiModelProperty(value = "总数")
    @Schema(description = "总数")
    /** 总数 */
    private Long total;

    /**
    * 启用数
     */
    @ApiModelProperty(value = "启用数")
    @Schema(description = "启用数")
    /** 是否启用 */
    private Long enabled;

    /**
    * 禁用数
     */
    @ApiModelProperty(value = "禁用数")
    @Schema(description = "禁用数")
    /** 已禁用 */
    private Long disabled;

    /**
    * API类型模块数
     */
    @ApiModelProperty(value = "API类型模块数")
    @Schema(description = "API类型模块数")
    /** API数量 */
    private Long apiCount;

    /**
    * 服务类型模块数
     */
    @ApiModelProperty(value = "服务类型模块数")
    @Schema(description = "服务类型模块数")
    /** 服务数量 */
    private Long serviceCount;
}
