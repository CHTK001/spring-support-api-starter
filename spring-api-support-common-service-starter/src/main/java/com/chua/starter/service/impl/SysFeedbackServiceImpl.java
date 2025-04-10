package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.service.SysFeedbackService;
import com.chua.starter.service.mapper.SysFeedbackMapper;
import com.chua.starter.system.entity.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysFeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements SysFeedbackService {

    @Override
    public ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request) {
        return ReturnPageResultUtils.ok(baseMapper.pageFor(feedbackV1Request.createPage(), feedbackV1Request));
    }

    @Override
    public ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume) {
        SysFeedback sysFeedback = this.getById(request.getSysFeedbackId());
        if (null == sysFeedback) {
            return ReturnResult.illegal("反馈信息不存在");
        }
        sysFeedback.setSysFeedbackStatus(1);
        sysFeedback.setSysFeedbackDealBy(Integer.valueOf(userResume.getUserId()));
        sysFeedback.setSysFeedbackDealName(userResume.getName());
        sysFeedback.setSysFeedbackRecoverTime(LocalDateTime.now());
        sysFeedback.setSysFeedbackRecoverContent(request.getSysFeedbackRecoverContent());
        sysFeedback.setSysFeedbackRecoverImages(request.getSysFeedbackRecoverImages());
        return ReturnResult.ok(baseMapper.updateById(sysFeedback) > 0);
    }
}
