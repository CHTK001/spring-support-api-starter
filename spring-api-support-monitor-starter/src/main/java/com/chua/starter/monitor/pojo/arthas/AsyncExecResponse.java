package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * 异步执行响应
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class AsyncExecResponse {
    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 消费者ID
     */
    private String consumerId;
    /**
     * 任务ID
     */
    private Integer jobId;
}
