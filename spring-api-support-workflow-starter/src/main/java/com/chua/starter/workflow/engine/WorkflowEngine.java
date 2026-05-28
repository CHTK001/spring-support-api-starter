package com.chua.starter.workflow.engine;

import com.chua.starter.workflow.entity.WfDefEdge;
import com.chua.starter.workflow.entity.WfDefNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流引擎 — 状态机路由、节点拓扑、校验
 * <p>
 * 职责:
 * 1. 实例/任务状态机校验 (canSubmit, canApprove, etc.)
 * 2. 节点拓扑: 邻接表构建、前驱/后继查询
 * 3. 环路检测 (DFS)
 * 4. 审批节点排序
 * </p>
 *
 * @author CH
 * @since 2026/5/28
 */
public class WorkflowEngine {

    // ==================== 实例状态常量 ====================
    public static final String INST_STATUS_DRAFT = "DRAFT";
    public static final String INST_STATUS_PENDING = "PENDING";
    public static final String INST_STATUS_APPROVED = "APPROVED";
    public static final String INST_STATUS_REJECTED = "REJECTED";
    public static final String INST_STATUS_WITHDRAWN = "WITHDRAWN";

    // ==================== 任务状态常量 ====================
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_APPROVED = "APPROVED";
    public static final String TASK_STATUS_REJECTED = "REJECTED";
    public static final String TASK_STATUS_RETURNED = "RETURNED";
    public static final String TASK_STATUS_TRANSFERRED = "TRANSFERRED";
    public static final String TASK_STATUS_CANCELLED = "CANCELLED";

    // ==================== 节点类型常量 ====================
    public static final String NODE_TYPE_START = "START";
    public static final String NODE_TYPE_END = "END";
    public static final String NODE_TYPE_APPROVAL = "APPROVAL";
    public static final String NODE_TYPE_CC = "CC";
    public static final String NODE_TYPE_CONDITION = "CONDITION";

    private WorkflowEngine() {
    }

    // ==================== 实例状态校验 ====================

    private static final Set<String> START_STATUSES = Set.of(INST_STATUS_DRAFT);
    private static final Set<String> APPROVE_STATUSES = Set.of(INST_STATUS_PENDING);
    private static final Set<String> REJECT_STATUSES = Set.of(INST_STATUS_PENDING);
    private static final Set<String> WITHDRAW_STATUSES = Set.of(INST_STATUS_PENDING);
    private static final Set<String> DELETE_STATUSES = Set.of(INST_STATUS_DRAFT, INST_STATUS_REJECTED, INST_STATUS_WITHDRAWN);

    public static boolean canSubmit(String status) {
        return START_STATUSES.contains(status);
    }

    public static boolean canApprove(String status) {
        return APPROVE_STATUSES.contains(status);
    }

    public static boolean canReject(String status) {
        return REJECT_STATUSES.contains(status);
    }

    public static boolean canWithdraw(String status) {
        return WITHDRAW_STATUSES.contains(status);
    }

    public static boolean canDelete(String status) {
        return DELETE_STATUSES.contains(status);
    }

    // ==================== 任务状态校验 ====================

    private static final Set<String> TASK_APPROVE = Set.of(TASK_STATUS_PENDING);
    private static final Set<String> TASK_REJECT = Set.of(TASK_STATUS_PENDING);
    private static final Set<String> TASK_RETURN = Set.of(TASK_STATUS_PENDING);
    private static final Set<String> TASK_TRANSFER = Set.of(TASK_STATUS_PENDING);

    public static boolean canApproveTask(String status) {
        return TASK_APPROVE.contains(status);
    }

    public static boolean canRejectTask(String status) {
        return TASK_REJECT.contains(status);
    }

    public static boolean canReturnTask(String status) {
        return TASK_RETURN.contains(status);
    }

    public static boolean canTransferTask(String status) {
        return TASK_TRANSFER.contains(status);
    }

    // ==================== 节点拓扑 ====================

    /**
     * 构建邻接表: nodeId → 出边列表
     */
    public static Map<Integer, List<WfDefEdge>> buildAdjacency(List<WfDefNode> nodes, List<WfDefEdge> edges) {
        Map<Integer, List<WfDefEdge>> adj = new LinkedHashMap<>();
        for (WfDefNode node : nodes) {
            adj.put(node.getWfDefNodeId(), new ArrayList<>());
        }
        for (WfDefEdge edge : edges) {
            adj.computeIfAbsent(edge.getSourceNodeId(), k -> new ArrayList<>()).add(edge);
        }
        return adj;
    }

