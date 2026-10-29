package com.chua.starter.tenant.server.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 租户绑定服务请求
 *
 * @since 2024/12/06
 * @author CH
 */
@ApiModel(description = "租户绑定服务请求")
@Schema(description = "租户绑定服务请求")
public record SysTenantBindServiceV1Request(
        /**
         * 租户标识
         */
        @ApiModelProperty(value = "租户ID", required = true)
        @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        /**
         * systenantid
         */
        String sysTenantId,
        /**
         * 服务绑定项列表
         */
        @ApiModelProperty(value = "服务绑定项列表")
        @Schema(description = "服务绑定项列表")
        /**
         * 服务binditems
         */
        List<ServiceBindItem> serviceBindItems
) {

    /**
     * 服务绑定项
     * @author CH
     * @since 4.0.0
     */
    @ApiModel(description = "服务绑定项")
    @Schema(description = "服务绑定项")
    public record ServiceBindItem(
            /**
             * 服务标识
             */
            @ApiModelProperty(value = "服务ID", required = true)
            @Schema(description = "服务ID", requiredMode = Schema.RequiredMode.REQUIRED)
            /**
             * SYS服务标识
             */
            Integer sysServiceId,
            /**
             * 到期时间
             */
            @ApiModelProperty(value = "到期时间")
            @Schema(description = "到期时间")
            /**
             * 是否有效时间
             */
            LocalDate validTime
    ) {
    }
}
