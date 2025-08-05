package com.chua.starter.monitor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 告警状态枚举
 *
 * @author CH
 * @since 2024/12/27
 */
@Getter
@AllArgsConstructor
public enum AlertStatus {

    /**
     * 已触发 - 告警刚刚被触发
     */
    TRIGGERED("TRIGGERED", "已触发"),

    /**
     * 活跃 - 告警持续存在
     */
    ACTIVE("ACTIVE", "活跃"),

    /**
     * 已恢复 - 告警条件已恢复正常
     */
    RECOVERED("RECOVERED", "已恢复"),

    /**
     * 已忽略 - 告警被手动忽略
     */
    IGNORED("IGNORED", "已忽略"),

    /**
     * 静默中 - 告警在静默期内
     */
    SILENCED("SILENCED", "静默中"),

    /**
     * 已过期 - 告警超时自动恢复
     */
    EXPIRED("EXPIRED", "已过期"),

    /**
     * 已取消 - 告警被手动取消
     */
    CANCELLED("CANCELLED", "已取消");

    /**
     * 告警状态代码
     */
    private final String code;

    /**
     * 告警状态描述
     */
    private final String description;

    /**
     * 根据代码获取告警状态
     *
     * @param code 代码
     * @return 告警状态
     */
    public static AlertStatus fromCode(String code) {
        for (AlertStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return TRIGGERED;
    }

    /**
     * 是否为活跃状态（TRIGGERED或ACTIVE）
     *
     * @return 是否为活跃状态
     */
    public boolean isActive() {
        return this == TRIGGERED || this == ACTIVE;
    }

    /**
     * 是否为结束状态（RECOVERED、IGNORED、EXPIRED、CANCELLED）
     *
     * @return 是否为结束状态
     */
    public boolean isFinished() {
        return this == RECOVERED || this == IGNORED || this == EXPIRED || this == CANCELLED;
    }

    /**
     * 是否可以恢复
     *
     * @return 是否可以恢复
     */
    public boolean canRecover() {
        return this == TRIGGERED || this == ACTIVE || this == SILENCED;
    }

    /**
     * 是否可以忽略
     *
     * @return 是否可以忽略
     */
    public boolean canIgnore() {
        return this == TRIGGERED || this == ACTIVE || this == SILENCED;
    }

    /**
     * 是否可以静默
     *
     * @return 是否可以静默
     */
    public boolean canSilence() {
        return this == TRIGGERED || this == ACTIVE;
    }
}
