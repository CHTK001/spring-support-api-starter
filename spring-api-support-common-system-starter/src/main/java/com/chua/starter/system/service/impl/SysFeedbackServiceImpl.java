package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.system.mapper.SysFeedbackMapper;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import com.chua.starter.system.service.SysFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class SysFeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements SysFeedbackService {

    @Override
    public ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request) {
        return null;
    }

    @Override
    public ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume) {
        return null;
    }
}
