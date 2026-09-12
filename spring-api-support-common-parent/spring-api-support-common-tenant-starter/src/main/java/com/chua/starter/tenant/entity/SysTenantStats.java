package com.chua.starter.tenant.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户统计信息
 *
   * @版本 1.0.0
 * @since 2024/12/20
 * @author CH
 */
@ApiModel(description = "租户统计信息")
@Schema(description = "租户统计信息")
@Data
public class SysTenantStats {

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
    * 已订阅服务的租户数
     */
    @ApiModelProperty(value = "已订阅服务的租户数")
    @Schema(description = "已订阅服务的租户数")
    /** Subscribed */
    private Long subscribed;
}
