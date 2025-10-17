package com.chua.docker.support.callback;

import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.command.PushImageResultCallback;
import lombok.extern.slf4j.Slf4j;

/**
 * Docker镜像推送适配器
 * 用于适配Docker镜像推送过程中的回调处理
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/1/17
 */
@Slf4j
public class DockerPushAdaptor extends PushImageResultCallback {

    /**
     * 用户自定义的回调接口
     */
    private final DockerResultCallback<PushResponseItem> callback;

    /**
     * 构造函数
     *
     * @param callback 用户自定义的回调接口
     */
    public DockerPushAdaptor(DockerResultCallback<PushResponseItem> callback) {
        this.callback = callback;
    }

    @Override
    public void onNext(PushResponseItem item) {
        super.onNext(item);

        if (callback != null) {
            try {
                // 调用用户回调（推送响应）
                callback.onNext(item);
            } catch (Exception e) {
                log.error("处理Docker推送回调时发生错误", e);
                callback.onError(e);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        super.onError(throwable);

        if (callback != null) {
            try {
                callback.onError(throwable);
            } catch (Exception e) {
                log.error("处理Docker推送错误回调时发生异常", e);
            }
        }
    }

    @Override
    public void onComplete() {
        super.onComplete();

        if (callback != null) {
            try {
                callback.onComplete();
            } catch (Exception e) {
                log.error("处理Docker推送完成回调时发生异常", e);
            }
        }
    }
}