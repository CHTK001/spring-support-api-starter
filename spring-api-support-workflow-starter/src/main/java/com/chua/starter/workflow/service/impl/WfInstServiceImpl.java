package com.chua.starter.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.workflow.dto.WfInstCreateRequest;
import com.chua.starter.workflow.dto.WfInstVO;
import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.engine.AssignmentStrategyFactory;
import com.chua.starter.workflow.engine.WorkflowEngine;
import com.chua.starter.workflow.entity.*;
import com.chua.starter.workflow.mapper.*;
import com.chua.starter.workflow.service.WfInstService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程实例服务实现
 *
 * @author CH
 * @since 2026/5/28
 */
@Service
@RequiredArgsConstructor
public class WfInstServiceImpl extends ServiceImpl<WfInstMapper, WfInst> implements WfInstService {

    private final WfDefMapper wfDefMapper;
    private final WfDefNodeMapper wfDefNodeMapper;
    private final WfDefEdgeMapper wfDefEdgeMapper;
    private final WfTaskMapper wfTaskMapper;
    private final WfTaskHistoryMapper wfTaskHistoryMapper;
    private final AssignmentStrategyFactory strategyFactory;

    @Override
    public IPage<WfInst> pageMyApply(Query<WfInst> query, WfInst inst, Integer userId) {
        LambdaQueryWrapper<WfInst> wrapper = new LambdaQueryWrapper<WfInst>()
                .eq(WfInst::getStarterId, userId)
                .orderByDesc(WfInst::getCreateTime);
        return baseMapper.selectPage(query.createPage(), wrapper);
    }

