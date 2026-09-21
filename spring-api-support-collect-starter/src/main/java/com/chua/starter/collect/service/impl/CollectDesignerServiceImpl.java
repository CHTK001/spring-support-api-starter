package com.chua.starter.collect.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.collect.engine.CollectBuiltinInvoker;
import com.chua.starter.collect.engine.CollectExecutorRegistry;
import com.chua.starter.collect.engine.spi.CollectShapeMeta;
import com.chua.starter.collect.entity.CollectFlow;
import com.chua.starter.collect.job.CollectFlowJobHandler;
import com.chua.starter.collect.mapper.CollectFlowMapper;
import com.chua.starter.collect.mapper.CollectTaskMapper;
import com.chua.starter.collect.properties.CollectProperties;
import com.chua.starter.collect.service.CollectDesignerService;
import com.chua.starter.collect.service.CollectExecutionService;
import com.chua.starter.collect.service.CollectFlowService;
import com.chua.starter.collect.service.CollectRecordService;
import com.chua.starter.collect.support.CollectCronSupport;
import com.chua.starter.collect.vo.CollectRuntimeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 设计器与运行时概览服务实现。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectDesignerServiceImpl implements CollectDesignerService {

    /**
     * 定时预览最多返回次数。
     */
    private static final int MAX_PREVIEW = 10;

    /**
     * 未删除条件值。
     */
    private static final Integer ALIVE = 0;

    /**
     * 启用调度条件值。
     */
    private static final Integer ENABLED = 1;

    /**
     * 节点执行器注册表。
     */
    private final CollectExecutorRegistry registry;

    /**
     * 平台配置。
     */
    private final CollectProperties properties;

    /**
     * 流程表访问。
     */
    private final CollectFlowMapper flowMapper;

    /**
     * 任务表访问。
     */
    private final CollectTaskMapper taskMapper;

    /**
     * 结果服务，取结果总数。
     */
    private final CollectRecordService recordService;

    /**
     * 执行服务，取本机执行中标识。
     */
    private final CollectExecutionService executionService;

    /**
     * 流程服务，取调度台账的登记情况。
     */
    private final CollectFlowService flowService;

    @Override
    public List<CollectShapeMeta> metas() {
        return registry.metas();
    }

    @Override
    public CollectRuntimeVO runtime() {
        CollectRuntimeVO vo = new CollectRuntimeVO();
        vo.setEnable(properties.isEnable());
        vo.setExecutorId(executionService.executorId());
        vo.setRunningTasks(executionService.runningCount());
        vo.setFlowCount(flowMapper.selectCount(Wrappers.<CollectFlow>lambdaQuery()
                .eq(CollectFlow::getCollectFlowIsDeleted, ALIVE)));
        vo.setEnabledFlowCount(flowMapper.selectCount(Wrappers.<CollectFlow>lambdaQuery()
                .eq(CollectFlow::getCollectFlowIsDeleted, ALIVE)
                .eq(CollectFlow::getCollectFlowStatus, ENABLED)));
        vo.setTaskCount(taskMapper.selectCount(null));
        vo.setRecordCount(recordService.countRecords(null));
        vo.setSchedulerGroup(CollectFlowJobHandler.JOB_GROUP);
        vo.setScheduledJobCount((long) flowService.scheduledJobCount());
        vo.setTaskTimeoutMs(properties.getTaskTimeoutMs());
        vo.setMaxRecordPerTask(properties.getMaxRecordPerTask());
        vo.setDefaultThreadCount(properties.getDefaultThreadCount());
        vo.setRequestTimeoutMs(properties.getRequestTimeoutMs());
        vo.setShapes(registry.metas());
        vo.setBuiltinFunctions(CollectBuiltinInvoker.builtinNames().stream().sorted().toList());
        return vo;
    }

    @Override
    public List<LocalDateTime> cronPreview(String cron, int times) {
        CollectCronSupport.validate(cron);
        int count = Math.min(Math.max(times, 1), MAX_PREVIEW);
        long[] next = CollectCronSupport.nextList(cron, System.currentTimeMillis(), count);
        List<LocalDateTime> preview = new ArrayList<>();
        for (long millis : next) {
            preview.add(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
        }
        return preview;
    }
}
