package com.chua.starter.workflow.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.workflow.dto.WfTaskProcessRequest;
import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategyFactory;
import com.chua.starter.workflow.engine.WorkflowEngine;
import com.chua.starter.workflow.entity.*;
import com.chua.starter.workflow.mapper.*;
import com.chua.starter.workflow.service.WfTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务服务实现
 *
 * @author CH
 * @since 2026/5/28
 */
@Service
@RequiredArgsConstructor
public class WfTaskServiceImpl implements WfTaskService {

    private final WfInstMapper wfInstMapper;
    private final WfDefNodeMapper wfDefNodeMapper;
    private final WfDefEdgeMapper wfDefEdgeMapper;
    private final WfTaskMapper wfTaskMapper;
    private final WfTaskHistoryMapper wfTaskHistoryMapper;
    private final AssignmentStrategyFactory strategyFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> approve(WfTaskProcessRequest request, Integer userId, String userName) {
        WfTask task = wfTaskMapper.selectById(request.getWfTaskId());
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        if (!WorkflowEngine.canApproveTask(task.getTaskStatus())) {
            return ReturnResult.illegal("当前任务状态不可审批: " + task.getTaskStatus());
        }

        // 检查是否是自己的待办
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            return ReturnResult.illegal("非当前审批人的任务");
        }

        WfInst inst = wfInstMapper.selectById(task.getWfInstId());
        if (inst == null || !WorkflowEngine.canApprove(inst.getInstStatus())) {
            return ReturnResult.illegal("流程实例状态异常");
        }

        // 计算耗时
        long duration = task.getCreateTime() != null
                ? ChronoUnit.SECONDS.between(task.getCreateTime(), LocalDateTime.now())
                : 0;

        // 更新任务状态
        int updated = wfTaskMapper.updateTaskStatus(task.getWfTaskId(),
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_APPROVED,
                request.getComment());
        if (updated == 0) {
            return ReturnResult.illegal("审批失败，任务状态已变更");
        }

        // 记录历史
        recordHistory(inst.getWfInstId(), task.getWfTaskId(), userId, userName,
                "APPROVE", request.getComment(), duration);

        // 检查是否当前节点所有任务都已审批
        Integer currentNodeId = task.getWfDefNodeId();
        List<WfTask> pendingAtNode = wfTaskMapper.selectByInstId(inst.getWfInstId())
                .stream()
                .filter(t -> Objects.equals(t.getWfDefNodeId(), currentNodeId)
                        && WorkflowEngine.TASK_STATUS_PENDING.equals(t.getTaskStatus()))
                .toList();

        if (!pendingAtNode.isEmpty()) {
            // 还有同节点待办(并行审批)，等待
            return ReturnResult.ok(true);
        }

