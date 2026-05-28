package com.chua.starter.workflow.engine.strategies;

import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.entity.WfInst;

import java.util.Collections;
import java.util.List;

/**
 * 指定角色审批(该角色下所有用户)
 *
 * @author CH
 * @since 2026/5/28
 */
public class RoleAssignmentStrategy implements AssignmentStrategy {

    @Override
    public String type() {
        return "ROLE";
    }

    @Override
    public List<AssigneeUser> assign(String assigneeValue, WfInst inst) {
        // assigneeValue 格式: "roleId" 或 "roleCode"
        if (assigneeValue == null || assigneeValue.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Integer roleId = Integer.parseInt(assigneeValue.trim());
            return Collections.singletonList(new AssigneeUser(0, "ROLE:" + roleId));
        } catch (NumberFormatException e) {
            // 按 roleCode 处理
            return Collections.singletonList(new AssigneeUser(0, "ROLE_CODE:" + assigneeValue));
        }
    }
}
