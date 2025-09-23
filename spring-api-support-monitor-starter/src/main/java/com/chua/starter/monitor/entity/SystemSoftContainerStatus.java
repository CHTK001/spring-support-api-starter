package com.chua.starter.monitor.entity;

/**
 * 系统软件容器状态枚举
 * <p>
 * 定义了容器可能的各种状态，用于跟踪容器的生命周期。
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/09/22
 */
public enum SystemSoftContainerStatus {

    /**
     * 已创建
     * <p>
     * 容器已成功创建，但尚未启动。
     */
    CREATED("CREATED", "已创建"),

    /**
     * 创建中
     * <p>
     * 正在创建容器的过程中。
     */
    CREATING("CREATING", "创建中"),

    /**
     * 创建失败
     * <p>
     * 容器创建过程中发生错误。
     */
    CREATE_FAILED("CREATE_FAILED", "创建失败"),

    /**
     * 运行中
     * <p>
     * 容器正在运行。
     */
    RUNNING("RUNNING", "运行中"),

    /**
     * 启动中
     * <p>
     * 容器正在启动过程中。
     */
    STARTING("STARTING", "启动中"),

    /**
     * 启动失败
     * <p>
     * 容器启动过程中发生错误。
     */
    START_FAILED("START_FAILED", "启动失败"),

    /**
     * 已暂停
     * <p>
     * 容器已被暂停。
     */
    PAUSED("PAUSED", "已暂停"),

    /**
     * 暂停中
     * <p>
     * 容器正在暂停过程中。
     */
    PAUSING("PAUSING", "暂停中"),

    /**
     * 恢复中
     * <p>
     * 容器正在从暂停状态恢复过程中。
     */
    RESUMING("RESUMING", "恢复中"),

    /**
     * 重启中
     * <p>
     * 容器正在重启过程中。
     */
    RESTARTING("RESTARTING", "重启中"),

    /**
     * 重启失败
     * <p>
     * 容器重启过程中发生错误。
     */
    RESTART_FAILED("RESTART_FAILED", "重启失败"),

    /**
     * 停止中
     * <p>
     * 容器正在停止过程中。
     */
    STOPPING("STOPPING", "停止中"),

    /**
     * 停止失败
     * <p>
     * 容器停止过程中发生错误。
     */
    STOP_FAILED("STOP_FAILED", "停止失败"),

    /**
     * 已停止
     * <p>
     * 容器已停止运行。
     */
    STOPPED("STOPPED", "已停止"),

    /**
     * 删除中
     * <p>
     * 容器正在被删除过程中。
     */
    DELETING("DELETING", "删除中"),

    /**
     * 删除失败
     * <p>
     * 容器删除过程中发生错误。
     */
    DELETE_FAILED("DELETE_FAILED", "删除失败"),

    /**
     * 已退出
     * <p>
     * 容器已正常退出。
     */
    EXITED("EXITED", "已退出"),

    /**
     * 已死亡
     * <p>
     * 容器已异常终止。
     */
    DEAD("DEAD", "已死亡");

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code        状态代码
     * @param description 状态描述
     */
    SystemSoftContainerStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态代码获取枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static SystemSoftContainerStatus fromCode(String code) {
        for (SystemSoftContainerStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取状态代码
     *
     * @return 状态代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}