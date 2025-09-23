package com.chua.starter.monitor.service;

import com.chua.docker.support.callback.CreateContainerCallback;
import com.chua.docker.support.callback.DockerResultCallback;
import com.chua.docker.support.callback.PullImageCallback;
import com.chua.docker.support.client.DockerCreateContainer;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.docker.support.client.DockerPullImage;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.callback.StatusUpdatingWebSocketContainerCreateCallback;
import com.chua.starter.monitor.callback.StatusUpdatingWebSocketDockerPullCallback;
import com.chua.starter.monitor.callback.WebSocketContainerCreateCallback;
import com.chua.starter.monitor.callback.WebSocketDockerPullCallback;
import com.chua.starter.monitor.enums.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Docker回调服务
 * 用于处理支持异步回调的Docker操作
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 */
@Service
@Slf4j
public class DockerCallbackService {

    private final SocketSessionTemplate socketSessionTemplate;
    private final SystemSoftImageService systemSoftImageService;
    private final SystemSoftContainerService systemSoftContainerService;
    public DockerCallbackService(SocketSessionTemplate socketSessionTemplate,
                                 SystemSoftImageService systemSoftImageService,
                                 SystemSoftContainerService systemSoftContainerService) {
        this.socketSessionTemplate = socketSessionTemplate;
        this.systemSoftImageService = systemSoftImageService;
        this.systemSoftContainerService = systemSoftContainerService;
    }

    public SocketSessionTemplate getSocketSessionTemplate() {
        return socketSessionTemplate;
    }

