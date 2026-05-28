package com.chua.starter.workflow.engine.strategies;

import com.chua.starter.workflow.engine.AssigneeUser;
import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.entity.WfInst;

import java.util.Collections;
import java.util.List;

/**
 * 发起人自己(如填单节点)
 *
 * @author CH
 * @since 2026/5/28
 */
public class ApplicantAssignmentStrategy implements AssignmentStrategy {

    @Override
    public String type() {
        return "APPLICANT";
    }

    @Override
    public List<AssigneeUser> assign(String assigneeValue, WfInst inst) {
        if (inst == null || inst.getStarterId() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new AssigneeUser(inst.getStarterId(), inst.getCreateName()));
    }
}
