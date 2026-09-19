package com.chua.starter.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 租户 - 服务关联表
 *
 * @版本 1.0.0
 * @since 2024/12/06
 * @author CH
 */
@ApiModel(description = "租户 - 服务")
@Schema(description = "租户 - 服务")
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "sys_tenant_service")
public class SysTenantService extends SysBase {

    /**
     * 主键标识
     */
    @TableId(value = "sys_tenant_service_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    /** systenant服务标识 */
    private Integer sysTenantServiceId;

    /**
     * 租户标识
     */
    @TableField(value = "sys_tenant_id")
    @ApiModelProperty(value = "租户ID")
    @Schema(description = "租户ID")
    /** systenantid */
    private String sysTenantId;

    /**
     * 服务标识
     */
    @TableField(value = "sys_service_id")
    @ApiModelProperty(value = "服务ID")
    @Schema(description = "服务ID")
    /** SYS服务标识 */
    private Integer sysServiceId;

    /**
     * 到期时间
     */
    @TableField(value = "sys_tenant_service_valid_time")
    @ApiModelProperty(value = "到期时间")
    @Schema(description = "到期时间")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    /** systenant服务是否有效时间 */
    private LocalDate sysTenantServiceValidTime;
}
