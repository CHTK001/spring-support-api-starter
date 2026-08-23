package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.system.model.feedback.FeedbackStatisticV1Response;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;

/**
 * 反馈服务
 *
 * @author Administrator
 * @author CH
 */
public interface SysFeedbackService extends IService<SysFeedback> {

    /**
     * 分页查询反馈列表
     *
     * @param feedbackV1Request 反馈分页查询请求
     * @return 反馈分页结果
     */
    ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request);

    /**
     * 处理反馈
     *
     * @param request    反馈处理请求
     * @param userResume 当前处理人信息
     * @return 处理结果
     */
    ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume);

    /**
     * 获取反馈统计信息
     *
     * @return 反馈统计结果
     */
    ReturnResult<FeedbackStatisticV1Response> statistic();
}
