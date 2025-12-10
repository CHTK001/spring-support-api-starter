package com.chua.starter.monitor.pojo.arthas;

import lombok.Data;

/**
 * 简单请求体
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class SimpleRequest {
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 收集毫秒数
     */
    private Integer collectMillis;
}
