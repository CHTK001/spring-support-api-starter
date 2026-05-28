package com.chua.starter.workflow.engine;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 审批人视图对象（含用户ID和姓名）
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@AllArgsConstructor
public class AssigneeUser {
    private Integer userId;
    private String userName;
}
