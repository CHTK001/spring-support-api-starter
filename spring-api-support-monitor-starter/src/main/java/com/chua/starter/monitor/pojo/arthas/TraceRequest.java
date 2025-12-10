package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * Trace 命令请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class TraceRequest {
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 类名模式（必填）
     */
    private String classPattern;
    /**
     * 方法名模式（可选，默认 *）
     */
    private String methodPattern;
    /**
     * 条件表达式（可选）
     */
    private String condition;
    /**
     * 使用正则 -E
     */
    private Boolean useRegex;
    /**
     * 数量限制 -n
     */
    private Integer count;
    /**
     * 展开层级 -x
     */
    private Integer expand;
    /**
     * 收集毫秒数
     */
    private Integer collectMillis;
}
