package com.chua.starter.collect.job;

import com.chua.starter.collect.service.CollectExecutionService;
import com.chua.starter.job.support.handler.JobHandler;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 采集流程的调度执行器：一个流程对应一个处理器实例，名称为 {@code collect-flow-<flowId>}。
 *
 * <p>job-starter 派发时只带处理器名称，不带执行参数，所以流程ID只能由处理器实例自己持有；
 * 执行时才回表读最新 DSL，改了流程图不需要重新登记调度。</p>
 *
 * <p>执行服务用 {@link ObjectProvider} 延迟取，避免流程服务与执行服务在容器启动期互相依赖。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class CollectFlowJobHandler implements JobHandler {

    /**
     * 流程调度在 sys_job 中的键前缀。
     */
    public static final String JOB_KEY_PREFIX = "collect-flow-";

    /**
     * 流程调度分组。
     */
    public static final String JOB_GROUP = "collect-flow";

    /**
     * 流程标识。
     */
    private final Integer flowId;

    /**
     * 流程名称，仅用于描述文案。
     */
    private final String flowName;

    /**
     * 执行服务提供者。
     */
    private final ObjectProvider<CollectExecutionService> executionServiceProvider;

    /**
     * 构造流程调度执行器。
     *
     * @param flowId                     流程ID
     * @param flowName                   流程名称
     * @param executionServiceProvider   执行服务
     */
    public CollectFlowJobHandler(Integer flowId, String flowName,
                                 ObjectProvider<CollectExecutionService> executionServiceProvider) {
        this.flowId = flowId;
        this.flowName = flowName;
        this.executionServiceProvider = executionServiceProvider;
    }

    /**
     * 推导流程对应的调度任务键。
     *
     * @param flowId 流程ID
     * @return 调度任务键
     */
    public static String jobKeyOf(Integer flowId) {
        return JOB_KEY_PREFIX + flowId;
    }

    @Override
    public String name() {
        return jobKeyOf(flowId);
    }

    @Override
    public String desc() {
        return "数据采集流程: " + flowName;
    }

    @Override
    public void execute() {
        executionServiceProvider.getObject().runScheduled(flowId);
    }
}
