package com.chua.starter.workflow.engine;

import lombok.Data;

/**
 * 节点运行时信息
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
public class WfNodeRuntime {
    private Integer nodeId;
    private String nodeName;
    private String nodeType;
    private String assigneeType;
    private String assigneeValue;
    private String nodeConfig;
}

