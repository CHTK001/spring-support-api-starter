package com.chua.starter.workflow.engine.strategies;

import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.entity.WfInst;

import java.util.Collections;
import java.util.List;

/**
 * 指定部门审批(部门下所有用户)
 * 注: 实际场景中应查询 SysUser 表获取部门下用户
 *
 * @author CH
 * @since 2026/5/28
 */
public class DeptAssignmentStrategy implements AssignmentStrategy {

    @Override
    public String type() {
        return "DEPT";
    }

    @Override
    public List<AssigneeUser> assign(String assigneeValue, WfInst inst) {
        // assigneeValue 格式: "deptId" 或 JSON
        if (assigneeValue == null || assigneeValue.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Integer deptId = Integer.parseInt(assigneeValue.trim());
            // 实际应通过 SysUserService 查询 deptId 下所有用户
            // 此处返回占位, 由 WfDeptService 在实际分配时注入
            return Collections.singletonList(new AssigneeUser(0, "DEPT:" + deptId));
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }
}
