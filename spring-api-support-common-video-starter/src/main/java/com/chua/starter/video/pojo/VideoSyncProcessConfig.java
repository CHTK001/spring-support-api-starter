package com.chua.starter.video.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * 视频同步配置
 *
 * @author CH
 * @since 2025/9/19 9:04
 */
@Data
@Builder
public class VideoSyncProcessConfig {

    /**
     * 视频类型
     * <p>
     * 示例:
     * <pre><code>
     * "TV"
     * </code></pre>
     * </p>
     */
    private String videoType;

    /**
     * 当前页码
     * <p>
     * 示例:
     * <pre><code>
     * 1
     * </code></pre>
     * </p>
     */
    private int currentPageInPage;

    /**
     * 每页总数
     * <p>
     * 示例:
     * <pre><code>
     * 10
     * </code></pre>
     * </p>
     */
    private int totalInPage;

    /**
     * 页码
     * <p>
     * 示例:
     * <pre><code>
     * 1
     * </code></pre>
     * </p>
     */
    private int page;

    /**
     * 总页数
     * <p>
     * 示例:
     * <pre><code>
     * 100
     * </code></pre>
     * </p>
     */
    private int totalPages;
}
