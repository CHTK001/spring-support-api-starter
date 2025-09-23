package com.chua.starter.monitor.callback;

import com.chua.docker.support.callback.DockerResultCallback;
import com.github.dockerjava.api.model.ResponseItem;

/**
 * 容器创建回调接口
 * <p>
 * 专门用于处理Docker容器创建操作的回调接口，扩展自通用的DockerResultCallback接口。
 * 该接口定义了容器创建过程中各个阶段的回调方法，允许实现类根据具体需求处理不同事件。
 *
 * @param <T> Docker响应项类型，通常为ResponseItem或其子类
 * @author CH
 * @version 1.0.0
 * @see DockerResultCallback 通用Docker回调接口
 * @see ResponseItem Docker响应项基类
 * @since 2025/09/22
 */
public interface ContainerCreateCallback<T extends ResponseItem> extends DockerResultCallback<T> {

    /**
     * 容器配置验证阶段回调
     * <p>
     * 在开始创建容器之前，对容器配置进行验证时调用此方法。
     * 可用于检查镜像是否存在、端口是否冲突等预检查操作。
     */
    default void onConfigValidating() {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 容器创建阶段回调
     * <p>
     * 在容器创建命令发送到Docker守护进程时调用此方法。
     * 可用于记录创建开始时间、初始化进度跟踪等操作。
     */
    default void onCreating() {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 容器创建成功回调
     * <p>
     * 在容器成功创建后调用此方法，提供创建的容器ID。
     * 可用于记录容器ID、更新数据库状态等操作。
     *
     * @param containerId 创建成功的容器ID
     */
    default void onCreateSuccess(String containerId) {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 容器启动阶段回调
     * <p>
     * 在容器创建成功后，开始启动容器时调用此方法。
     * 可用于记录启动开始时间、更新状态等操作。
     */
    default void onStarting() {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 容器启动成功回调
     * <p>
     * 在容器成功启动后调用此方法。
     * 可用于更新数据库状态、记录启动完成时间等操作。
     */
    default void onStartSuccess() {
        // 默认空实现，允许子类选择性重写
    }
}