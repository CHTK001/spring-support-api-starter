package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;

/**
 * 反馈服务
 *
 * @author Administrator
 */
public interface SysFeedbackService extends IService<SysFeedback> {


    /**
     * 分页查询
     *
     * @param feedbackV1Request 请求
     * @return 数据
     */
    ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request);

    /**
     * 处理反馈闂
     *
     * @param request    请求
     * @param userResume
     * @return 数据
     */
    ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume);
}