    @Override
    public IPage<WfInst> pageMyTodo(Query<WfInst> query, Integer userId) {
        // 子查询: 先查待办任务对应的 instId
        List<WfTask> tasks = wfTaskMapper.selectTodoByUserId(userId);
        if (tasks.isEmpty()) {
            return new Page<>(query.getPage(), query.getPageSize(), 0);
        }
        Set<Integer> instIds = tasks.stream()
                .map(WfTask::getWfInstId)
                .collect(Collectors.toSet());
        Page<WfInst> page = baseMapper.selectPage(query.createPage(),
                new LambdaQueryWrapper<WfInst>()
                        .in(WfInst::getWfInstId, instIds)
                        .orderByDesc(WfInst::getCreateTime));
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<WfInst> create(WfInstCreateRequest request, Integer userId, String userName, Integer deptId) {
        // 幂等检查
        if (request.getIdempotentKey() != null && !request.getIdempotentKey().isBlank()) {
            WfInst exist = baseMapper.selectByIdempotentKey(request.getIdempotentKey());
            if (exist != null) {
                return ReturnResult.ok(exist);
            }
        }

        // 校验流程定义
        WfDef def = wfDefMapper.selectById(request.getWfDefId());
        if (def == null) {
            return ReturnResult.illegal("流程定义不存在");
        }
        if (def.getWfDefStatus() != 1) {
            return ReturnResult.illegal("流程定义未发布");
        }

        WfInst inst = new WfInst();
        inst.setWfDefId(request.getWfDefId());
        inst.setBusinessType(request.getBusinessType());
        inst.setBusinessId(request.getBusinessId());
        inst.setInstTitle(request.getInstTitle());
        inst.setFormData(request.getFormData() != null ? Json.toJson(request.getFormData()) : null);
        inst.setStarterId(userId);
        inst.setDeptId(deptId);
        inst.setInstStatus(WorkflowEngine.INST_STATUS_DRAFT);
        inst.setIdempotentKey(request.getIdempotentKey());
        baseMapper.insert(inst);

        return ReturnResult.ok(inst);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> submit(Integer instId, Integer userId) {
        WfInst inst = baseMapper.selectById(instId);
        if (inst == null) {
            return ReturnResult.illegal("流程实例不存在");
        }
        if (!Objects.equals(inst.getStarterId(), userId)) {
            return ReturnResult.illegal("只能提交自己的流程");
        }
        if (!WorkflowEngine.canSubmit(inst.getInstStatus())) {
            return ReturnResult.illegal("当前状态不可提交: " + inst.getInstStatus());
        }

        // 获取流程节点
        Integer defId = inst.getWfDefId();
        List<WfDefNode> nodes = wfDefNodeMapper.selectByDefIdOrderBySort(defId);
        List<WfDefEdge> edges = wfDefEdgeMapper.selectByDefId(defId);
        Map<Integer, List<WfDefEdge>> adjacency = WorkflowEngine.buildAdjacency(nodes, edges);

        // 查找起始节点后的第一个审批节点
        WfDefNode startNode = WorkflowEngine.findStartNode(nodes);
        List<WfDefNode> firstSteps = WorkflowEngine.getNextNonConditionNodes(
                startNode.getWfDefNodeId(), nodes, adjacency);

        List<WfDefNode> approvalNodes = firstSteps.stream()
                .filter(n -> WorkflowEngine.NODE_TYPE_APPROVAL.equals(n.getNodeType()))
                .toList();

        if (approvalNodes.isEmpty()) {
            // 无审批节点：直接完成
            int updated = baseMapper.updateStatus(instId,
                    WorkflowEngine.INST_STATUS_DRAFT,
                    WorkflowEngine.INST_STATUS_APPROVED, null);
            if (updated == 0) {
                return ReturnResult.illegal("提交失败，状态已变更");
            }
            return ReturnResult.ok(true);
        }

        // 为第一个审批节点创建任务
        WfDefNode firstApprovalNode = approvalNodes.get(0);
        createTasksForNode(inst, firstApprovalNode, 1);

        // 更新实例状态
        int updated = baseMapper.updateStatus(instId,
                WorkflowEngine.INST_STATUS_DRAFT,
                WorkflowEngine.INST_STATUS_PENDING,
                firstApprovalNode.getWfDefNodeId());
        if (updated == 0) {
            // 回滚已创建的任务
            throw new IllegalStateException("提交失败，状态已变更");
        }

        // 记录历史
        recordHistory(instId, null, userId, inst.getCreateName(),
                "SUBMIT", "提交审批", null);

        return ReturnResult.ok(true);
    }

    @Override
    public WfInstVO getDetail(Integer instId, Integer userId) {
        WfInst inst = baseMapper.selectById(instId);
        if (inst == null) return null;

        WfInstVO vo = new WfInstVO();
        copyInstFields(inst, vo);

        // 流程定义名称
        WfDef def = wfDefMapper.selectById(inst.getWfDefId());
        if (def != null) {
            vo.setWfDefName(def.getWfDefName());
        }

        // 任务列表
        List<WfTask> tasks = wfTaskMapper.selectByInstId(instId);
        vo.setTasks(tasks);
        vo.setCurrentTasks(tasks.stream()
                .filter(t -> WorkflowEngine.TASK_STATUS_PENDING.equals(t.getTaskStatus()))
                .toList());

        // 历史
        vo.setHistories(wfTaskHistoryMapper.selectByInstId(instId));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> withdraw(Integer instId, Integer userId) {
        WfInst inst = baseMapper.selectById(instId);
        if (inst == null) {
            return ReturnResult.illegal("流程实例不存在");
        }
        if (!Objects.equals(inst.getStarterId(), userId)) {
            return ReturnResult.illegal("只能撤销自己的流程");
        }
        if (!WorkflowEngine.canWithdraw(inst.getInstStatus())) {
            return ReturnResult.illegal("当前状态不可撤销: " + inst.getInstStatus());
        }

        // 取消所有待办任务
        wfTaskMapper.cancelByInstId(instId,
                WorkflowEngine.TASK_STATUS_PENDING,
                WorkflowEngine.TASK_STATUS_CANCELLED);

        // 更新实例状态
        int updated = baseMapper.updateStatus(instId,
                WorkflowEngine.INST_STATUS_PENDING,
                WorkflowEngine.INST_STATUS_WITHDRAWN, null);
        if (updated == 0) {
            return ReturnResult.illegal("撤销失败，状态已变更");
        }

        recordHistory(instId, null, userId, inst.getCreateName(),
                "WITHDRAW", "撤销申请", null);
        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteInst(Integer instId, Integer userId) {
        WfInst inst = baseMapper.selectById(instId);
        if (inst == null) {
            return ReturnResult.illegal("流程实例不存在");
        }
        if (!Objects.equals(inst.getStarterId(), userId)) {
            return ReturnResult.illegal("只能删除自己的流程");
        }
        if (!WorkflowEngine.canDelete(inst.getInstStatus())) {
            return ReturnResult.illegal("当前状态不可删除: " + inst.getInstStatus());
        }
        baseMapper.deleteById(instId);
        return ReturnResult.ok(true);
    }

    // ==================== 内部方法 ====================

    /**
     * 为指定节点创建审批任务
     */
    void createTasksForNode(WfInst inst, WfDefNode node, int sort) {
        String assigneeType = node.getAssigneeType();
        String assigneeValue = node.getAssigneeValue();

        if (assigneeType == null || assigneeType.isBlank()) {
            throw new IllegalStateException("节点[" + node.getNodeName() + "]未配置审批人");
        }

        AssignmentStrategy strategy = strategyFactory.getStrategy(assigneeType);
        List<AssigneeUser> assignees = strategy.assign(assigneeValue, inst);

        if (assignees.isEmpty()) {
            throw new IllegalStateException("节点[" + node.getNodeName() + "]未匹配到审批人");
        }

        for (AssigneeUser assignee : assignees) {
            WfTask task = new WfTask();
            task.setWfInstId(inst.getWfInstId());
            task.setWfDefNodeId(node.getWfDefNodeId());
            task.setNodeName(node.getNodeName());
            task.setAssigneeId(assignee.getUserId());
            task.setAssigneeName(assignee.getUserName());
            task.setTaskStatus(WorkflowEngine.TASK_STATUS_PENDING);
            task.setSort(sort);
            wfTaskMapper.insert(task);
        }
    }

    void recordHistory(Integer instId, Integer taskId, Integer operatorId,
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

    private void copyInstFields(WfInst src, WfInst dst) {
        dst.setWfInstId(src.getWfInstId());
        dst.setWfDefId(src.getWfDefId());
        dst.setBusinessType(src.getBusinessType());
        dst.setBusinessId(src.getBusinessId());
        dst.setInstTitle(src.getInstTitle());
        dst.setFormData(src.getFormData());
        dst.setStarterId(src.getStarterId());
        dst.setDeptId(src.getDeptId());
        dst.setInstStatus(src.getInstStatus());
        dst.setIdempotentKey(src.getIdempotentKey());
        dst.setCurrentNodeId(src.getCurrentNodeId());
        dst.setExpireTime(src.getExpireTime());
        dst.setFinishTime(src.getFinishTime());
        dst.setCreateTime(src.getCreateTime());
        dst.setCreateBy(src.getCreateBy());
        dst.setCreateName(src.getCreateName());
        dst.setUpdateTime(src.getUpdateTime());
        dst.setUpdateBy(src.getUpdateBy());
        dst.setUpdateName(src.getUpdateName());
    }
}
