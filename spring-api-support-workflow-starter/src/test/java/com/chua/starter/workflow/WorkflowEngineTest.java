package com.chua.starter.workflow;

import com.chua.starter.workflow.engine.WorkflowEngine;
import com.chua.starter.workflow.entity.WfDefEdge;
import com.chua.starter.workflow.entity.WfDefNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkflowEngine 纯单元测试 — 状态机 + 拓扑 + 环路检测
 */
class WorkflowEngineTest {

    // ======================== 状态机 ========================

    @Test
    void testInstanceStateMachine() {
        // DRAFT 可以提交
        assertTrue(WorkflowEngine.canSubmit("DRAFT"));
        assertFalse(WorkflowEngine.canSubmit("PENDING"));
        assertFalse(WorkflowEngine.canSubmit("APPROVED"));

        // PENDING 可以审批/驳回/撤销
        assertTrue(WorkflowEngine.canApprove("PENDING"));
        assertTrue(WorkflowEngine.canReject("PENDING"));
        assertTrue(WorkflowEngine.canWithdraw("PENDING"));
        assertFalse(WorkflowEngine.canApprove("DRAFT"));
        assertFalse(WorkflowEngine.canApprove("APPROVED"));

        // 可删除状态
        assertTrue(WorkflowEngine.canDelete("DRAFT"));
        assertTrue(WorkflowEngine.canDelete("REJECTED"));
        assertTrue(WorkflowEngine.canDelete("WITHDRAWN"));
        assertFalse(WorkflowEngine.canDelete("PENDING"));
        assertFalse(WorkflowEngine.canDelete("APPROVED"));
    }

    @Test
    void testTaskStateMachine() {
        // PENDING 任务可操作
        assertTrue(WorkflowEngine.canApproveTask("PENDING"));
        assertTrue(WorkflowEngine.canRejectTask("PENDING"));
        assertTrue(WorkflowEngine.canReturnTask("PENDING"));
        assertTrue(WorkflowEngine.canTransferTask("PENDING"));

        // 已审批任务不可重复操作
        assertFalse(WorkflowEngine.canApproveTask("APPROVED"));
        assertFalse(WorkflowEngine.canRejectTask("APPROVED"));
        assertFalse(WorkflowEngine.canReturnTask("REJECTED"));

        // 已取消不可操作
        assertFalse(WorkflowEngine.canApproveTask("CANCELLED"));
        assertFalse(WorkflowEngine.canTransferTask("CANCELLED"));
    }

    // ======================== 查找起始/结束节点 ========================

    @Test
    void testFindStartNode() {
        List<WfDefNode> nodes = nodes(
            node(1, "开始", "START"),
            node(2, "审批", "APPROVAL"),
            node(3, "结束", "END")
        );
        assertEquals(1, WorkflowEngine.findStartNode(nodes).getWfDefNodeId().intValue());
    }

    @Test
    void testFindStartNode_throwsWhenMissing() {
        List<WfDefNode> nodes = nodes(node(1, "审批", "APPROVAL"));
        assertThrows(IllegalArgumentException.class, () -> WorkflowEngine.findStartNode(nodes));
    }

    @Test
    void testFindEndNode() {
        List<WfDefNode> nodes = nodes(
            node(1, "开始", "START"),
            node(2, "审批", "APPROVAL"),
            node(3, "结束", "END")
        );
        assertEquals(3, WorkflowEngine.findEndNode(nodes).getWfDefNodeId().intValue());
    }

    @Test
    void testFindEndNode_throwsWhenMissing() {
        List<WfDefNode> nodes = nodes(node(1, "开始", "START"));
        assertThrows(IllegalArgumentException.class, () -> WorkflowEngine.findEndNode(nodes));
    }

    // ======================== 邻接表 / 后续节点 ========================

