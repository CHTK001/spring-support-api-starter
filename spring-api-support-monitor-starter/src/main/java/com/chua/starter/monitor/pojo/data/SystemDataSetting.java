package com.chua.starter.monitor.pojo.data;

import lombok.Data;

/**
 * 系统数据源配置（与前端 SystemDataSetting 字段对齐）
 */
@Data
public class SystemDataSetting {
    private Long systemDataSettingId;
    private String systemDataSettingName;
    /** SPI 类型: JDBC/REDIS/... */
    private String systemDataSettingType;

    /** 完整连接串（优先） */
    private String systemDataSettingServer;
    private String systemDataSettingHost;
    private Integer systemDataSettingPort;
    private String systemDataSettingUsername;
    private String systemDataSettingPassword;
    private String systemDataSettingAuthType;
    private String systemDataSettingIcon;

    /** 控制台类型：TABLE/GRAPH/FILE（为空视为禁用） */
    private String systemDataSettingConsoleType;

    /** 图表类型 */
    private String systemDataSettingChartType;

    /** 协议（如 jdbc:mysql、redis 等） */
    private String systemDataSettingProtocol;

    private Integer systemDataSettingTimeoutMs;

    /** 模式：REMOTE/FILE */
    private String systemDataSettingMode;

    private Boolean systemDataSettingEnabled;

    private String systemDataSettingConfig;

    /** JDBC 驱动类名（仅数据库类型使用） */
    private String systemDataSettingDriverClass;
    /** JDBC 驱动文件路径（仅数据库类型使用） */
    private String systemDataSettingDriverPath;

    /** 远程图片地址 */
    private String systemDataSettingImageUrl;
}

