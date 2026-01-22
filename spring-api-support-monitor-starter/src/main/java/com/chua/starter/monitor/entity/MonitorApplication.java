package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.network.discovery.Discovery;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ApiModel(description="monitor_application")
@Schema
@Data
@TableName(value = "monitor_application")
public class MonitorApplication extends SysBase implements Serializable {
    @TableId(value = "monitor_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer monitorId;

    /**
     * 监控名称
     */
    @TableField(value = "monitor_name")
    @ApiModelProperty(value="监控名称")
    @Schema(description="监控名称")
    @Size(max = 255,message = "监控名称最大长度要小于 255")
    private String monitorName;

    /**
     * 所属应用
     */
    @TableField(value = "monitor_application_name")
    @ApiModelProperty(value="所属应用")
    @Schema(description="所属应用")
    @Size(max = 255,message = "所属应用最大长度要小于 255")
    private String monitorApplicationName;

    /**
     * Prometheus地址
     */
    @TableField(value = "monitor_prometheus_address")
    @ApiModelProperty(value = "Prometheus地址")
    @Schema(description = "Prometheus地址")
    private String monitorPrometheusAddress;
    /**
     * 绑定的应用
     */
    @TableField(exist = false)
    @ApiModelProperty(value="绑定的应用")
    @Schema(description="绑定的应用")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Discovery> monitorRequests;

}