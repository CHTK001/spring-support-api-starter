package com.chua.starter.workflow.engine;

import com.chua.starter.workflow.entity.WfInst;

import java.util.List;

/**
 * 审批人分配策略接口
 *
 * @author CH
 * @since 2026/5/28
 */
public interface AssignmentStrategy {

    /**
     * 策略类型标识
     */
    String type();

    /**
     * 根据策略分配审批人
     *
     * @param assigneeValue 策略值(JSON)
     * @param inst          流程实例(含发起人信息)
     * @return 审批人列表
     */
    List<AssigneeUser> assign(String assigneeValue, WfInst inst);
}
