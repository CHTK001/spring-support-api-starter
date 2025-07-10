package com.chua.starter.monitor.starter.enums;

/**
 * 代理类型枚举
 *
 * @author CH
 * @since 2024/12/26
 */
public enum ProxyType {
    HTTP("HTTP", "HTTP代理"),
    SOCKS4("SOCKS4", "SOCKS4代理"),
    SOCKS5("SOCKS5", "SOCKS5代理"),
    SSH_TUNNEL("SSH_TUNNEL", "SSH隧道"),
    GUACAMOLE("GUACAMOLE", "Guacamole代理");

    private final String code;
    private final String desc;

    ProxyType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProxyType fromCode(String code) {
        for (ProxyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return HTTP;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
