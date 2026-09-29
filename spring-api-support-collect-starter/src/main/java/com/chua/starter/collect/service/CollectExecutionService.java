package com.chua.starter.collect.service;

import com.chua.starter.collect.dto.CollectDslDTO;
import com.chua.starter.collect.dto.CollectRunDTO;
import com.chua.starter.collect.vo.CollectTaskVO;

/**
 * 采集执行服务。
 *
 * <p>任务是先落库再异步跑的：接口立即拿到任务号，进度与结果靠轮询任务与节点日志获取。
 * 同一流程的并发受 {@code collect_flow_concurrency} 限制，超限时调度触发会被跳过并记日志。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface CollectExecutionService {

    /**
     * 手动运行已保存的流程。
     *
     * @param dto 运行入参
     * @return 任务出参
     */
    CollectTaskVO runFlow(CollectRunDTO dto);

    /**
     * 按已保存的流程图执行一次调度触发，供定时器调用。
     *
     * @param flowId 流程ID
     * @return 任务出参
     */
    CollectTaskVO runScheduled(Integer flowId);

    /**
     * 试运行未保存的草稿，结果不写结果表。
     *
     * @param dsl 流程图 DSL
     * @return 任务出参
     */
    CollectTaskVO runDraft(CollectDslDTO dsl);

    /**
     * 停止运行中的任务。
     *
     * @param taskId 任务ID
     * @return 是否命中本机运行中的任务
     */
    boolean stop(Long taskId);

    /**
     * 某流程当前排队与执行中的任务数。
     *
     * @param flowId 流程ID
     * @return 任务数
     */
    int inflight(Integer flowId);

    /**
     * 本机执行中的任务数。
     *
     * @return 任务数
     */
    int runningCount();

    /**
     * 本机执行器标识，主机名加进程号。
     *
     * @return 执行器标识
     */
    String executorId();
}
