package com.chua.starter.service.controller;

import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.service.SysFeedbackService;
import com.chua.starter.system.dto.feedback.FeedbackStatisticV1Response;
import com.chua.starter.system.entity.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈(缓存)
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "反馈接口")
@RestController
@RequestMapping("/v2/feedback")
@RequiredArgsConstructor
@Ignore
public class SysFeedbackController {

    final SysFeedbackService sysFeedbackService;

    /**
     * 反馈分页列表
     *
     * @param feedbackV1Request 反馈请求
     * @return 反馈分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "反馈分页列表")
    public ReturnPageResult<SysFeedback> page(@ParameterObject FeedbackV1Request feedbackV1Request) {
        return sysFeedbackService.pageFor(feedbackV1Request);
    }


    /**
     * 处理反馈问题
     *
     * @param request 反馈请求
     * @return 反馈结果
     */
    @PutMapping("/issue")
    @Operation(summary = "处理反馈问题")
    public ReturnResult<Boolean> issue(@Validated(UpdateGroup.class) @ParameterObject @RequestBody FeedbackIssueV1Request request,
                                       @UserValue UserResume userResume,
                                       BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysFeedbackService.issue(request, userResume);
    }


    /**
     * 反馈统计
     *
     * @return 反馈统计
     */
    public ReturnResult<FeedbackStatisticV1Response> statistic() {
        return null;
    }
}
