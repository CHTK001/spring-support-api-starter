package com.chua.starter.collect.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.collect.engine.CollectEngine;
import com.chua.starter.collect.entity.CollectTask;
import com.chua.starter.collect.entity.CollectTaskLog;
import com.chua.starter.collect.mapper.CollectTaskLogMapper;
import com.chua.starter.collect.mapper.CollectTaskMapper;
import com.chua.starter.collect.service.CollectTaskService;
import com.chua.starter.collect.support.CollectTaskStatus;
import com.chua.starter.collect.vo.CollectTaskLogVO;
import com.chua.starter.collect.vo.CollectTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 采集任务查询服务实现。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectTaskServiceImpl extends ServiceImpl<CollectTaskMapper, CollectTask>
        implements CollectTaskService {

    /**
     * 日志接口单次返回上限，取最近的一批。
     */
    private static final int LOG_LIMIT = 1000;

    /**
     * 节点日志表访问。
     */
    private final CollectTaskLogMapper taskLogMapper;

    /**
     * 执行引擎，用于标记任务是否仍在本机跑。
     */
    private final CollectEngine engine;

    @Override
    public IPage<CollectTaskVO> pageTasks(IPage<CollectTask> page, CollectTask condition) {
        CollectTask safe = condition == null ? new CollectTask() : condition;
        IPage<CollectTask> entityPage = page(page, Wrappers.<CollectTask>lambdaQuery()
                .eq(safe.getCollectTaskFlowId() != null, CollectTask::getCollectTaskFlowId, safe.getCollectTaskFlowId())
                .eq(safe.getCollectTaskStatus() != null && !safe.getCollectTaskStatus().isBlank(),
                        CollectTask::getCollectTaskStatus, safe.getCollectTaskStatus())
                .eq(safe.getCollectTaskTriggerType() != null && !safe.getCollectTaskTriggerType().isBlank(),
                        CollectTask::getCollectTaskTriggerType, safe.getCollectTaskTriggerType())
                .like(safe.getCollectTaskNo() != null && !safe.getCollectTaskNo().isBlank(),
                        CollectTask::getCollectTaskNo, safe.getCollectTaskNo())
                .like(safe.getCollectTaskFlowName() != null && !safe.getCollectTaskFlowName().isBlank(),
                        CollectTask::getCollectTaskFlowName, safe.getCollectTaskFlowName())
                .orderByDesc(CollectTask::getCollectTaskId));
        return entityPage.convert(this::toVo);
    }

    @Override
    public CollectTaskVO detail(Long taskId) {
        return toVo(requireTask(taskId));
    }

    @Override
    public List<CollectTaskLogVO> logs(Long taskId) {
        requireTask(taskId);
        List<CollectTaskLog> rows = taskLogMapper.selectList(Wrappers.<CollectTaskLog>lambdaQuery()
                .eq(CollectTaskLog::getCollectTaskLogTaskId, taskId)
                .orderByDesc(CollectTaskLog::getCollectTaskLogId)
                .last("LIMIT " + LOG_LIMIT));
        Collections.reverse(rows);
        List<CollectTaskLogVO> list = new ArrayList<>(rows.size());
        for (CollectTaskLog row : rows) {
            list.add(toLogVo(row));
        }
        return list;
    }

    @Override
    public List<CollectTaskLogVO> nodeStates(Long taskId) {
        requireTask(taskId);
        List<CollectTaskLog> rows = taskLogMapper.selectList(Wrappers.<CollectTaskLog>lambdaQuery()
                .eq(CollectTaskLog::getCollectTaskLogTaskId, taskId)
                .orderByDesc(CollectTaskLog::getCollectTaskLogId)
                .last("LIMIT " + LOG_LIMIT));
        Map<String, CollectTaskLog> latest = new LinkedHashMap<>();
        for (CollectTaskLog row : rows) {
            // 子流程与宿主流程的节点ID 可以重名，按 流程+节点 去重才不会互相顶掉
            latest.putIfAbsent(row.getCollectTaskLogFlowId() + ":" + row.getCollectTaskLogNodeId(), row);
        }
        List<CollectTaskLogVO> list = new ArrayList<>(latest.size());
        for (CollectTaskLog row : latest.values()) {
            list.add(toLogVo(row));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Long taskId) {
        CollectTask task = requireTask(taskId);
        if (CollectTaskStatus.RUNNING.equals(task.getCollectTaskStatus()) || engine.isRunning(taskId)) {
            throw new IllegalArgumentException("任务正在执行，请先停止再删除");
        }
        taskLogMapper.delete(Wrappers.<CollectTaskLog>lambdaQuery()
                .eq(CollectTaskLog::getCollectTaskLogTaskId, taskId));
        return removeById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanTasks(Integer flowId, LocalDateTime before) {
        if (before == null) {
            throw new IllegalArgumentException("必须指定清理时间点");
        }
        List<CollectTask> stale = list(Wrappers.<CollectTask>lambdaQuery()
                .eq(flowId != null, CollectTask::getCollectTaskFlowId, flowId)
                .ne(CollectTask::getCollectTaskStatus, CollectTaskStatus.RUNNING)
                .lt(CollectTask::getCollectTaskBeginTime, before));
        if (stale.isEmpty()) {
            return 0;
        }
        List<Long> ids = new ArrayList<>(stale.size());
        for (CollectTask task : stale) {
            ids.add(task.getCollectTaskId());
        }
        taskLogMapper.delete(Wrappers.<CollectTaskLog>lambdaQuery().in(CollectTaskLog::getCollectTaskLogTaskId, ids));
        removeByIds(ids);
        return ids.size();
    }

    /**
     * 按主键取任务。
     *
     * @param taskId 任务ID
     * @return 任务
     */
    private CollectTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        CollectTask task = getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        return task;
    }

    /**
     * 实体转出参。
     *
     * @param task 任务
     * @return 出参
     */
    private CollectTaskVO toVo(CollectTask task) {
        CollectTaskVO vo = new CollectTaskVO();
        vo.setCollectTaskId(task.getCollectTaskId());
        vo.setCollectTaskNo(task.getCollectTaskNo());
        vo.setCollectTaskFlowId(task.getCollectTaskFlowId());
        vo.setCollectTaskFlowName(task.getCollectTaskFlowName());
        vo.setCollectTaskStatus(task.getCollectTaskStatus());
        vo.setCollectTaskTriggerType(task.getCollectTaskTriggerType());
        vo.setCollectTaskParams(task.getCollectTaskParams());
        vo.setCollectTaskBeginTime(task.getCollectTaskBeginTime());
        vo.setCollectTaskEndTime(task.getCollectTaskEndTime());
        vo.setCollectTaskCost(task.getCollectTaskCost());
        vo.setCollectTaskNodeCount(task.getCollectTaskNodeCount());
        vo.setCollectTaskRequestCount(task.getCollectTaskRequestCount());
        vo.setCollectTaskRecordCount(task.getCollectTaskRecordCount());
        vo.setCollectTaskError(task.getCollectTaskError());
        vo.setCollectTaskExecutorId(task.getCollectTaskExecutorId());
        vo.setRunning(engine.isRunning(task.getCollectTaskId()));
        vo.setCreateName(task.getCreateName());
        return vo;
    }

    /**
     * 日志实体转出参。
     *
     * @param row 日志
     * @return 出参
     */
    private CollectTaskLogVO toLogVo(CollectTaskLog row) {
        CollectTaskLogVO vo = new CollectTaskLogVO();
        vo.setCollectTaskLogId(row.getCollectTaskLogId());
        vo.setCollectTaskLogTaskId(row.getCollectTaskLogTaskId());
        vo.setCollectTaskLogFlowId(row.getCollectTaskLogFlowId());
        vo.setCollectTaskLogNodeId(row.getCollectTaskLogNodeId());
        vo.setCollectTaskLogNodeName(row.getCollectTaskLogNodeName());
        vo.setCollectTaskLogNodeShape(row.getCollectTaskLogNodeShape());
        vo.setCollectTaskLogIteration(row.getCollectTaskLogIteration());
        vo.setCollectTaskLogStatus(row.getCollectTaskLogStatus());
        vo.setCollectTaskLogCost(row.getCollectTaskLogCost());
        vo.setCollectTaskLogMessage(row.getCollectTaskLogMessage());
        vo.setCollectTaskLogOutput(row.getCollectTaskLogOutput());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
