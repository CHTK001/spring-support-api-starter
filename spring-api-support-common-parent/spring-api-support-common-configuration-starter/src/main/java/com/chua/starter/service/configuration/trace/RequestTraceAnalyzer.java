package com.chua.starter.service.configuration.trace;

import java.util.Collection;

/**
* 请求追踪分析器
*
* @since 2026/03/25
* @author CH
 */
public interface RequestTraceAnalyzer {

    /**
    * 分析当前请求是否命中信号
    *
    * @param context 分析上下文
    * @return 命中的信号
     */
    Collection<RequestTraceSignal> analyze(RequestTraceAnalysisContext context);
}
