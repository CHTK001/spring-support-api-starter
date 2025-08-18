package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Arthas Tunnel 独立配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_arthas_tunnel_config")
@Schema(description = "Arthas Tunnel 独立配置")
public class MonitorArthasTunnelConfig {

    public static final MonitorArthasTunnelConfig EMPTY = new MonitorArthasTunnelConfig();
    /** 主键ID */
    @TableId(value = "monitor_arthas_tunnel_config_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer monitorArthasTunnelConfigId;

    /** 服务器ID */
    @Schema(description = "服务器ID")
    @TableField("monitor_arthas_tunnel_config_server_id")
    private String monitorArthasTunnelConfigServerId;

    /** Tunnel 地址（例如 ws://host:port/ws） */
    @Schema(description = "Tunnel 地址（例如 ws://host:port/ws）")
    @TableField("monitor_arthas_tunnel_config_address")
    private String monitorArthasTunnelConfigAddress;
    /** HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas */
    @Schema(description = "HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas")
    @TableField("monitor_arthas_tunnel_config_http")
    private String arthasTunnelConfigHttp;
    /** 认证用户名（可选） */
    @Schema(description = "认证用户名（可选）")
    @TableField("monitor_arthas_tunnel_config_username")
    private String monitorArthasTunnelConfigUsername;

    /** 认证密码（可选） */
    @Schema(description = "认证密码（可选）")
    @TableField("monitor_arthas_tunnel_config_password")
    private String monitorArthasTunnelConfigPassword;
}

