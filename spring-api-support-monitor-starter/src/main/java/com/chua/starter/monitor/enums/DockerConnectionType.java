package com.chua.starter.monitor.enums;

/**
 * Docker连接方式枚举
 *
 * @author CH
 * @since 2024/12/26
 */
public enum DockerConnectionType {
    SHELL("SHELL", "命令行"),
    TCP("TCP", "TCP连接"),
    SOCKET("SOCKET", "Unix Socket");

    private final String code;
    private final String desc;

    DockerConnectionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DockerConnectionType fromCode(String code) {
        for (DockerConnectionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return SHELL;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
