package com.chua.starter.system.controller;

import com.chua.common.support.bigmodel.colorization.ImageColorizationAddTaskV1Response;
import com.chua.common.support.bigmodel.colorization.ImageColorizationSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.system.pojo.vincent.ImageColorizationAddTaskV2Request;
import com.chua.starter.system.pojo.vincent.ImageColorizationSearchTaskV2Request;
import com.chua.starter.system.service.ImageColorizationService;
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
 * 图片上色接口
 *
 * @author CH
 * @since 2025/03/19 16:01
 */
@RestController
@Slf4j
@Api(tags = "图片上色接口")
@Tag(name = "图片上色接口")
@RequestMapping("/v2/ai/image/colorization")
@RequiredArgsConstructor
public class ImageColorizationController {

    final ImageColorizationService imageColorizationService;

    /**
     * 提交任务。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "提交任务")
    @PostMapping("task")
    public ReturnResult<ImageColorizationAddTaskV1Response> task(@Validated(SelectGroup.class) @RequestBody @ParameterObject ImageColorizationAddTaskV2Request request,
                                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return imageColorizationService.addTask(request);
    }

    /**
     * 查询任务。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询任务")
    @GetMapping("task")
    public ReturnResult<ImageColorizationSearchTaskV1Response> task(@Validated(SelectGroup.class) @ParameterObject ImageColorizationSearchTaskV2Request request,
                                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return imageColorizationService.searchTask(request);
    }


}
