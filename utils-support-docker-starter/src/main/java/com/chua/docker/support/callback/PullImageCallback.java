package com.chua.docker.support.callback;

import com.github.dockerjava.api.model.PullResponseItem;

/**
 * 镜像拉取回调接口
 * <p>
 * 专门用于处理Docker镜像拉取操作的回调接口，扩展自通用的DockerResultCallback接口。
 * 该接口定义了镜像拉取过程中各个阶段的回调方法，允许实现类根据具体需求处理不同事件。
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/09/22
 * 
 * @see DockerResultCallback 通用Docker回调接口
 * @see PullResponseItem Docker镜像拉取响应项
 */
public interface PullImageCallback extends DockerResultCallback<PullResponseItem> {

    /**
     * 镜像验证阶段回调
     * <p>
     * 在开始拉取镜像之前，对镜像信息进行验证时调用此方法。
     * 可用于检查镜像名称格式、标签有效性等预检查操作。
     */
    default void onImageValidating() {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 拉取开始阶段回调
     * <p>
     * 在镜像拉取命令发送到Docker守护进程时调用此方法。
     * 可用于记录拉取开始时间、初始化进度跟踪等操作。
     */
    default void onPulling() {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 拉取成功回调
     * <p>
     * 在镜像成功拉取后调用此方法。
     * 可用于记录拉取完成时间、更新镜像状态等操作。
     */
    default void onPullSuccess() {
        // 默认空实现，允许子类选择性重写
    }

    /**
     * 拉取失败回调
     * <p>
     * 在镜像拉取失败时调用此方法。
     * 可用于记录错误信息、更新镜像状态等操作。
     *
     * @param errorMessage 错误信息
     */
    default void onPullFailure(String errorMessage) {
        // 默认空实现，允许子类选择性重写
    }
}