package com.chua.starter.monitor.starter.dto;

import com.chua.starter.monitor.starter.entity.MonitorProxy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务器代理DTO（适配前端期望的数据格式）
 *
 * @author CH
 * @since 2024/12/27
 */
@Data
@Schema(description = "服务器代理DTO")
public class ServerProxyDto {

    @Schema(description = "代理ID")
    private Integer monitorSysGenServerProxyId;

    @Schema(description = "代理类型")
    private String monitorSysGenServerProxyType;

    @Schema(description = "代理主机地址")
    private String monitorSysGenServerProxyHost;

    @Schema(description = "代理端口")
    private Integer monitorSysGenServerProxyPort;

    @Schema(description = "是否启用")
    private Integer monitorSysGenServerProxyEnabled;

    @Schema(description = "代理状态")
    private Integer monitorSysGenServerProxyStatus;

    @Schema(description = "最后连接时间")
    private LocalDateTime monitorSysGenServerProxyLastConnectTime;

    @Schema(description = "连接失败原因")
    private String monitorSysGenServerProxyConnectionError;

    @Schema(description = "备注")
    private String monitorSysGenServerProxyRemark;

    @Schema(description = "代理名称")
    private String proxyName;

    @Schema(description = "代理描述")
    private String proxyDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 从 MonitorProxy 转换为 ServerProxyDto
     *
     * @param proxy MonitorProxy 实体
     * @return ServerProxyDto
     */
    public static ServerProxyDto fromMonitorProxy(MonitorProxy proxy) {
        if (proxy == null) {
            return null;
        }

        ServerProxyDto dto = new ServerProxyDto();
        dto.setMonitorSysGenServerProxyId(proxy.getProxyId());
        dto.setMonitorSysGenServerProxyType(proxy.getProxyProtocol());
        dto.setMonitorSysGenServerProxyHost(proxy.getProxyHost());
        dto.setMonitorSysGenServerProxyPort(proxy.getProxyPort());
        dto.setMonitorSysGenServerProxyEnabled(proxy.getProxyStatus());
        dto.setMonitorSysGenServerProxyStatus(proxy.getProxyStatus());
        dto.setMonitorSysGenServerProxyRemark(proxy.getProxyDesc());
        dto.setProxyName(proxy.getProxyName());
        dto.setProxyDesc(proxy.getProxyDesc());

        return dto;
    }

    /**
     * 转换为 MonitorProxy
     *
     * @return MonitorProxy 实体
     */
    public MonitorProxy toMonitorProxy() {
        MonitorProxy proxy = new MonitorProxy();
        proxy.setProxyId(this.monitorSysGenServerProxyId);
        proxy.setProxyProtocol(this.monitorSysGenServerProxyType);
        proxy.setProxyHost(this.monitorSysGenServerProxyHost);
        proxy.setProxyPort(this.monitorSysGenServerProxyPort);
        proxy.setProxyStatus(this.monitorSysGenServerProxyEnabled);
        proxy.setProxyName(this.proxyName);
        proxy.setProxyDesc(this.proxyDesc);

        return proxy;
    }
}
