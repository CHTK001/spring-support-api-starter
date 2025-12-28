package com.chua.starter.monitor.callback;

import com.chua.docker.support.callback.DockerResultCallback;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.enums.OperationType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Docker拉取回调实现
 * <p>
 * 用于在Docker镜像拉取过程中通过WebSocket实时推送进度信息。
 * 该回调处理器实现了DockerResultCallback接口，能够处理拉取过程中的各种事件：
 * <ul>
 *   <li>onStart: 拉取开始事件</li>
 *   <li>onNext: 拉取进度更新事件</li>
 *   <li>onComplete: 拉取完成事件</li>
 *   <li>onError: 拉取错误事件</li>
 * </ul>
 *
 * <p>每个事件都会通过WebSocket向客户端推送实时信息，但不会更新数据库状态。
 * 如果需要同时更新数据库状态，请使用StatusUpdatingWebSocketDockerPullCallback。
 *
 * @param <T> Docker响应项类型
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 *
 * @see DockerResultCallback Docker回调接口
 * @see SocketSessionTemplate WebSocket会话模板
 * @see StatusUpdatingWebSocketDockerPullCallback 带状态更新的回调实现
 */
@Slf4j
public class WebSocketDockerPullCallback implements com.chua.docker.support.callback.PullImageCallback {

    /**
     * WebSocket会话模板
     * <p>
     * 用于向客户端发送WebSocket消息，实时推送Docker操作的进度信息。
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 操作类型
     * <p>
     * 标识当前Docker操作的类型，如PULL（拉取）、BUILD（构建）等。
     */
    private final OperationType operationType;

    /**
     * 镜像名称
     * <p>
     * 当前操作的Docker镜像名称，用于在消息中标识操作对象。
     */
    private final String imageName;

    /**
     * 开始时间
     * <p>
     * 记录Docker操作开始的时间，用于计算操作耗时等统计信息。
     */
    private final LocalDateTime startTime;

    /**
     * 当前状态
     * <p>
     * 记录Docker操作的当前状态信息，从拉取响应中获取。
     */
    @Getter
    private String currentStatus;

    /**
     * 是否完成
     * <p>
     * 标识Docker操作是否已完成，无论成功还是失败。
     */
    @Getter
    private boolean completed = false;

    /**
     * 是否发生错误
     * <p>
     * 标识Docker操作过程中是否发生了错误。
     */
    private boolean hasError = false;

    /**
     * 错误信息
     * <p>
     * 如果Docker操作过程中发生错误，此处存储错误的详细信息。
     */
    @Getter
    private String errorMessage;

    /**
     * 构造函数
     * <p>
     * 创建WebSocket Docker拉取回调实例。
     *
     * @param socketSessionTemplate WebSocket会话模板，用于发送消息
     * @param operationType         操作类型，标识当前Docker操作的类型
     * @param imageName             镜像名称，标识当前操作的Docker镜像
     */
    public WebSocketDockerPullCallback(SocketSessionTemplate socketSessionTemplate,
                                       OperationType operationType, String imageName) {
        this.socketSessionTemplate = socketSessionTemplate;
        this.operationType = operationType;
        this.imageName = imageName;
        this.startTime = LocalDateTime.now();
        log.info("创建WebSocket Docker拉取回调 - 操作类型: {}, 镜像: {}", operationType, imageName);
    }

    /**
     * 处理拉取进度更新事件
     * <p>
     * 当Docker拉取操作有新的进度信息时调用此方法。该方法会：
     * 1. 构造包含进度信息的WebSocket消息
     * 2. 通过WebSocket向客户端推送进度信息
     * 3. 更新当前状态信息
     *
     * @param pullItem 拉取响应项，包含进度信息
     */
    @Override
    public void onNext(com.github.dockerjava.api.model.PullResponseItem pullItem) {
        if (pullItem == null) {
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_progress");
            message.put("operation", operationType.toString());
            message.put("imageName", imageName);
            message.put("timestamp", LocalDateTime.now());

            // 根据不同类型的响应项处理
            message.put("status", pullItem.getStatus());
            message.put("progress", pullItem.getProgress());
            message.put("id", pullItem.getId());

            if (pullItem.getStatus() != null) {
                this.currentStatus = pullItem.getStatus();
            }

            log.debug("Docker拉取进度: {} - {}", pullItem.getStatus(), pullItem.getProgress());


            // 发送WebSocket消息
            socketSessionTemplate.sendObject(MonitorSocketTopics.Monitor.Docker.PROGRESS, message);

        } catch (Exception e) {
            log.error("发送Docker进度WebSocket消息失败", e);
        }
    }

    /**
     * 处理拉取开始事件
     * <p>
     * 当Docker拉取操作开始时调用此方法。该方法会：
     * 1. 构造拉取开始的WebSocket消息
     * 2. 通过WebSocket向客户端推送开始信息
     *
     * @param stream 可关闭的流对象
     */
    @Override
    public void onStart(Closeable stream) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_start");
            message.put("operation", operationType);
            message.put("imageName", imageName);
            message.put("timestamp", startTime);

            log.info("Docker拉取操作开始 - 镜像: {}", imageName);

            // 发送WebSocket消息
            socketSessionTemplate.sendObject(MonitorSocketTopics.Monitor.Docker.START, message);

        } catch (Exception e) {
            log.error("发送Docker开始WebSocket消息失败", e);
        }
    }

    /**
     * 处理拉取错误事件
     * <p>
     * 当Docker拉取操作发生错误时调用此方法。该方法会：
     * 1. 记录错误信息
     * 2. 构造错误信息的WebSocket消息
     * 3. 通过WebSocket向客户端推送错误信息
     *
     * @param throwable 错误异常对象
     */
    @Override
    public void onError(Throwable throwable) {
        this.hasError = true;
        this.errorMessage = throwable.getMessage();
        this.completed = true;

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_error");
            message.put("operation", operationType);
            message.put("imageName", imageName);
            message.put("errorMessage", errorMessage);
            message.put("timestamp", LocalDateTime.now());

            log.error("Docker操作发生错误 - 镜像: {}, 错误: {}", imageName, errorMessage, throwable);

            // 发送WebSocket消息
            socketSessionTemplate.sendObject(MonitorSocketTopics.Monitor.Docker.ERROR, message);

        } catch (Exception e) {
            log.error("发送Docker错误WebSocket消息失败", e);
        }
    }

    /**
     * 处理拉取完成事件
     * <p>
     * 当Docker拉取操作完成时调用此方法。该方法会：
     * 1. 更新完成状态
     * 2. 构造完成信息的WebSocket消息
     * 3. 通过WebSocket向客户端推送完成信息
     */
    @Override
    public void onComplete() {
        this.completed = true;

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_complete");
            message.put("operation", operationType);
            message.put("imageName", imageName);
            message.put("duration", java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds());
            message.put("timestamp", LocalDateTime.now());

            log.info("Docker操作完成 - 镜像: {}, 耗时: {}秒", imageName,
                    java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds());

            // 发送WebSocket消息
            socketSessionTemplate.sendObject(MonitorSocketTopics.Monitor.Docker.COMPLETE, message);

        } catch (Exception e) {
            log.error("发送Docker完成WebSocket消息失败", e);
        }
    }
}