    @Test
    void testBuildAdjacency() {
        List<WfDefNode> nodes = nodes(
            node(1, "开始", "START"),
            node(2, "审批", "APPROVAL"),
            node(3, "结束", "END")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2),
            edge(2, 3)
        );
        Map<Integer, List<WfDefEdge>> adj = WorkflowEngine.buildAdjacency(nodes, edges);
        assertEquals(3, adj.size());
        assertEquals(1, adj.get(1).size());
        assertEquals(1, adj.get(2).size());
        assertEquals(0, adj.get(3).size());
    }

    @Test
    void testGetNextNodes() {
        List<WfDefNode> nodes = nodes(
            node(1, "START", "START"),
            node(2, "APPROVAL1", "APPROVAL"),
            node(3, "APPROVAL2", "APPROVAL"),
            node(4, "END", "END")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2),
            edge(2, 3),
            edge(3, 4)
        );
        Map<Integer, List<WfDefEdge>> adj = WorkflowEngine.buildAdjacency(nodes, edges);

        List<WfDefNode> nextFromStart = WorkflowEngine.getNextNodes(1, nodes, adj);
        assertEquals(1, nextFromStart.size());
        assertEquals("APPROVAL1", nextFromStart.get(0).getNodeName());

        List<WfDefNode> nextFromApproval2 = WorkflowEngine.getNextNodes(3, nodes, adj);
        assertEquals(1, nextFromApproval2.size());
        assertEquals("END", nextFromApproval2.get(0).getNodeType());
    }

    @Test
    void testGetNextNonConditionNodes_skipsCondition() {
        List<WfDefNode> nodes = nodes(
            node(1, "START", "START"),
            node(2, "COND", "CONDITION"),
            node(3, "APPROVAL", "APPROVAL"),
            node(4, "END", "END")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2),
            edge(2, 3),
            edge(3, 4)
        );
        Map<Integer, List<WfDefEdge>> adj = WorkflowEngine.buildAdjacency(nodes, edges);

        List<WfDefNode> next = WorkflowEngine.getNextNonConditionNodes(1, nodes, adj);
        assertEquals(1, next.size());
        assertEquals("APPROVAL", next.get(0).getNodeName());
    }

    // ======================== 环路检测 ========================

    @Test
    void testHasCycle_noCycle() {
        List<WfDefNode> nodes = nodes(
            node(1, "START", "START"),
            node(2, "A", "APPROVAL"),
            node(3, "B", "APPROVAL"),
            node(4, "END", "END")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2), edge(2, 3), edge(3, 4)
        );
        assertFalse(WorkflowEngine.hasCycle(nodes, edges));
    }

    @Test
    void testHasCycle_simpleCycle() {
        List<WfDefNode> nodes = nodes(
            node(1, "A", "APPROVAL"),
            node(2, "B", "APPROVAL"),
            node(3, "C", "APPROVAL")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2), edge(2, 3), edge(3, 1)
        );
        assertTrue(WorkflowEngine.hasCycle(nodes, edges));
    }

    @Test
    void testHasCycle_selfLoop() {
        List<WfDefNode> nodes = nodes(
            node(1, "A", "APPROVAL"),
            node(2, "B", "APPROVAL")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2), edge(2, 2)
        );
        assertTrue(WorkflowEngine.hasCycle(nodes, edges));
    }

    @Test
    void testHasCycle_disconnectedGraph_noCycle() {
        List<WfDefNode> nodes = nodes(
            node(1, "A", "START"),
            node(2, "B", "APPROVAL"),
            node(3, "C", "START"),
            node(4, "D", "APPROVAL")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2), edge(3, 4)
        );
        assertFalse(WorkflowEngine.hasCycle(nodes, edges));
    }

    @Test
    void testHasCycle_emptyGraph() {
        assertFalse(WorkflowEngine.hasCycle(new ArrayList<>(), new ArrayList<>()));
    }

    // ======================== 审批节点排序 ========================

    @Test
    void testGetApprovalNodesInOrder() {
        List<WfDefNode> nodes = nodes(
            node(1, "START", "START"),
            node(2, "经理审批", "APPROVAL"),
            node(3, "总监审批", "APPROVAL"),
            node(4, "END", "END")
        );
        List<WfDefEdge> edges = edges(
            edge(1, 2), edge(2, 3), edge(3, 4)
        );
        List<WfDefNode> ordered = WorkflowEngine.getApprovalNodesInOrder(nodes, edges);
        assertEquals(2, ordered.size());
        assertEquals("经理审批", ordered.get(0).getNodeName());
        assertEquals("总监审批", ordered.get(1).getNodeName());
    }

    // ======================== 工具方法 ========================

    private static WfDefNode node(Integer id, String name, String type) {
        WfDefNode n = new WfDefNode();
        n.setWfDefNodeId(id);
        n.setNodeName(name);
        n.setNodeType(type);
        return n;
    }

    private static WfDefEdge edge(Integer source, Integer target) {
        WfDefEdge e = new WfDefEdge();
        e.setSourceNodeId(source);
        e.setTargetNodeId(target);
        return e;
    }

    private static List<WfDefNode> nodes(WfDefNode... ns) {
        return List.of(ns);
    }

    private static List<WfDefEdge> edges(WfDefEdge... es) {
        return List.of(es);
    }
}