    /**
     * 查找起始节点
     */
    public static WfDefNode findStartNode(List<WfDefNode> nodes) {
        return nodes.stream()
                .filter(n -> NODE_TYPE_START.equals(n.getNodeType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("流程定义缺少 START 节点"));
    }

    /**
     * 查找结束节点
     */
    public static WfDefNode findEndNode(List<WfDefNode> nodes) {
        return nodes.stream()
                .filter(n -> NODE_TYPE_END.equals(n.getNodeType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("流程定义缺少 END 节点"));
    }

    /**
     * 获取节点的后续节点列表
     */
    public static List<WfDefNode> getNextNodes(Integer currentNodeId, List<WfDefNode> allNodes,
                                               Map<Integer, List<WfDefEdge>> adjacency) {
        List<WfDefEdge> outgoingEdges = adjacency.getOrDefault(currentNodeId, Collections.emptyList());
        if (outgoingEdges.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> targetIds = outgoingEdges.stream()
                .map(WfDefEdge::getTargetNodeId)
                .collect(Collectors.toSet());
        return allNodes.stream()
                .filter(n -> targetIds.contains(n.getWfDefNodeId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取首个非条件后续节点(跳过 CONDITION 节点)
     */
    public static List<WfDefNode> getNextNonConditionNodes(Integer currentNodeId, List<WfDefNode> allNodes,
                                                           Map<Integer, List<WfDefEdge>> adjacency) {
        List<WfDefNode> nextNodes = getNextNodes(currentNodeId, allNodes, adjacency);
        List<WfDefNode> result = new ArrayList<>();
        for (WfDefNode node : nextNodes) {
            if (NODE_TYPE_CONDITION.equals(node.getNodeType())) {
                result.addAll(getNextNonConditionNodes(node.getWfDefNodeId(), allNodes, adjacency));
            } else {
                result.add(node);
            }
        }
        return result;
    }

    // ==================== 环路检测 ====================

    /**
     * DFS 环路检测
     */
    public static boolean hasCycle(List<WfDefNode> nodes, List<WfDefEdge> edges) {
        Map<Integer, List<WfDefEdge>> adj = buildAdjacency(nodes, edges);
        Set<Integer> white = new HashSet<>();
        Set<Integer> gray = new HashSet<>();
        Set<Integer> black = new HashSet<>();
        for (WfDefNode node : nodes) {
            white.add(node.getWfDefNodeId());
        }
        for (WfDefNode node : nodes) {
            if (white.contains(node.getWfDefNodeId())) {
                if (dfsCycle(node.getWfDefNodeId(), adj, white, gray, black)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean dfsCycle(Integer nodeId, Map<Integer, List<WfDefEdge>> adj,
                                    Set<Integer> white, Set<Integer> gray, Set<Integer> black) {
        move(nodeId, white, gray);
        for (WfDefEdge edge : adj.getOrDefault(nodeId, Collections.emptyList())) {
            Integer target = edge.getTargetNodeId();
            if (black.contains(target)) continue;
            if (gray.contains(target)) return true;
            if (dfsCycle(target, adj, white, gray, black)) return true;
        }
        move(nodeId, gray, black);
        return false;
    }

    private static void move(Integer nodeId, Set<Integer> from, Set<Integer> top) {
        from.remove(nodeId);
        top.add(nodeId);
    }

    // ==================== 审批节点排序 ====================

    /**
     * 获取审批节点列表（按拓扑 BFS 顺序）
     */
    public static List<WfDefNode> getApprovalNodesInOrder(List<WfDefNode> nodes, List<WfDefEdge> edges) {
        WfDefNode start = findStartNode(nodes);
        Map<Integer, List<WfDefEdge>> adj = buildAdjacency(nodes, edges);
        List<WfDefNode> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        dfsCollectApproval(start.getWfDefNodeId(), adj, nodes, visited, result);
        return result;
    }

    private static void dfsCollectApproval(Integer nodeId, Map<Integer, List<WfDefEdge>> adj,
                                           List<WfDefNode> allNodes, Set<Integer> visited,
                                           List<WfDefNode> collector) {
        if (!visited.add(nodeId)) return;
        allNodes.stream()
                .filter(n -> n.getWfDefNodeId().equals(nodeId) && NODE_TYPE_APPROVAL.equals(n.getNodeType()))
                .findFirst()
                .ifPresent(collector::add);
        for (WfDefEdge edge : adj.getOrDefault(nodeId, Collections.emptyList())) {
            dfsCollectApproval(edge.getTargetNodeId(), adj, allNodes, visited, collector);
        }
    }
}
