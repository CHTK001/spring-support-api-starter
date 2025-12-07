package com.chua.starter.system.controller;

import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.dto.feedback.FeedbackStatisticV1Response;
import com.chua.starter.system.entity.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import com.chua.starter.system.service.SysFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 鍙嶉(缂撳瓨)
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "鍙嶉鎺ュ彛")
@RestController
@RequestMapping("/v2/feedback")
@RequiredArgsConstructor
@Ignore
public class SysFeedbackController {

    final SysFeedbackService sysFeedbackService;

    /**
     * 鍙嶉鍒嗛〉鍒楄〃
     *
     * @param feedbackV1Request 鍙嶉璇锋眰
     * @return 鍙嶉鍒嗛〉鍒楄〃
     */
    @GetMapping("/page")
    @Operation(summary = "鍙嶉鍒嗛〉鍒楄〃")
    public ReturnPageResult<SysFeedback> page(@ParameterObject FeedbackV1Request feedbackV1Request) {
        return sysFeedbackService.pageFor(feedbackV1Request);
    }


    /**
     * 澶勭悊鍙嶉闂
     *
     * @param request 鍙嶉璇锋眰
     * @return 鍙嶉缁撴灉
     */
    @PutMapping("/issue")
    @Operation(summary = "澶勭悊鍙嶉闂")
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
     * 鍙嶉缁熻
     *
     * @return 鍙嶉缁熻
     */
    public ReturnResult<FeedbackStatisticV1Response> statistic() {
        return null;
    }
}
