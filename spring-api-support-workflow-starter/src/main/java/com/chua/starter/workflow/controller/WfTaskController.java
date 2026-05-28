package com.chua.starter.workflow.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.workflow.dto.WfTaskProcessRequest;
import com.chua.starter.workflow.service.WfTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 流程任务 Controller
 *
 * @author CH
 * @since 2026/5/28
 */
@Tag(name = "流程任务")
@RestController
@RequestMapping("/v2/workflow/task")
@RequiredArgsConstructor
public class WfTaskController {

    private final WfTaskService wfTaskService;

    @Operation(summary = "审批通过")
    @PostMapping("/approve")
    public ReturnResult<Boolean> approve(@Valid @RequestBody WfTaskProcessRequest request,
                                         @Parameter(hidden = true) @UserValue("userId") Integer userId,
                                         @Parameter(hidden = true) @UserValue("userName") String userName) {
        return wfTaskService.approve(request, userId, userName);
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/reject")
    public ReturnResult<Boolean> reject(@Valid @RequestBody WfTaskProcessRequest request,
                                        @Parameter(hidden = true) @UserValue("userId") Integer userId,
                                        @Parameter(hidden = true) @UserValue("userName") String userName) {
        return wfTaskService.reject(request, userId, userName);
    }

    @Operation(summary = "退回")
    @PostMapping("/return")
    public ReturnResult<Boolean> returnTask(@Valid @RequestBody WfTaskProcessRequest request,
                                            @Parameter(hidden = true) @UserValue("userId") Integer userId,
                                            @Parameter(hidden = true) @UserValue("userName") String userName) {
        return wfTaskService.returnTask(request, userId, userName);
    }

    @Operation(summary = "转交")
    @PostMapping("/transfer")
    public ReturnResult<Boolean> transfer(@Valid @RequestBody WfTaskProcessRequest request,
                                          @Parameter(hidden = true) @UserValue("userId") Integer userId,
                                          @Parameter(hidden = true) @UserValue("userName") String userName) {
        return wfTaskService.transfer(request, userId, userName);
    }
}
