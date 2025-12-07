package com.chua.starter.project.controller;

import com.chua.deeplearning.support.ml.bigmodel.resolution.ImageResolutionAddTaskV1Response;
import com.chua.deeplearning.support.ml.bigmodel.resolution.ImageResolutionSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.project.service.ImageResolutionService;
import com.chua.starter.project.pojo.vincent.ImageResolutionAddTaskV2Request;
import com.chua.starter.project.pojo.vincent.ImageResolutionSearchTaskV2Request;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 图片超分辨率接口
 *
 * @author CH
 * @since 2025/03/19 16:01
 */
@RestController
@Slf4j
@Api(tags = "图片超分辨率接口")
@Tag(name = "图片超分辨率接口")
@RequestMapping("/v2/ai/image/resolution")
@RequiredArgsConstructor
public class ImageResolutionController {

    final ImageResolutionService imageResolutionService;

    /**
     * 提交任务。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "提交任务")
    @PostMapping("task")
    public ReturnResult<ImageResolutionAddTaskV1Response> task(@Validated(SelectGroup.class) @RequestBody @ParameterObject ImageResolutionAddTaskV2Request request,
                                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return imageResolutionService.addTask(request);
    }

    /**
     * 查询任务。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询任务")
    @GetMapping("task")
    public ReturnResult<ImageResolutionSearchTaskV1Response> task(@Validated(SelectGroup.class) @ParameterObject ImageResolutionSearchTaskV2Request request,
                                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return imageResolutionService.searchTask(request);
    }


}
