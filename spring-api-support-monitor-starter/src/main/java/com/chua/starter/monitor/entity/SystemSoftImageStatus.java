package com.chua.starter.monitor.entity;

import lombok.Getter;

/**
 * 系统软件镜像状态枚举
 * <p>
 * 定义了系统软件镜像的完整生命周期状态，包括：
 * <ul>
 *   <li>镜像拉取相关状态：AVAILABLE, PULLING, PULL_FAILED</li>
 *   <li>容器启动相关状态：STARTING, START_FAILED, RUNNING</li>
 *   <li>容器停止相关状态：STOPPING, STOP_FAILED, STOPPED</li>
 *   <li>容器重启相关状态：RESTARTING, RESTART_FAILED</li>
 *   <li>镜像删除相关状态：DELETING, DELETE_FAILED</li>
 *   <li>异常状态：ABNORMAL</li>
 * </ul>
 *
 * <p>每个状态都有对应的code和description，便于在数据库中存储和在前端显示。
 *
 * @author CH
 * @since 2025/01/15
 */
@Getter
public enum SystemSoftImageStatus {
    /**
     * 可用状态
     * <p>
     * 镜像已成功拉取并可正常使用，或者容器正在正常运行。
     */
    AVAILABLE("available", "可用"),

    /**
     * 拉取中
     * <p>
     * 正在从远程仓库拉取镜像，这是一个进行中的操作状态。
     */
    PULLING("pulling", "拉取中"),

    /**
     * 拉取失败
     * <p>
     * 镜像拉取操作失败，可能由于网络问题、认证失败或镜像不存在等原因。
     */
    PULL_FAILED("pull_failed", "拉取失败"),

    /**
     * 启动中
     * <p>
     * 正在启动基于该镜像的容器，这是一个进行中的操作状态。
     */
    STARTING("starting", "启动中"),

    /**
     * 启动失败
     * <p>
     * 容器启动操作失败，可能由于配置错误、端口冲突或资源不足等原因。
     */
    START_FAILED("start_failed", "启动失败"),

    /**
     * 运行中
     * <p>
     * 基于该镜像的容器正在正常运行。
     */
    RUNNING("running", "运行中"),

    /**
     * 停止中
     * <p>
     * 正在停止运行中的容器，这是一个进行中的操作状态。
     */
    STOPPING("stopping", "停止中"),

    /**
     * 停止失败
     * <p>
     * 容器停止操作失败，可能由于权限问题或容器无响应等原因。
     */
    STOP_FAILED("stop_failed", "停止失败"),

    /**
     * 已停止
     * <p>
     * 基于该镜像的容器已成功停止。
     */
    STOPPED("stopped", "已停止"),

    /**
     * 重启中
     * <p>
     * 正在重启运行中的容器，这是一个进行中的操作状态。
     */
    RESTARTING("restarting", "重启中"),

    /**
     * 重启失败
     * <p>
     * 容器重启操作失败，可能由于配置错误或资源不足等原因。
     */
    RESTART_FAILED("restart_failed", "重启失败"),

    /**
     * 删除中
     * <p>
     * 正在删除镜像或容器，这是一个进行中的操作状态。
     */
    DELETING("deleting", "删除中"),

    /**
     * 删除失败
     * <p>
     * 镜像或容器删除操作失败，可能由于权限问题或资源被占用等原因。
     */
    DELETE_FAILED("delete_failed", "删除失败"),

    /**
     * 异常状态
     * <p>
     * 镜像或容器处于异常状态，需要人工干预检查。
     */
    ABNORMAL("abnormal", "异常");

    /**
     * 状态码
     * <p>
     * 用于在数据库中存储状态的唯一标识符。
     */
    private final String code;

    /**
     * 状态描述
     * <p>
     * 用于在前端界面显示状态的可读描述信息。
     */
    private final String description;

    /**
     * 构造函数
     * <p>
     * 创建系统软件镜像状态枚举实例。
     *
     * @param code        状态码
     * @param description 状态描述
     */
    SystemSoftImageStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举值
     * <p>
     * 通过状态码查找对应的枚举值，便于从数据库中恢复状态。
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static SystemSoftImageStatus fromCode(String code) {
        for (SystemSoftImageStatus status : SystemSoftImageStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 返回状态码
     * <p>
     * 重写toString方法，返回状态码而不是枚举名称，便于日志记录和显示。
     *
     * @return 状态码
     */
    @Override
    public String toString() {
        return code;
    }
}