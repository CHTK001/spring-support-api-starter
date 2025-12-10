package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * Thread 命令请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class ThreadRequest {
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 线程ID（优先）
     */
    private String threadId;
    /**
     * 仅阻塞线程
     */
    private Boolean blocking;
    /**
     * 数量限制
     */
    private Integer count;
    /**
     * 收集毫秒数
     */
    private Integer collectMillis;
}
