package com.chua.docker.support.callback;

import com.github.dockerjava.api.model.ResponseItem;

import java.io.Closeable;

/**
 * Docker结果回调接口
 *
 * @author CH
 * @since 2025/9/14 12:35
 */
public interface DockerResultCallback<T extends ResponseItem> {

    /**
     * 处理构建响应项
     *
     * @param item 构建响应项
     */
    void onNext(T item);

    /**
     * 回调开始时触发
     *
     * @param stream 可关闭的流对象，用于控制数据传输过程
     */
    void onStart(Closeable stream);

    /**
     * 发生错误时触发
     *
     * @param throwable 异常信息，例如：new RuntimeException("网络连接失败")
     */
    void onError(Throwable throwable);

    /**
     * 回调完成时触发
     */
    void onComplete();
}
