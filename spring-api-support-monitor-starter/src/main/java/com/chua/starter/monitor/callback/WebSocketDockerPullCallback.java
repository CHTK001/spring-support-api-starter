package com.chua.starter.monitor.callback;

import com.chua.docker.support.callback.DockerBuildResponseItem;
import com.chua.docker.support.callback.DockerPullResponseItem;
import com.chua.docker.support.callback.DockerPushResponseItem;
import com.chua.docker.support.callback.DockerResultCallback;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.github.dockerjava.api.model.ResponseItem;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Docker拉取回调实现
 * 用于在Docker镜像拉取过程中通过WebSocket实时推送进度信息
 * 支持泛型，可处理不同类型的Docker响应项
 *
 * @param <T> Docker响应项类型
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 */
@Slf4j
public class WebSocketDockerPullCallback<T> implements DockerResultCallback {

    /**
     * WebSocket会话模板
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 操作类型
     */
    private final String operationType;

    /**
     * 镜像名称
     */
    private final String imageName;
    /**
     * 开始时间
     */
    private final LocalDateTime startTime;
    /**
     * 当前状态
     */
    private String currentStatus;
    /**
     * 是否完成
     */
    private boolean completed = false;
    /**
     * 是否发生错误
     */
    private boolean hasError = false;
    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 构造函数
     *
     * @param socketSessionTemplate WebSocket会话模板
     * @param operationType         操作类型
     * @param imageName             镜像名称
     */
    public WebSocketDockerPullCallback(SocketSessionTemplate socketSessionTemplate,
                                       String operationType, String imageName) {
        this.socketSessionTemplate = socketSessionTemplate;
        this.operationType = operationType;
        this.imageName = imageName;
        this.startTime = LocalDateTime.now();
        log.info("创建WebSocket Docker拉取回调 - 操作类型: {}, 镜像: {}", operationType, imageName);
    }

    @Override
    public void onNext(ResponseItem item) {
        if (item == null) {
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_progress");
            message.put("operation", operationType);
            message.put("imageName", imageName);
            message.put("timestamp", LocalDateTime.now());

            // 根据不同类型的响应项处理
            if (item instanceof DockerPullResponseItem) {
                DockerPullResponseItem pullItem = (DockerPullResponseItem) item;
                message.put("status", pullItem.getStatus());
                message.put("progress", pullItem.getProgress());
                message.put("id", pullItem.getId());

                if (pullItem.getStatus() != null) {
                    this.currentStatus = pullItem.getStatus();
                }

                log.debug("Docker拉取进度: {} - {}", pullItem.getStatus(), pullItem.getProgress());

            }

            // 发送WebSocket消息
            socketSessionTemplate.send("docker_progress", message);

        } catch (Exception e) {
            log.error("发送Docker进度WebSocket消息失败", e);
        }
    }


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
            socketSessionTemplate.send("docker_start", message);

        } catch (Exception e) {
            log.error("发送Docker开始WebSocket消息失败", e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        try {
            this.hasError = true;
            this.errorMessage = throwable.getMessage();

            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_error");
            message.put("operation", operationType);
            message.put("imageName", imageName);
            message.put("error", throwable.getMessage());
            message.put("timestamp", LocalDateTime.now());

            log.error("Docker拉取操作失败 - 镜像: {}, 错误: {}", imageName, throwable.getMessage(), throwable);

            // 发送WebSocket消息
            socketSessionTemplate.send("docker_error", message);

        } catch (Exception e) {
            log.error("发送Docker错误WebSocket消息失败", e);
        }
    }

    @Override
    public void onComplete() {
        try {
            this.completed = true;

            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_complete");
            message.put("operation", operationType);
            message.put("imageName", imageName);
            message.put("success", !hasError);
            message.put("timestamp", LocalDateTime.now());

            if (hasError) {
                message.put("error", errorMessage);
                log.info("Docker拉取操作完成(失败) - 镜像: {}, 错误: {}", imageName, errorMessage);
            } else {
                log.info("Docker拉取操作完成(成功) - 镜像: {}", imageName);
            }

            // 发送WebSocket消息
            socketSessionTemplate.send("docker_complete", message);

        } catch (Exception e) {
            log.error("发送Docker完成WebSocket消息失败", e);
        }
    }

    /**
     * 获取当前状态
     *
     * @return 当前状态
     */
    public String getCurrentStatus() {
        return currentStatus;
    }

    /**
     * 是否完成
     *
     * @return 是否完成
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * 是否有错误
     *
     * @return 是否有错误
     */
    public boolean hasError() {
        return hasError;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}