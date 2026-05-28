package com.chua.starter.workflow.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.workflow.dto.WfInstCreateRequest;
import com.chua.starter.workflow.dto.WfInstVO;
import com.chua.starter.workflow.entity.WfInst;
import com.chua.starter.workflow.service.WfInstService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 流程实例 Controller
 *
 * @author CH
 * @since 2026/5/28
 */
@Tag(name = "流程实例")
@RestController
@RequestMapping("/v2/workflow/inst")
@RequiredArgsConstructor
public class WfInstController {

    private final WfInstService wfInstService;

    @Operation(summary = "我的申请列表")
    @GetMapping("/myApply")
    public ReturnPageResult<WfInst> myApply(@ParameterObject Query<WfInst> query, @ParameterObject WfInst inst,
                                             @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return PageResultUtils.ok(wfInstService.pageMyApply(query, inst, userId));
    }

    @Operation(summary = "我的待办列表")
    @GetMapping("/myTodo")
    public ReturnPageResult<WfInst> myTodo(@ParameterObject Query<WfInst> query,
                                            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return PageResultUtils.ok(wfInstService.pageMyTodo(query, userId));
    }

    @Operation(summary = "创建流程实例(草稿)")
    @PostMapping("/create")
    public ReturnResult<WfInst> create(@Valid @RequestBody WfInstCreateRequest request,
                                       @Parameter(hidden = true) @UserValue("userId") Integer userId,
                                       @Parameter(hidden = true) @UserValue("userName") String userName,
                                       @Parameter(hidden = true) @UserValue("deptId") Integer deptId) {
        return wfInstService.create(request, userId, userName, deptId);
    }

    @Operation(summary = "提交审批")
    @PostMapping("/submit")
    public ReturnResult<Boolean> submit(Integer instId,
                                        @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return wfInstService.submit(instId, userId);
    }

    @Operation(summary = "实例详情")
    @GetMapping("/detail")
    public ReturnResult<WfInstVO> detail(Integer instId,
                                         @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        WfInstVO vo = wfInstService.getDetail(instId, userId);
        if (vo == null) {
            return ReturnResult.illegal("流程实例不存在");
        }
        return ReturnResult.ok(vo);
    }

    @Operation(summary = "撤销申请")
    @PostMapping("/withdraw")
    public ReturnResult<Boolean> withdraw(Integer instId,
                                          @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return wfInstService.withdraw(instId, userId);
    }

    @Operation(summary = "删除实例")
    @DeleteMapping("/delete")
    public ReturnResult<Boolean> delete(Integer instId,
                                        @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return wfInstService.deleteInst(instId, userId);
    }
}
