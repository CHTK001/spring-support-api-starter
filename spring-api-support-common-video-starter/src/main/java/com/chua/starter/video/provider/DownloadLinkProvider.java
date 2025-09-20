package com.chua.starter.video.provider;

import com.chua.common.support.lang.code.ListReturnResult;

/**
 * 下载链接提供者
 *
 * @author CH
 * @since 2025/9/19 16:23
 */
public interface DownloadLinkProvider {

    /**
     * 检索视频下载地址
     *
     * @param keyword 关键词
     * @return 视频下载地址列表
     */
    ListReturnResult<String> searchDownloadUrls(String keyword);
}
