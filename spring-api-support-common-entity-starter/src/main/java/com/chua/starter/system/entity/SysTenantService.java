package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户 - 服务
 */
@ApiModel(description = "租户 - 服务")
@Schema(description = "租户 - 服务")
@Data
@TableName(value = "sys_tenant_service")
public class SysTenantService {
    @TableId(value = "sys_tenant_service_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysTenantServiceId;

    /**
     * 租户ID
     */
    @TableField(value = "sys_tenant_id")
    @ApiModelProperty(value = "租户ID")
    @Schema(description = "租户ID")
    private Integer sysTenantId;

    /**
     * 服务ID
     */
    @TableField(value = "sys_service_id")
    @ApiModelProperty(value = "服务ID")
    @Schema(description = "服务ID")
    private Integer sysServiceId;

    /**
     * 到期时间
     */
    @TableField(value = "sys_tenant_service_valid_time")
    @ApiModelProperty(value = "到期时间")
    @Schema(description = "到期时间")
    private LocalDateTime sysTenantServiceValidTime;
}