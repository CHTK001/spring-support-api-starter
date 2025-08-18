package com.chua.starter.monitor.arthas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Arthas Tunnel 配置
 * - 地址与凭证信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Arthas Tunnel 配置")
public class ArthasTunnelConfig {

    /** Tunnel 服务器地址，例如：ws://arthas-tunnel:7777/ws */
    @Schema(description = "Tunnel 服务器地址，例如：ws://arthas-tunnel:7777/ws")
    private String arthasTunnelConfigAddress;
    /** HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas */
    @Schema(description = "HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas")
    private String arthasTunnelConfigHttp;

    /** 认证用户名（可选） */
    @Schema(description = "认证用户名（可选）")
    private String arthasTunnelConfigUsername;

    /** 认证密码（可选） */
    @Schema(description = "认证密码（可选）")
    private String arthasTunnelConfigPassword;
}

