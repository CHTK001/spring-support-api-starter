package com.chua.starter.system.controller;

import com.chua.common.support.bigmodel.BigModelResponse;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.pojo.bigmodel.BigModelRequest;
import com.chua.starter.system.service.BigModelService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 大语言模型接口
 * @author CH
 * @since 2024/9/7
 */
@RestController
@Slf4j
@Api(tags = "大语言模型接口")
@Tag(name = "大语言模型接口")
@RequestMapping("/v2/api/language")
@RequiredArgsConstructor
public class BigModelController {

    final BigModelService bigModelService;


    /**
     * 查询基本信息。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "大语言模型")
    @GetMapping("call")
    public ReturnResult<BigModelResponse> call(@ParameterObject Query<?> query, @Validated(SelectGroup.class) @ParameterObject BigModelRequest request,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return bigModelService.call(query, request);
    }
    
    
    /**
     * 查询基本信息。
     *
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "大语言模型")
    @GetMapping("models")
    public ReturnResult<String[]> models() {
        return bigModelService.models();
    }
    /**
     * 查询基本信息。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "大语言模型")
    @PostMapping(value = "stream", produces = "text/event-stream")
    public SseEmitter callStream(@ParameterObject Query<?> query,
                                 @Validated(SelectGroup.class) @RequestBody @ParameterObject BigModelRequest request,
                                 HttpServletResponse response,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return bigModelService.callStream(query, request, response);
    }

}
