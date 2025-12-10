package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * 拉取结果请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class PullResultsRequest {
    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 消费者ID
     */
    private String consumerId;
}