    /**
     * 拉取镜像（支持回调）
     * <p>
     * 使用Docker客户端和DockerPullImage配置对象来拉取Docker镜像，并通过回调处理器
     * 处理拉取过程中的各种事件。该方法封装了Docker镜像拉取的完整流程，包括错误处理。
     *
     * @param dockerClient Docker客户端实例，用于执行实际的镜像拉取操作
     * @param config       拉取镜像配置对象，包含镜像名称、标签、仓库、认证信息等参数
     * @param callback     回调处理器，用于处理拉取过程中的各种事件（如进度更新、错误处理等）
     * @see DockerProtocolClient#pullImage(DockerPullImage, DockerResultCallback) Docker客户端拉取方法
     * @see DockerPullImage 镜像拉取配置对象
     * @see DockerResultCallback Docker回调接口
     */
    public void pullImageWithCallback(DockerProtocolClient dockerClient, DockerPullImage config, PullImageCallback callback) {
        try {
            log.info("开始拉取镜像(带回调): {}:{}", config.getImageName(), config.getTag());
            // 调用Docker客户端的pullImage方法，传入回调处理器
            dockerClient.pullImage(config, callback);
        } catch (Exception e) {
            log.error("拉取镜像失败: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    /**
     * 创建容器（支持回调）
     * <p>
     * 使用Docker客户端和DockerCreateContainer配置对象来创建Docker容器，并通过回调处理器
     * 处理创建过程中的各种事件。该方法封装了Docker容器创建的完整流程，包括错误处理。
     *
     * @param dockerClient Docker客户端实例，用于执行实际的容器创建操作
     * @param config       创建容器配置对象，包含镜像名称、容器名称、端口映射等参数
     * @param callback     回调处理器，用于处理创建过程中的各种事件（如进度更新、错误处理等）
     * @see DockerCreateContainer 容器创建配置对象
     * @see DockerResultCallback Docker回调接口
     */
    public void createContainerWithCallback(DockerProtocolClient dockerClient, DockerCreateContainer config, CreateContainerCallback callback) {
        try {
            log.info("开始创建容器(带回调): {}:{}", config.getImageName(), config.getContainerName());
            // 调用Docker客户端的createContainer方法，传入回调处理器
            dockerClient.createContainer(config, callback);
        } catch (Exception e) {
            log.error("创建容器失败: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    /**
     * 创建并启动容器（支持回调）
     * <p>
     * 使用Docker客户端和DockerCreateContainer配置对象来创建并启动Docker容器，并通过回调处理器
     * 处理创建和启动过程中的各种事件。该方法封装了Docker容器创建和启动的完整流程，包括错误处理。
     *
     * @param dockerClient Docker客户端实例，用于执行实际的容器创建和启动操作
     * @param config       创建容器配置对象，包含镜像名称、容器名称、端口映射等参数
     * @param callback     回调处理器，用于处理创建和启动过程中的各种事件（如进度更新、错误处理等）
     * @see DockerCreateContainer 容器创建配置对象
     * @see DockerResultCallback Docker回调接口
     */
    public void createAndStartContainerWithCallback(DockerProtocolClient dockerClient, DockerCreateContainer config, CreateContainerCallback callback) {
        try {
            log.info("开始创建并启动容器(带回调): {}:{}", config.getImageName(), config.getContainerName());
            // 调用Docker客户端的createAndStartContainer方法，传入回调处理器
            dockerClient.createAndStartContainer(config, callback);
        } catch (Exception e) {
            log.error("创建并启动容器失败: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    /**
     * 构建镜像（支持回调）
     *
     * @param dockerClient Docker客户端
     * @param dockerfile   Dockerfile文件
     * @param timeout      超时时间
     * @param timeUnit     时间单位
     * @param callback     回调处理器
     * @param tags         镜像标签
     */
    public void buildImageWithCallback(DockerProtocolClient dockerClient, File dockerfile, long timeout, String timeUnit, DockerResultCallback callback, String... tags) {
        try {
            log.info("开始构建镜像(带回调), Dockerfile: {}", dockerfile.getAbsolutePath());
            // 调用Docker客户端的buildImage方法，传入回调处理器
            dockerClient.buildImage(dockerfile, timeout, java.util.concurrent.TimeUnit.valueOf(timeUnit), callback, tags);
        } catch (Exception e) {
            log.error("构建镜像失败: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    /**
     * 推送镜像（支持回调）
     *
     * @param dockerClient Docker客户端
     * @param imageName    镜像名称
     * @param callback     回调处理器
     */
    public void pushImageWithCallback(DockerProtocolClient dockerClient, String imageName, DockerResultCallback callback) {
        try {
            log.info("开始推送镜像(带回调): {}", imageName);
            // 调用Docker客户端的pushImage方法，传入回调处理器
            dockerClient.pushImage(imageName, callback);
        } catch (Exception e) {
            log.error("推送镜像失败: {}", e.getMessage(), e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    /**
     * 创建WebSocket Docker拉取回调
     *
     * @param operationType 操作类型
     * @param imageName     镜像名称
     * @return WebSocket Docker拉取回调
     */
    public WebSocketDockerPullCallback createWebSocketPullCallback(OperationType operationType, String imageName) {
        return new WebSocketDockerPullCallback(socketSessionTemplate, operationType, imageName);
    }

    /**
     * 创建状态更新WebSocket Docker拉取回调
     *
     * @param operationType 操作类型
     * @param imageName     镜像名称
     * @param imageId       镜像ID
     * @return 状态更新WebSocket Docker拉取回调
     */
    public StatusUpdatingWebSocketDockerPullCallback createStatusUpdatingWebSocketPullCallback(OperationType operationType, String imageName, Integer imageId) {
        return new StatusUpdatingWebSocketDockerPullCallback(socketSessionTemplate, operationType, imageName, systemSoftImageService, imageId);
    }

    /**
     * 创建WebSocket容器创建回调
     *
     * @param operationType 操作类型
     * @param containerName 容器名称
     * @return WebSocket容器创建回调
     */
    public WebSocketContainerCreateCallback createWebSocketContainerCallback(OperationType operationType, String containerName) {
        return new WebSocketContainerCreateCallback(socketSessionTemplate, operationType, containerName);
    }

    /**
     * 创建状态更新WebSocket容器创建回调
     *
     * @param operationType 操作类型
     * @param containerName 容器名称
     * @param containerId   容器ID
     * @return 状态更新WebSocket容器创建回调
     */
    public StatusUpdatingWebSocketContainerCreateCallback createStatusUpdatingWebSocketContainerCallback(OperationType operationType, String containerName, Integer containerId) {
        return new StatusUpdatingWebSocketContainerCreateCallback(socketSessionTemplate, operationType, containerName, systemSoftContainerService, containerId);
    }
}