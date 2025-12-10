package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * 执行请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class ExecRequest {
    /**
     * 节点ID（必填）
     */
    private String nodeId;
    /**
     * 要执行的命令（必填）
     */
    private String command;
    /**
     * 收集输出的毫秒数（可选，默认3000ms）
     */
    private Integer collectMillis;
}
