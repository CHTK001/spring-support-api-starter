package com.chua.starter.video.provider;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;

import java.util.List;

/**
 * 资源提供者接口
 *
 * @author CH
 */
public interface ResourceProvider {

    /**
     * 检索视频资源
     *
     * @param keyword 关键词
     * @return 视频资源列表
     */
    ReturnResult<List<VideoInfo>> searchResource(String keyword);

    /**
     * 获取资源提供者名称
     *
     * @return 资源提供者名称
     */
    String getProviderName();

    /**
     * 获取资源提供者优先级
     *
     * @return 优先级，数字越小优先级越高
     */
    int getPriority();

    /**
     * 检查资源提供者是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
} 