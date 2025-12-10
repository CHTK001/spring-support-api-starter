package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * 中断任务请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class InterruptJobRequest {
    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 任务ID
     */
    private Integer jobId;
}