        // 当前节点全部完成，路由到下一节点
        return routeToNext(inst, currentNodeId, userId, userName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> reject(WfTaskProcessRequest request, Integer userId, String userName) {
        WfTask task = wfTaskMapper.selectById(request.getWfTaskId());
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        if (!WorkflowEngine.canRejectTask(task.getTaskStatus())) {
            return ReturnResult.illegal("当前任务状态不可驳回: " + task.getTaskStatus());
        }
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            return ReturnResult.illegal("非当前审批人的任务");
        }

        WfInst inst = wfInstMapper.selectById(task.getWfInstId());
        if (inst == null || !WorkflowEngine.canReject(inst.getInstStatus())) {
            return ReturnResult.illegal("流程实例状态异常");
        }

        long duration = task.getCreateTime() != null
                ? ChronoUnit.SECONDS.between(task.getCreateTime(), LocalDateTime.now())
                : 0;

        // 更新任务状态
        wfTaskMapper.updateTaskStatus(task.getWfTaskId(),
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_REJECTED,
                request.getComment());

        // 取消所有其他待办
        wfTaskMapper.cancelByInstId(inst.getWfInstId(),
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_CANCELLED);

        // 更新实例状态
        wfInstMapper.updateStatus(inst.getWfInstId(),
                WorkflowEngine.INST_STATUS_PENDING,
                WorkflowEngine.INST_STATUS_REJECTED, null);

        recordHistory(inst.getWfInstId(), task.getWfTaskId(), userId, userName,
                "REJECT", request.getComment(), duration);

        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> returnTask(WfTaskProcessRequest request, Integer userId, String userName) {
        WfTask task = wfTaskMapper.selectById(request.getWfTaskId());
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        if (!WorkflowEngine.canReturnTask(task.getTaskStatus())) {
            return ReturnResult.illegal("当前任务状态不可退回: " + task.getTaskStatus());
        }
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            return ReturnResult.illegal("非当前审批人的任务");
        }

        WfInst inst = wfInstMapper.selectById(task.getWfInstId());
        if (inst == null || !WorkflowEngine.canApprove(inst.getInstStatus())) {
            return ReturnResult.illegal("流程实例状态异常");
        }

        long duration = task.getCreateTime() != null
                ? ChronoUnit.SECONDS.between(task.getCreateTime(), LocalDateTime.now())
                : 0;

        // 更新当前任务状态
        wfTaskMapper.updateTaskStatus(task.getWfTaskId(),
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_RETURNED,
                request.getComment());

        // 取消其他待办
        wfTaskMapper.cancelByInstId(inst.getWfInstId(),
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_CANCELLED);

        // 查找上一个审批节点，创建新任务回到上一节点
        List<WfDefNode> allNodes = wfDefNodeMapper.selectByDefIdOrderBySort(inst.getWfDefId());
        List<WfDefEdge> allEdges = wfDefEdgeMapper.selectByDefId(inst.getWfDefId());
        Map<Integer, List<WfDefEdge>> adjacency = WorkflowEngine.buildAdjacency(allNodes, allEdges);

        // 反向查找: 谁指向当前节点
        Integer currentNodeId = task.getWfDefNodeId();
        Integer prevApprovalNodeId = findPreviousApprovalNode(currentNodeId, allNodes, allEdges);

        if (prevApprovalNodeId == null) {
            // 退回给发起人
            wfInstMapper.updateStatus(inst.getWfInstId(),
                    WorkflowEngine.INST_STATUS_PENDING,
                    WorkflowEngine.INST_STATUS_REJECTED, null);
        } else {
            // 创建上一节点任务
            WfDefNode prevNode = allNodes.stream()
                    .filter(n -> n.getWfDefNodeId().equals(prevApprovalNodeId))
                    .findFirst().orElse(null);
            if (prevNode != null) {
                // 当前实例由 WfInstServiceImpl 注入，无法直接调用 createTasksForNode
                // 通过策略创建任务
                createTaskForNode(inst, prevNode);
                wfInstMapper.updateStatus(inst.getWfInstId(),
                        WorkflowEngine.INST_STATUS_PENDING,
                        WorkflowEngine.INST_STATUS_PENDING, prevApprovalNodeId);
            }
        }

        recordHistory(inst.getWfInstId(), task.getWfTaskId(), userId, userName,
                "RETURN", request.getComment(), duration);

        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> transfer(WfTaskProcessRequest request, Integer userId, String userName) {
        if (request.getTransferUserId() == null) {
            return ReturnResult.illegal("转交目标用户不能为空");
        }

        WfTask task = wfTaskMapper.selectById(request.getWfTaskId());
        if (task == null) {
            return ReturnResult.illegal("任务不存在");
        }
        if (!WorkflowEngine.canTransferTask(task.getTaskStatus())) {
            return ReturnResult.illegal("当前任务状态不可转交: " + task.getTaskStatus());
        }
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            return ReturnResult.illegal("非当前审批人的任务");
        }

        long duration = task.getCreateTime() != null
                ? ChronoUnit.SECONDS.between(task.getCreateTime(), LocalDateTime.now())
                : 0;

        // 更新原任务为已转交
        wfTaskMapper.updateTaskStatus(task.getWfTaskId(),
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_TRANSFERRED,
                "转交给: " + request.getTransferUserName());

        // 创建新任务给目标用户
        WfTask newTask = new WfTask();
        newTask.setWfInstId(task.getWfInstId());
        newTask.setWfDefNodeId(task.getWfDefNodeId());
        newTask.setNodeName(task.getNodeName());
        newTask.setAssigneeId(request.getTransferUserId());
        newTask.setAssigneeName(request.getTransferUserName());
        newTask.setTaskStatus(WorkflowEngine.TASK_STATUS_PENDING);
        newTask.setSort(task.getSort());
        wfTaskMapper.insert(newTask);

        recordHistory(task.getWfInstId(), task.getWfTaskId(), userId, userName,
                "TRANSFER", "转交给 " + request.getTransferUserName(), duration);

        return ReturnResult.ok(true);
    }

    // ==================== 内部方法 ====================

