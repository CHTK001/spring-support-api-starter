package com.chua.starter.monitor.callback;

import com.chua.docker.support.callback.DockerResultCallback;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.enums.OperationType;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftImageStatus;
import com.chua.starter.monitor.service.SystemSoftImageService;
import com.github.dockerjava.api.model.PullResponseItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 状态更新WebSocket Docker拉取回调实现
 * <p>
 * 用于在Docker镜像拉取过程中通过WebSocket实时推送进度信息，并更新数据库中的镜像状态。
 * 该回调处理器实现了DockerResultCallback接口，能够处理拉取过程中的各种事件：
 * <ul>
 *   <li>onStart: 拉取开始事件</li>
 *   <li>onNext: 拉取进度更新事件</li>
 *   <li>onComplete: 拉取完成事件</li>
 *   <li>onError: 拉取错误事件</li>
 * </ul>
 *
 * <p>每个事件都会通过WebSocket向客户端推送实时信息，并根据事件类型更新数据库中镜像的状态。
 *
 * @author CH
 * @version 1.0.0
 * @see DockerResultCallback Docker回调接口
 * @see SocketSessionTemplate WebSocket会话模板
 * @see SystemSoftImageService 系统软件镜像服务
 * @since 2024/01/01
 */
@Slf4j
public class StatusUpdatingWebSocketDockerPullCallback implements com.chua.docker.support.callback.PullImageCallback {

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
     * 系统软件镜像服务
     * <p>
     * 用于更新数据库中镜像的状态信息。
     */
    private final SystemSoftImageService systemSoftImageService;

    /**
     * 镜像ID
     * <p>
     * 数据库中镜像记录的ID，用于定位和更新特定镜像的状态。
     */
    private final Integer imageId;

    /**
     * 开始时间
     * <p>
     * 记录Docker操作开始的时间，用于计算操作耗时等统计信息。
     */
    private final LocalDateTime startTime;

    /**
     * Docker镜像ID
     * <p>
     * 从拉取响应中获取的Docker镜像ID。
     */
    private String dockerImageId;

    /**
     * 镜像摘要
     * <p>
     * 从拉取响应中获取的镜像摘要信息。
     */
    private String imageDigest;

    /**
     * 镜像大小
     * <p>
     * 从拉取响应中获取的镜像大小信息。
     */
    private Long imageSize;

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
     * 创建状态更新WebSocket Docker拉取回调实例。
     *
     * @param socketSessionTemplate  WebSocket会话模板，用于发送消息
     * @param operationType          操作类型，标识当前Docker操作的类型
     * @param imageName              镜像名称，标识当前操作的Docker镜像
     * @param systemSoftImageService 系统软件镜像服务，用于更新数据库状态
     * @param imageId                镜像ID，用于定位和更新特定镜像的状态
     */
    public StatusUpdatingWebSocketDockerPullCallback(SocketSessionTemplate socketSessionTemplate,
                                                     OperationType operationType, String imageName,
                                                     SystemSoftImageService systemSoftImageService, Integer imageId) {
        this.socketSessionTemplate = socketSessionTemplate;
        this.operationType = operationType;
        this.imageName = imageName;
        this.systemSoftImageService = systemSoftImageService;
        this.imageId = imageId;
        this.startTime = LocalDateTime.now();
        log.info("创建状态更新WebSocket Docker拉取回调 - 操作类型: {}, 镜像: {}, 镜像ID: {}", operationType, imageName, imageId);
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
    public void onNext(PullResponseItem pullItem) {
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

            // 检查是否有Docker镜像ID
            if (pullItem.getId() != null && !pullItem.getId().isEmpty()) {
                this.dockerImageId = pullItem.getId();
            }

            // 检查aux字段中的镜像信息
            if (pullItem.getAux() != null) {
                // 获取镜像摘要
                if (pullItem.getAux().getDigest() != null && !pullItem.getAux().getDigest().isEmpty()) {
                    this.imageDigest = pullItem.getAux().getDigest();
                }

                // 获取镜像大小
                if (pullItem.getAux().getSize() != null) {
                    this.imageSize = pullItem.getAux().getSize().longValue();
                }
            }

            log.debug("Docker拉取进度: {} - {}", pullItem.getStatus(), pullItem.getProgress());

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_progress", message);

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
     * 3. 更新数据库中镜像的状态为PULLING（拉取中）
     *
     * @param stream 可关闭的流对象
     */
    @Override
    public void onStart(Closeable stream) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_start");
            message.put("operation", operationType.toString());
            message.put("imageName", imageName);
            message.put("timestamp", startTime);

            log.info("Docker拉取操作开始 - 镜像: {}", imageName);

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_start", message);

            // 更新数据库中的镜像状态为拉取中
            updateImageStatus(SystemSoftImageStatus.PULLING, null);

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
     * 4. 更新数据库中镜像的状态为PULL_FAILED（拉取失败）
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
            message.put("imageName", imageName);
            message.put("error", throwable.getMessage());
            message.put("timestamp", LocalDateTime.now());

            log.error("Docker拉取操作失败 - 镜像: {}, 错误: {}", imageName, throwable.getMessage(), throwable);

            // 发送WebSocket消息
            socketSessionTemplate.sendObject("docker_error", message);

            // 更新数据库中的镜像状态为拉取失败，并记录错误信息
            updateImageStatus(SystemSoftImageStatus.PULL_FAILED, throwable.getMessage());

        } catch (Exception e) {
            log.error("发送Docker错误WebSocket消息失败", e);
        }
    }

