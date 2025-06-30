package com.chua.starter.monitor.starter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 告警级别枚举
 *
 * @author CH
 * @since 2024/12/27
 */
@Getter
@AllArgsConstructor
public enum AlertLevel {

    /**
     * 信息级别
     */
    INFO("INFO", "信息", 1),

    /**
     * 警告级别
     */
    WARNING("WARNING", "警告", 2),

    /**
     * 错误级别
     */
    ERROR("ERROR", "错误", 3),

    /**
     * 严重级别
     */
    CRITICAL("CRITICAL", "严重", 4);

    /**
     * 告警级别代码
     */
    private final String code;

    /**
     * 告警级别描述
     */
    private final String description;

    /**
     * 告警级别优先级（数字越大优先级越高）
     */
    private final Integer priority;

    /**
     * 根据代码获取告警级别
     *
     * @param code 代码
     * @return 告警级别
     */
    public static AlertLevel fromCode(String code) {
        for (AlertLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return WARNING;
    }

    /**
     * 根据优先级获取告警级别
     *
     * @param priority 优先级
     * @return 告警级别
     */
    public static AlertLevel fromPriority(Integer priority) {
        for (AlertLevel level : values()) {
            if (level.priority.equals(priority)) {
                return level;
            }
        }
        return WARNING;
    }



    /**
     * 是否为高级别告警（ERROR或CRITICAL）
     *
     * @return 是否为高级别告警
     */
    public boolean isHighLevel() {
        return this == ERROR || this == CRITICAL;
    }

    /**
     * 是否为严重告警（CRITICAL）
     *
     * @return 是否为严重告警
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }
}
