package com.chua.docker.support.callback;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.PushResponseItem;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

/**
 * Docker镜像拉取适配器
 * 用于将Docker Java API的PullResponseItem适配为自定义的DockerPullResponseItem
 * 并通过DockerResultCallback接口提供回调功能
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/1/17
 */
@Slf4j
public class DockerPullAdaptor extends ResultCallback.Adapter<PushResponseItem> {

    /**
     * 用户自定义的回调接口
     */
    private final DockerResultCallback<PushResponseItem> callback;

    /**
     * 构造函数
     *
     * @param callback 用户自定义的回调接口，不能为null
     * @throws IllegalArgumentException 当callback为null时抛出
     */
    public DockerPullAdaptor(DockerResultCallback<PushResponseItem> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("回调接口不能为null");
        }
        this.callback = callback;
    }

    /**
     * 处理下一个拉取响应项
     * 将Docker Java API的PullResponseItem转换为自定义的DockerPullResponseItem
     *
     * @param object Docker Java API返回的拉取响应项
     */
    @Override
    public void onNext(PushResponseItem object) {
        try {
            if (object != null) {
                callback.onNext(object);
            }
        } catch (Exception e) {
            log.error("处理Docker拉取响应时发生异常", e);
            callback.onError(e);
        }
    }

    /**
     * 回调开始时触发
     *
     * @param stream 可关闭的流对象
     */
    @Override
    public void onStart(Closeable stream) {
        try {
            log.debug("Docker镜像拉取开始");
            callback.onStart(stream);
        } catch (Exception e) {
            log.error("处理Docker拉取开始回调时发生异常", e);
            callback.onError(e);
        }
    }

    /**
     * 发生错误时触发
     *
     * @param throwable 异常信息
     */
    @Override
    public void onError(Throwable throwable) {
        try {
            log.error("Docker镜像拉取过程中发生错误", throwable);
            callback.onError(throwable);
        } catch (Exception e) {
            log.error("处理Docker拉取错误回调时发生异常", e);
        }
    }

    /**
     * 回调完成时触发
     */
    @Override
    public void onComplete() {
        try {
            log.debug("Docker镜像拉取完成");
            callback.onComplete();
        } catch (Exception e) {
            log.error("处理Docker拉取完成回调时发生异常", e);
        }
    }
}