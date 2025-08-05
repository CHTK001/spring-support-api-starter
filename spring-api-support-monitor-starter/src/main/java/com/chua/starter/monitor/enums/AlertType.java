package com.chua.starter.monitor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 告警类型枚举
 *
 * @author CH
 * @since 2024/12/27
 */
@Getter
@AllArgsConstructor
public enum AlertType {

    /**
     * CPU使用率告警
     */
    CPU("CPU", "CPU使用率"),

    /**
     * 内存使用率告警
     */
    MEMORY("MEMORY", "内存使用率"),

    /**
     * 磁盘使用率告警
     */
    DISK("DISK", "磁盘使用率"),

    /**
     * 网络使用率告警
     */
    NETWORK("NETWORK", "网络使用率"),

    /**
     * 响应时间告警
     */
    RESPONSE_TIME("RESPONSE_TIME", "响应时间"),

    /**
     * 连接状态告警
     */
    CONNECTION("CONNECTION", "连接状态"),

    /**
     * 服务状态告警
     */
    SERVICE("SERVICE", "服务状态"),

    /**
     * 进程状态告警
     */
    PROCESS("PROCESS", "进程状态"),

    /**
     * 端口状态告警
     */
    PORT("PORT", "端口状态"),

    /**
     * 文件系统告警
     */
    FILESYSTEM("FILESYSTEM", "文件系统"),

    /**
     * 温度告警
     */
    TEMPERATURE("TEMPERATURE", "温度"),

    /**
     * 负载告警
     */
    LOAD("LOAD", "系统负载"),

    /**
     * 自定义告警
     */
    CUSTOM("CUSTOM", "自定义");

    /**
     * 告警类型代码
     */
    private final String code;

    /**
     * 告警类型描述
     */
    private final String description;

    /**
     * 根据代码获取告警类型
     *
     * @param code 代码
     * @return 告警类型
     */
    public static AlertType fromCode(String code) {
        for (AlertType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
