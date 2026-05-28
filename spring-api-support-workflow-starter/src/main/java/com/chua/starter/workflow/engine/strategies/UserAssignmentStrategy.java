package com.chua.starter.workflow.engine.strategies;

import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.entity.WfInst;

import java.util.Collections;
import java.util.List;

/**
 * 指定具体人员审批
 *
 * @author CH
 * @since 2026/5/28
 */
public class UserAssignmentStrategy implements AssignmentStrategy {

    @Override
    public String type() {
        return "USER";
    }

    @Override
    public List<AssigneeUser> assign(String assigneeValue, WfInst inst) {
        // assigneeValue 格式: "userId:userName" 或 JSON [{"userId":1,"userName":"张三"}]
        if (assigneeValue == null || assigneeValue.isBlank()) {
            return Collections.emptyList();
        }
        // 简单格式: "1:张三"
        if (!assigneeValue.startsWith("[")) {
            String[] parts = assigneeValue.split(":", 2);
            if (parts.length >= 1) {
                try {
                    Integer userId = Integer.parseInt(parts[0].trim());
                    String userName = parts.length >= 2 ? parts[1].trim() : "";
                    return Collections.singletonList(new AssigneeUser(userId, userName));
                } catch (NumberFormatException e) {
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }
}
