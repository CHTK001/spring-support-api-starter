package com.chua.starter.soft.support.entity;

import lombok.Data;

@Data
public class SoftTarget {
    private Long softTargetId;
    private String targetName;
    private String targetCode;
    private String targetType;
    private String osType;
    private String architecture;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String privateKey;
    private String baseDirectory;
    private Boolean enabled;
    private String description;
    private String metadataJson;
}
