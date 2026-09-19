package com.chua.starter.tenant.server.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 租户绑定服务请求
 *
 * @since 2024/12/06
 * @author CH
 */
@Data
@ApiModel(description = "租户绑定服务请求")
@Schema(description = "租户绑定服务请求")
public class SysTenantBindServiceV1Request {

    /**
     * 租户标识
     */
    @ApiModelProperty(value = "租户ID", required = true)
    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    /**
     * systenantid
    */
    private String sysTenantId;

    /**
     * 服务绑定项列表
     */
    @ApiModelProperty(value = "服务绑定项列表")
    @Schema(description = "服务绑定项列表")
    /**
     * 服务binditems
    */
    private List<ServiceBindItem> serviceBindItems;

    /**
     * 服务绑定项
     * @author CH
     * @since 4.0.0
     */
    @Data
    @ApiModel(description = "服务绑定项")
    @Schema(description = "服务绑定项")
    public static class ServiceBindItem {

        /**
         * 服务标识
         */
        @ApiModelProperty(value = "服务ID", required = true)
        @Schema(description = "服务ID", requiredMode = Schema.RequiredMode.REQUIRED)
        /**
         * SYS服务标识
        */
        private Integer sysServiceId;

        /**
         * 到期时间
         */
        @ApiModelProperty(value = "到期时间")
        @Schema(description = "到期时间")
        /**
         * 是否有效时间
        */
        private LocalDate validTime;
    }
}