    /**
     * 处理拉取完成事件
     * <p>
     * 当Docker拉取操作完成时调用此方法。该方法会：
     * 1. 标记操作已完成
     * 2. 构造完成信息的WebSocket消息
     * 3. 通过WebSocket向客户端推送完成信息
     * 4. 如果没有错误，更新数据库中镜像的状态为AVAILABLE（可用）
     */
    @Override
    public void onComplete() {
        try {
            this.completed = true;

            Map<String, Object> message = new HashMap<>();
            message.put("type", "docker_complete");
            message.put("operation", operationType.toString());
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
            socketSessionTemplate.sendObject("docker_complete", message);

            // 如果没有错误，更新数据库中的镜像状态为可用，并补充镜像详细信息
            if (!hasError) {
                updateImageStatus(SystemSoftImageStatus.AVAILABLE, null);
            }

        } catch (Exception e) {
            log.error("发送Docker完成WebSocket消息失败", e);
        }
    }

    /**
     * 更新镜像状态
     * <p>
     * 根据指定的状态更新数据库中镜像记录的状态信息。
     *
     * @param status       新的状态
     * @param errorMessage 错误信息（如果有的话）
     */
    private void updateImageStatus(SystemSoftImageStatus status, String errorMessage) {
        try {
            if (imageId != null && systemSoftImageService != null) {
                SystemSoftImage image = systemSoftImageService.getById(imageId);
                if (image != null) {
                    image.setSystemSoftImageStatus(status);
                    // 如果有Docker镜像ID，则更新到数据库中
                    if (dockerImageId != null && !dockerImageId.isEmpty()) {
                        image.setSystemSoftImageImageId(dockerImageId);
                    }

                    // 如果有镜像摘要，则更新到数据库中
                    if (imageDigest != null && !imageDigest.isEmpty()) {
                        image.setSystemSoftImageDigest(imageDigest);
                    }

                    // 如果有镜像大小，则更新到数据库中
                    if (imageSize != null) {
                        image.setSystemSoftImageSize(imageSize);
                    }

                    // 如果有错误信息，则更新到数据库中
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        image.setSystemSoftImageRemark(errorMessage);
                    }
                    systemSoftImageService.updateById(image);
                    log.info("更新镜像状态成功 - 镜像ID: {}, 状态: {}, Docker镜像ID: {}, 摘要: {}, 大小: {}",
                            imageId, status, dockerImageId, imageDigest, imageSize);
                } else {
                    log.warn("无法更新镜像状态，镜像不存在 - 镜像ID: {}", imageId);
                }
            }
        } catch (Exception e) {
            log.error("更新镜像状态失败 - 镜像ID: {}, 状态: {}", imageId, status, e);
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