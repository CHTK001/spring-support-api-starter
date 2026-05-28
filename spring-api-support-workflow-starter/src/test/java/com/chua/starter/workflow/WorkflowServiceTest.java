package com.chua.starter.workflow;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.workflow.dto.WfDefVO;
import com.chua.starter.workflow.dto.WfInstCreateRequest;
import com.chua.starter.workflow.dto.WfTaskProcessRequest;
import com.chua.starter.workflow.entity.WfDef;
import com.chua.starter.workflow.entity.WfDefEdge;
import com.chua.starter.workflow.entity.WfDefNode;
import com.chua.starter.workflow.entity.WfInst;
import com.chua.starter.workflow.service.WfDefService;
import com.chua.starter.workflow.service.WfInstService;
import com.chua.starter.workflow.service.WfTaskService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流服务集成测试 — 全流程 DRAFT → PENDING → APPROVED
 */
@SpringBootTest(classes = WorkflowTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowServiceTest {

    @Autowired
    private WfDefService wfDefService;

    @Autowired
    private WfInstService wfInstService;

    @Autowired
    private WfTaskService wfTaskService;

    private static Integer savedDefId;
    private static Integer savedInstId;
    private static Integer savedTaskId;

    @Test
    @Order(1)
    void testSaveWorkflowDefinition() {
        WfDefVO vo = new WfDefVO();
        vo.setWfDefName("测试审批流程");
        vo.setWfDefCode("TEST_FLOW_001");
        vo.setWfDefCategory("测试");

        // 节点(使用临时ID，saveFull 会自动重映射)
        WfDefNode start = new WfDefNode();
        start.setWfDefNodeId(-1);
        start.setNodeName("开始");
        start.setNodeType("START");

        WfDefNode approval = new WfDefNode();
        approval.setWfDefNodeId(-2);
        approval.setNodeName("经理审批");
        approval.setNodeType("APPROVAL");
        approval.setAssigneeType("USER");
        approval.setAssigneeValue("1:系统管理员");

        WfDefNode end = new WfDefNode();
        end.setWfDefNodeId(-3);
        end.setNodeName("结束");
        end.setNodeType("END");

        vo.setNodes(List.of(start, approval, end));

        // 边(引用节点临时ID)
        vo.setEdges(List.of(
            edge(-1, -2),
            edge(-2, -3)
        ));

        ReturnResult<Boolean> result = wfDefService.saveFull(vo);
        assertTrue(result.isSuccess());

        // 提取保存后的 defId
        WfDef def = wfDefService.lambdaQuery()
                .eq(WfDef::getWfDefCode, "TEST_FLOW_001")
                .one();
        assertNotNull(def);
        savedDefId = def.getWfDefId();
    }

    @Test
    @Order(2)
    void testPublishWorkflow() {
        assertNotNull(savedDefId);
        ReturnResult<Boolean> result = wfDefService.publish(savedDefId);
        assertTrue(result.isSuccess());

        WfDef def = wfDefService.getById(savedDefId);
        assertEquals(1, def.getWfDefStatus().intValue());
    }

    @Test
    @Order(3)
    void testCreateInstance() {
        assertNotNull(savedDefId);

        WfInstCreateRequest request = new WfInstCreateRequest();
        request.setWfDefId(savedDefId);
        request.setInstTitle("测试请假单-20260528");
        request.setBusinessType("LEAVE");
        request.setBusinessId("LEAVE-001");
        request.setFormData(Map.of("days", 3, "reason", "事假"));
        request.setIdempotentKey("LEAVE-001:submit");

        ReturnResult<WfInst> result = wfInstService.create(request, 1, "系统管理员", null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("DRAFT", result.getData().getInstStatus());

        savedInstId = result.getData().getWfInstId();
    }

    @Test
    @Order(4)
    void testCreateInstance_idempotent() {
        assertNotNull(savedDefId);

        WfInstCreateRequest request = new WfInstCreateRequest();
        request.setWfDefId(savedDefId);
        request.setInstTitle("测试请假单-20260528-重复");
        request.setIdempotentKey("LEAVE-001:submit");

        // 相同幂等键应返回已有实例
        ReturnResult<WfInst> result = wfInstService.create(request, 1, "系统管理员", null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(savedInstId, result.getData().getWfInstId());
    }

    @Test
    @Order(5)
    void testSubmitInstance() {
        assertNotNull(savedInstId);

        ReturnResult<Boolean> result = wfInstService.submit(savedInstId, 1);
        assertTrue(result.isSuccess());

        WfInst inst = wfInstService.getById(savedInstId);
        assertEquals("PENDING", inst.getInstStatus());
    }

    @Test
    @Order(6)
    void testApproveTask() {
        assertNotNull(savedInstId);

        // 查询待办任务
        var detail = wfInstService.getDetail(savedInstId, 1);
        assertNotNull(detail);
        assertNotNull(detail.getCurrentTasks());
        assertFalse(detail.getCurrentTasks().isEmpty());

        // 获取第一个待办任务ID
        savedTaskId = detail.getCurrentTasks().get(0).getWfTaskId();

        // 审批通过
        WfTaskProcessRequest approveReq = new WfTaskProcessRequest();
        approveReq.setWfTaskId(savedTaskId);
        approveReq.setComment("同意");

        ReturnResult<Boolean> result = wfTaskService.approve(approveReq, 1, "系统管理员");
        assertTrue(result.isSuccess());
    }

    @Test
    @Order(7)
    void testInstanceCompletedAfterLastApproval() {
        assertNotNull(savedInstId);

        // 该流程只有一级审批，通过后应自动完成
        WfInst inst = wfInstService.getById(savedInstId);
        assertEquals("APPROVED", inst.getInstStatus());
        assertNotNull(inst.getFinishTime());
    }

    @Test
    @Order(8)
    void testTaskHistoryCreated() {
        assertNotNull(savedInstId);

        var detail = wfInstService.getDetail(savedInstId, 1);
        assertNotNull(detail.getHistories());
        assertFalse(detail.getHistories().isEmpty());

        // 应有 SUBMIT 和 APPROVE 两条历史
        long submitCount = detail.getHistories().stream()
                .filter(h -> "SUBMIT".equals(h.getOperation()))
                .count();
        long approveCount = detail.getHistories().stream()
                .filter(h -> "APPROVE".equals(h.getOperation()))
                .count();
        assertTrue(submitCount >= 1);
        assertTrue(approveCount >= 1);
    }

    @Test
    @Order(9)
    void testCreateAndReject() {
        // 创建新流程并测试驳回
        WfInstCreateRequest request = new WfInstCreateRequest();
        request.setWfDefId(savedDefId);
        request.setInstTitle("驳回测试");
        request.setIdempotentKey("REJECT-TEST-001");

        ReturnResult<WfInst> createResult = wfInstService.create(request, 2, "测试用户", null);
        assertTrue(createResult.isSuccess());
        Integer rejectInstId = createResult.getData().getWfInstId();

        // 提交
        wfInstService.submit(rejectInstId, 2);

        // 获取任务并驳回
        var rejectDetail = wfInstService.getDetail(rejectInstId, 2);
        Integer rejectTaskId = rejectDetail.getCurrentTasks().get(0).getWfTaskId();

        WfTaskProcessRequest rejectReq = new WfTaskProcessRequest();
        rejectReq.setWfTaskId(rejectTaskId);
        rejectReq.setComment("不同意");
        ReturnResult<Boolean> rejectResult = wfTaskService.reject(rejectReq, 1, "系统管理员");
        assertTrue(rejectResult.isSuccess());

        WfInst rejected = wfInstService.getById(rejectInstId);
        assertEquals("REJECTED", rejected.getInstStatus());
    }

    @Test
    @Order(10)
    void testCreateAndWithdraw() {
        WfInstCreateRequest request = new WfInstCreateRequest();
        request.setWfDefId(savedDefId);
        request.setInstTitle("撤销测试");
        request.setIdempotentKey("WITHDRAW-TEST-001");

        ReturnResult<WfInst> createResult = wfInstService.create(request, 3, "用户三", null);
        assertTrue(createResult.isSuccess());
        Integer withdrawInstId = createResult.getData().getWfInstId();

        // 提交
        wfInstService.submit(withdrawInstId, 3);

        // 撤销
        ReturnResult<Boolean> withdrawResult = wfInstService.withdraw(withdrawInstId, 3);
        assertTrue(withdrawResult.isSuccess());

        WfInst withdrawn = wfInstService.getById(withdrawInstId);
        assertEquals("WITHDRAWN", withdrawn.getInstStatus());
    }

    // ======================== 工具 ========================

    private static WfDefEdge edge(Integer sourceId, Integer targetId) {
        WfDefEdge e = new WfDefEdge();
        e.setSourceNodeId(sourceId);
        e.setTargetNodeId(targetId);
        return e;
    }
}
