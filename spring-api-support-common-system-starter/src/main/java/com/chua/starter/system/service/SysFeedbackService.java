package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;

/**
 * 鍙嶉鏈嶅姟
 *
 * @author Administrator
 */
public interface SysFeedbackService extends IService<SysFeedback> {


    /**
     * 鍒嗛〉鏌ヨ
     *
     * @param feedbackV1Request 璇锋眰
     * @return 鏁版嵁
     */
    ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request);

    /**
     * 澶勭悊鍙嶉闂
     *
     * @param request    璇锋眰
     * @param userResume
     * @return 鏁版嵁
     */
    ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume);
}
