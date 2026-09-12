package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监控网关实体，定义采集器访问监控资产的中转网关信息。
 *
 * @since 2026-08-11
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ms_monitor_gateway")
@ApiModel(description = "监控网关")
@Schema(description = "监控网关")
public class MsMonitorGateway extends SysBase {

    /**
      * 网关主键标识，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    /** 标识 */
    private Long id;

    /**
    * 网关名称，用于前端展示
     */
    @TableField("gateway_name")
    @ApiModelProperty("网关名称")
    @Schema(description = "网关名称")
    /** Gateway名称 */
    private String gatewayName;

    /**
    * 关联的监控资产标识
     */
    @TableField("monitor_id")
    @ApiModelProperty("监控资产ID")
    @Schema(description = "监控资产ID")
    /** 监控id */
    private Long monitorId;

    /**
    * 网关主机地址，例如 192.168.1.20
     */
    @TableField("gateway_host")
    @ApiModelProperty("网关主机")
    @Schema(description = "网关主机")
    /** Gateway主机 */
    private String gatewayHost;

    /**
    * 网关端口，例如 2222
     */
    @TableField("gateway_port")
    @ApiModelProperty("网关端口")
    @Schema(description = "网关端口")
    /** Gateway端口 */
    private Integer gatewayPort;

    /**
    * 网关协议，例如 SSH/HTTPS
     */
    @TableField("protocol")
    @ApiModelProperty("协议")
    @Schema(description = "协议")
    /** 协议 */
    private String protocol;

    /**
    * 鉴权启用状态，1=启用，0=禁用
     */
    @TableField("auth_enabled")
    @ApiModelProperty("鉴权启用（0禁用/1启用）")
    @Schema(description = "鉴权启用（0禁用/1启用）")
    /** 认证是否启用 */
    private Integer authEnabled;

    /**
    * 网关运行状态，取值 活跃/STANDBY/错误
     */
    @TableField("gateway_status")
    @ApiModelProperty("网关状态（ACTIVE/STANDBY/ERROR）")
    @Schema(description = "网关状态")
    /** Gateway状态 */
    private String gatewayStatus;

    /**
    * 逻辑删除标记，0=未删除，1=已删除
     */
    @TableField("deleted")
    @TableLogic
    @ApiModelProperty("逻辑删除")
    @Schema(description = "逻辑删除")
    /** 删除标记 */
    private Integer deleted;
}
