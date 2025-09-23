package com.chua.starter.monitor.callback;

import com.chua.docker.support.callback.CreateContainerCallback;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.enums.OperationType;

import com.github.dockerjava.api.model.ResponseItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket容器创建回调实现
 * <p>
 * 用于在Docker容器创建过程中通过WebSocket实时推送进度信息。
 * 该回调处理器实现了CreateContainerCallback接口，能够处理创建过程中的各种事件：
 * <ul>
 *   <li>onStart: 创建开始事件</li>
 *   <li>onNext: 创建进度更新事件</li>
 *   <li>onComplete: 创建完成事件</li>
 *   <li>onError: 创建错误事件</li>
 * </ul>
 *
 * <p>每个事件都会通过WebSocket向客户端推送实时信息。
 *
 * @author CH
 * @version 1.0.0
 * @see CreateContainerCallback 容器创建回调接口
 * @see SocketSessionTemplate WebSocket会话模板
 * @since 2025/09/22
 */
@Slf4j
public class WebSocketContainerCreateCallback implements CreateContainerCallback {

    /**
     * WebSocket会话模板
     * <p>
     * 用于向客户端发送WebSocket消息，实时推送Docker操作的进度信息。
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 操作类型
     * <p>
     * 标识当前Docker操作的类型，如CREATE（创建）等。
     */
    private final OperationType operationType;

    /**
     * 容器名称
     * <p>
     * 当前操作的Docker容器名称，用于在消息中标识操作对象。
     */
    private final String containerName;

    /**
     * 开始时间
     * <p>
     * 记录Docker操作开始的时间，用于计算操作耗时等统计信息。
     */
    private final LocalDateTime startTime;

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
     * 创建WebSocket容器创建回调实例。
     *
     * @param socketSessionTemplate WebSocket会话模板，用于发送消息
     * @param operationType         操作类型，标识当前Docker操作的类型
     * @param containerName         容器名称，标识当前操作的Docker容器
     */
    public WebSocketContainerCreateCallback(SocketSessionTemplate socketSessionTemplate,
                                            OperationType operationType, String containerName) {
        this.socketSessionTemplate = socketSessionTemplate;
        this.operationType = operationType;
        this.containerName = containerName;
        this.startTime = LocalDateTime.now();
        log.info("创建WebSocket容器创建回调 - 操作类型: {}, 容器: {}", operationType, containerName);
    }

    /**
     * 处理创建进度更新事件
     * <p>
     * 当Docker创建操作有新的进度信息时调用此方法。该方法会：
     * 1. 构造包含进度信息的WebSocket消息
     * 2. 通过WebSocket向客户端推送进度信息
     * 3. 更新当前状态信息
     *
     * @param responseItem 响应项，包含进度信息
     */
    @Override
    public void onNext(ResponseItem responseItem) {
        if (responseItem == null) {
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_progress");
            message.put("operation", operationType.toString());
            message.put("containerName", containerName);
            message.put("timestamp", LocalDateTime.now());

            // 根据不同类型的响应项处理
            message.put("status", responseItem.getStatus());
            message.put("progress", responseItem.getProgress());
            message.put("id", responseItem.getId());

            log.debug("Docker创建进度: {} - {}", responseItem.getStatus(), responseItem.getProgress());

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_progress", message);

        } catch (Exception e) {
            log.error("发送Docker进度WebSocket消息失败", e);
        }
    }

    /**
     * 处理创建开始事件
     * <p>
     * 当Docker创建操作开始时调用此方法。该方法会：
     * 1. 构造创建开始的WebSocket消息
     * 2. 通过WebSocket向客户端推送开始信息
     *
     * @param stream 可关闭的流对象
     */
    @Override
    public void onStart(Closeable stream) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_start");
            message.put("operation", operationType.toString());
            message.put("containerName", containerName);
            message.put("timestamp", startTime);

            log.info("Docker创建操作开始 - 容器: {}", containerName);

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_start", message);

        } catch (Exception e) {
            log.error("发送Docker开始WebSocket消息失败", e);
        }
    }

    /**
     * 处理创建错误事件
     * <p>
     * 当Docker创建操作发生错误时调用此方法。该方法会：
     * 1. 记录错误信息
     * 2. 构造错误信息的WebSocket消息
     * 3. 通过WebSocket向客户端推送错误信息
     *
     * @param throwable 错误异常对象
     */
    @Override
    public void onError(Throwable throwable) {
        try {
            this.hasError = true;
            this.errorMessage = throwable.getMessage();

            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_error");
            message.put("operation", operationType.toString());
            message.put("containerName", containerName);
            message.put("error", throwable.getMessage());
            message.put("timestamp", LocalDateTime.now());

            log.error("Docker创建操作失败 - 容器: {}, 错误: {}", containerName, throwable.getMessage(), throwable);

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_error", message);

        } catch (Exception e) {
            log.error("发送Docker错误WebSocket消息失败", e);
        }
    }

    /**
     * 处理创建完成事件
     * <p>
     * 当Docker创建操作完成时调用此方法。该方法会：
     * 1. 标记操作已完成
     * 2. 构造完成信息的WebSocket消息
     * 3. 通过WebSocket向客户端推送完成信息
     */
    @Override
    public void onComplete() {
        try {
            this.completed = true;

            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_complete");
            message.put("operation", operationType.toString());
            message.put("containerName", containerName);
            message.put("success", !hasError);
            message.put("timestamp", LocalDateTime.now());

            if (hasError) {
                message.put("error", errorMessage);
                log.info("Docker创建操作完成(失败) - 容器: {}, 错误: {}", containerName, errorMessage);
            } else {
                log.info("Docker创建操作完成(成功) - 容器: {}", containerName);
            }

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_complete", message);

        } catch (Exception e) {
            log.error("发送Docker完成WebSocket消息失败", e);
        }
    }

    /**
     * 是否有错误
     * <p>
     * 检查Docker操作过程中是否发生了错误。
     *
     * @return 是否有错误
     */
    public boolean hasError() {
        return hasError;
    }

}