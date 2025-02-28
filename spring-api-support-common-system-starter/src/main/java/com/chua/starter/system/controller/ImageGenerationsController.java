package com.chua.starter.system.controller;

import com.chua.common.support.bigmodel.generations.image.ImageGenerationsSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysAiVincentTask;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1SearchTaskRequest;
import com.chua.starter.system.service.VincentDiagramService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文生图接口
 *
 * @author CH
 * @since 2022/7/29 16:01
 */
@RestController
@Slf4j
@Api(tags = "文生图接口")
@Tag(name = "文生图接口")
@RequestMapping("/v2/ai/vincent")
@RequiredArgsConstructor
public class ImageGenerationsController {

    final VincentDiagramService vincentDiagramService;

    /**
     * 提交任务。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "提交任务")
    @PostMapping("task")
    public ReturnResult<VincentDiagramV1AddTaskResponse> task(@Validated(SelectGroup.class) @RequestBody @ParameterObject VincentDiagramV1AddTaskRequest request,
                                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return vincentDiagramService.addTask(request);
    }

    /**
     * 查询任务。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询任务")
    @GetMapping("task")
    public ReturnResult<ImageGenerationsSearchTaskV1Response> task(@Validated(SelectGroup.class) @ParameterObject VincentDiagramV1SearchTaskRequest request,
                                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return vincentDiagramService.searchTask(request);
    }


    /**
     * 历史任务
     *
     * @return {@link ReturnResult}<{@link List}<{@link SysAiModule}>>
     */
    @Operation(summary = "历史任务")
    @GetMapping("history")
    public ReturnResult<List<SysAiVincentTask>> history(@Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return ReturnResult.ok(vincentDiagramService.history(userId));
    }
}
