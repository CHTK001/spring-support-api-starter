package com.chua.starter.server.support.entity;

import lombok.Data;

@Data
public class ServerHost {
    private Long serverId;
    private String serverCode;
    private String serverName;
    private String serverType;
    private String osType;
    private String architecture;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String privateKey;
    private String baseDirectory;
    private String tags;
    private Boolean enabled;
    private String description;
    private String metadataJson;
}
