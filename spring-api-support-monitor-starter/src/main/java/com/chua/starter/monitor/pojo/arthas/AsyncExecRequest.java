package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * 异步执行请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class AsyncExecRequest {
    /**
     * 节点ID（必填）
     */
    private String nodeId;
    /**
     * 要执行的 arthas 命令（必填）
     */
    private String command;
}
