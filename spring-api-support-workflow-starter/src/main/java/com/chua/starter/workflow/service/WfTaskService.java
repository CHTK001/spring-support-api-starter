package com.chua.starter.workflow.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.workflow.dto.WfTaskProcessRequest;

/**
 * 流程任务服务
 *
 * @author CH
 * @since 2026/5/28
 */
public interface WfTaskService {

    /**
     * 审批通过
     */
    ReturnResult<Boolean> approve(WfTaskProcessRequest request, Integer userId, String userName);

    /**
     * 审批驳回
     */
    ReturnResult<Boolean> reject(WfTaskProcessRequest request, Integer userId, String userName);

    /**
     * 退回
     */
    ReturnResult<Boolean> returnTask(WfTaskProcessRequest request, Integer userId, String userName);

    /**
     * 转交
     */
    ReturnResult<Boolean> transfer(WfTaskProcessRequest request, Integer userId, String userName);
}
