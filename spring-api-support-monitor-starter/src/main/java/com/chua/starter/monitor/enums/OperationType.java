package com.chua.starter.monitor.enums;

import com.chua.starter.monitor.callback.StatusUpdatingWebSocketDockerPullCallback;
import com.chua.starter.monitor.callback.WebSocketDockerPullCallback;

/**
 * Docker操作类型枚举
 * <p>
 * 定义了系统支持的Docker操作类型，用于标识和区分不同的Docker操作。
 * 每个操作类型都有对应的code和description，便于在日志记录、WebSocket消息
 * 和用户界面中显示。
 *
 * <p>操作类型包括：
 * <ul>
 *   <li>PULL: 从远程仓库拉取镜像</li>
 *   <li>BUILD: 基于Dockerfile构建镜像</li>
 *   <li>PUSH: 将本地镜像推送到远程仓库</li>
 *   <li>START: 启动容器</li>
 *   <li>STOP: 停止容器</li>
 *   <li>RESTART: 重启容器</li>
 *   <li>DELETE: 删除镜像或容器</li>
 * </ul>
 *
 * @author CH
 * @see WebSocketDockerPullCallback WebSocket回调处理器
 * @see StatusUpdatingWebSocketDockerPullCallback 带状态更新的回调处理器
 * @since 2025/01/15
 */
public enum OperationType {
    /**
     * 拉取操作
     * <p>
     * 从远程仓库拉取Docker镜像到本地，是Docker最常用的操作之一。
     */
    PULL("pull", "拉取"),

    /**
     * 构建操作
     * <p>
     * 基于Dockerfile和构建上下文构建Docker镜像。
     */
    BUILD("build", "构建"),

    /**
     * 推送操作
     * <p>
     * 将本地Docker镜像推送到远程仓库，便于镜像分发和共享。
     */
    PUSH("push", "推送"),

    /**
     * 启动操作
     * <p>
     * 启动基于指定镜像的容器实例。
     */
    START("start", "启动"),

    /**
     * 停止操作
     * <p>
     * 停止正在运行的容器实例。
     */
    STOP("stop", "停止"),

    /**
     * 重启操作
     * <p>
     * 重启正在运行的容器实例。
     */
    RESTART("restart", "重启"),

    /**
     * 删除操作
     * <p>
     * 删除本地的Docker镜像或容器实例。
     */
    DELETE("delete", "删除"),
    /**
     * 创建操作
     * <p>
     * 创建容器实例。
     */
    CREATE("create", "创建" );

    /**
     * 操作码
     * <p>
     * 用于标识操作类型的唯一字符串标识符。
     */
    private final String code;

    /**
     * 操作描述
     * <p>
     * 用于在用户界面显示操作类型的可读描述信息。
     */
    private final String description;

    /**
     * 构造函数
     * <p>
     * 创建Docker操作类型枚举实例。
     *
     * @param code        操作码
     * @param description 操作描述
     */
    OperationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取操作码
     * <p>
     * 返回操作类型的唯一标识符。
     *
     * @return 操作码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取操作描述
     * <p>
     * 返回操作类型的可读描述信息。
     *
     * @return 操作描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回操作码
     * <p>
     * 重写toString方法，返回操作码而不是枚举名称，便于日志记录和显示。
     *
     * @return 操作码
     */
    @Override
    public String toString() {
        return code;
    }
}