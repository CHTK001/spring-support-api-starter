package com.chua.starter.monitor.pojo.server;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProxyConnectionInfo implements Serializable {

    private Integer proxyInstanceId;
    private String proxyType;
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String source;

    public boolean hasEndpoint() {
        return proxyHost != null && !proxyHost.isBlank() && proxyPort != null;
    }

    public boolean isGuacamoleProxy() {
        return proxyType != null && "GUACAMOLE".equalsIgnoreCase(proxyType);
    }
}