    /**
     * 路由到下一节点
     */
    private ReturnResult<Boolean> routeToNext(WfInst inst, Integer currentNodeId,
                                              Integer userId, String userName) {
        List<WfDefNode> allNodes = wfDefNodeMapper.selectByDefIdOrderBySort(inst.getWfDefId());
        List<WfDefEdge> allEdges = wfDefEdgeMapper.selectByDefId(inst.getWfDefId());
        Map<Integer, List<WfDefEdge>> adjacency = WorkflowEngine.buildAdjacency(allNodes, allEdges);

        List<WfDefNode> nextSteps = WorkflowEngine.getNextNonConditionNodes(currentNodeId, allNodes, adjacency);

        if (nextSteps.isEmpty()) {
            // 无后续节点，检查是否走到了 END
            boolean reachedEnd = allNodes.stream()
                    .filter(n -> WorkflowEngine.NODE_TYPE_END.equals(n.getNodeType()))
                    .anyMatch(end -> {
                        List<WfDefNode> endPrev = WorkflowEngine.getNextNonConditionNodes(
                                end.getWfDefNodeId(), allNodes, adjacency);
                        return false;
                    });
            // 默认走 END
            wfInstMapper.updateStatus(inst.getWfInstId(),
                    WorkflowEngine.INST_STATUS_PENDING,
                    WorkflowEngine.INST_STATUS_APPROVED, null);
            inst.setInstStatus(WorkflowEngine.INST_STATUS_APPROVED);
            recordHistory(inst.getWfInstId(), null, userId, userName,
                    "APPROVE", "流程审批通过", null);
            return ReturnResult.ok(true);
        }

        // 处理下一审批节点
        List<WfDefNode> approvalNodes = nextSteps.stream()
                .filter(n -> WorkflowEngine.NODE_TYPE_APPROVAL.equals(n.getNodeType()))
                .toList();

        if (approvalNodes.isEmpty()) {
            // 后续无审批节点，直接完成
            wfInstMapper.updateStatus(inst.getWfInstId(),
                    WorkflowEngine.INST_STATUS_PENDING,
                    WorkflowEngine.INST_STATUS_APPROVED, null);
            recordHistory(inst.getWfInstId(), null, userId, userName,
                    "APPROVE", "流程审批通过", null);
            return ReturnResult.ok(true);
        }

        // 创建下一审批节点任务
        WfDefNode nextNode = approvalNodes.get(0);
        createTaskForNode(inst, nextNode);
        wfInstMapper.updateStatus(inst.getWfInstId(),
                WorkflowEngine.INST_STATUS_PENDING,
                WorkflowEngine.INST_STATUS_PENDING, nextNode.getWfDefNodeId());

        return ReturnResult.ok(true);
    }

    private void createTaskForNode(WfInst inst, WfDefNode node) {
        com.chua.starter.workflow.engine.AssignmentStrategy strategy =
                strategyFactory.getStrategy(node.getAssigneeType());
        List<AssigneeUser> assignees = strategy.assign(node.getAssigneeValue(), inst);
        int maxSort = wfTaskMapper.selectByInstId(inst.getWfInstId())
                .stream().mapToInt(t -> t.getSort() != null ? t.getSort() : 0)
                .max().orElse(0);

        for (AssigneeUser assignee : assignees) {
            WfTask task = new WfTask();
            task.setWfInstId(inst.getWfInstId());
            task.setWfDefNodeId(node.getWfDefNodeId());
            task.setNodeName(node.getNodeName());
            task.setAssigneeId(assignee.getUserId());
            task.setAssigneeName(assignee.getUserName());
            task.setTaskStatus(WorkflowEngine.TASK_STATUS_PENDING);
            task.setSort(maxSort + 1);
            wfTaskMapper.insert(task);
        }
    }

    /**
     * 反向查找上一审批节点
     */
    private Integer findPreviousApprovalNode(Integer currentNodeId,
                                             List<WfDefNode> allNodes,
                                             List<WfDefEdge> allEdges) {
        // 找谁指向当前节点
        List<WfDefEdge> incoming = allEdges.stream()
                .filter(e -> e.getTargetNodeId().equals(currentNodeId))
                .toList();
        if (incoming.isEmpty()) return null;

        for (WfDefEdge edge : incoming) {
            Integer sourceId = edge.getSourceNodeId();
            WfDefNode sourceNode = allNodes.stream()
                    .filter(n -> n.getWfDefNodeId().equals(sourceId))
                    .findFirst().orElse(null);
            if (sourceNode != null && WorkflowEngine.NODE_TYPE_APPROVAL.equals(sourceNode.getNodeType())) {
                return sourceId;
            }
            // 跳过 CONDITION 继续回溯
            if (sourceNode != null && WorkflowEngine.NODE_TYPE_CONDITION.equals(sourceNode.getNodeType())) {
                return findPreviousApprovalNode(sourceId, allNodes, allEdges);
            }
        }
        return null;
    }

    private void recordHistory(Integer instId, Integer taskId, Integer operatorId,
                               String operatorName, String operation, String comment, Long duration) {
        WfTaskHistory history = new WfTaskHistory();
        history.setWfInstId(instId);
        history.setWfTaskId(taskId);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setOperation(operation);
        history.setComment(comment);
        history.setDurationSeconds(duration);
        history.setCreateTime(LocalDateTime.now());
        wfTaskHistoryMapper.insert(history);
    }
}
