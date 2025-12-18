package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SkyWalking 配置信息
 *
 * @author CH
 * @since 2024-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_skywalking_config")
@ApiModel(description = "SkyWalking 配置信息")
@Schema(description = "SkyWalking 配置信息")
public class MonitorSkywalkingConfig extends SysBase {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "skywalking_config_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Integer skywalkingConfigId;

    /** 配置名称 */
    @TableField("skywalking_config_name")
    @ApiModelProperty("配置名称")
    @Schema(description = "配置名称")
    private String skywalkingConfigName;

    /** 服务地址 */
    @TableField("skywalking_config_host")
    @ApiModelProperty("服务地址")
    @Schema(description = "服务地址")
    private String skywalkingConfigHost;

    /** 端口 */
    @TableField("skywalking_config_port")
    @ApiModelProperty("端口")
    @Schema(description = "端口")
    private Integer skywalkingConfigPort;

    /** 用户名（可选） */
    @TableField("skywalking_config_username")
    @ApiModelProperty("用户名")
    @Schema(description = "用户名")
    private String skywalkingConfigUsername;

    /** 密码（可选） */
    @TableField("skywalking_config_password")
    @ApiModelProperty("密码")
    @Schema(description = "密码")
    private String skywalkingConfigPassword;

    /** 状态 0:禁用 1:启用 */
    @TableField("skywalking_config_status")
    @ApiModelProperty("状态 0:禁用 1:启用")
    @Schema(description = "状态 0:禁用 1:启用")
    private Integer skywalkingConfigStatus;

    /** 描述 */
    @TableField("skywalking_config_desc")
    @ApiModelProperty("描述")
    @Schema(description = "描述")
    private String skywalkingConfigDesc;

    /** 是否使用HTTPS */
    @TableField("skywalking_config_use_https")
    @ApiModelProperty("是否使用HTTPS 0:否 1:是")
    @Schema(description = "是否使用HTTPS 0:否 1:是")
    private Integer skywalkingConfigUseHttps;

    /**
     * 获取 GraphQL 端点 URL
     */
    public String getGraphqlUrl() {
        String protocol = (skywalkingConfigUseHttps != null && skywalkingConfigUseHttps == 1) ? "https" : "http";
        return String.format("%s://%s:%d/graphql", protocol, skywalkingConfigHost, skywalkingConfigPort);
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return skywalkingConfigStatus != null && skywalkingConfigStatus == 1;
    }
}
