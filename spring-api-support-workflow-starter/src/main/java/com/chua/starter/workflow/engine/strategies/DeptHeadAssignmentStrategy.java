package com.chua.starter.workflow.engine.strategies;

import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.entity.WfInst;

import java.util.Collections;
import java.util.List;

/**
 * 发起人所在部门负责人审批
 *
 * @author CH
 * @since 2026/5/28
 */
public class DeptHeadAssignmentStrategy implements AssignmentStrategy {

    @Override
    public String type() {
        return "DEPT_HEAD";
    }

    @Override
    public List<AssigneeUser> assign(String assigneeValue, WfInst inst) {
        if (inst == null || inst.getDeptId() == null) {
            return Collections.emptyList();
        }
        // assigneeValue 可选: 表示向上级第几层的负责人 (默认1)
        // 实际应通过 SysDeptPrincipalRel 查询部门负责人
        return Collections.singletonList(new AssigneeUser(0, "DEPT_HEAD:" + inst.getDeptId()));
    }
}
