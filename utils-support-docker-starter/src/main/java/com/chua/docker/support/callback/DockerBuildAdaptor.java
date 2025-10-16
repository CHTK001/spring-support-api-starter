package com.chua.docker.support.callback;

import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import lombok.extern.slf4j.Slf4j;

/**
 * Docker镜像构建适配器
 * 用于适配Docker镜像构建过程中的回调处理
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/1/17
 */
@Slf4j
public class DockerBuildAdaptor extends BuildImageResultCallback {

    /**
     * 用户自定义的回调接口
     */
    private final DockerResultCallback<BuildResponseItem> callback;

    /**
     * 构造函数
     *
     * @param callback 用户自定义的回调接口
     */
    public DockerBuildAdaptor(DockerResultCallback<BuildResponseItem> callback) {
        this.callback = callback;
    }

    @Override
    public void onNext(BuildResponseItem item) {
        super.onNext(item);

        if (callback != null) {
            try {
                callback.onNext(item);
            } catch (Exception e) {
                log.error("处理Docker构建回调时发生错误", e);
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
                log.error("处理Docker构建错误回调时发生异常", e);
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
                log.error("处理Docker构建完成回调时发生异常", e);
            }
        }
    }

}