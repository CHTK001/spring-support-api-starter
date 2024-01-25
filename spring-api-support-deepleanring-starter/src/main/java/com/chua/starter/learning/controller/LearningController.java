package com.chua.starter.learning.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.ml.identification.IdentificationEngine;
import com.chua.common.support.spi.Option;
import com.chua.common.support.spi.ServiceProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/25
 */
@Tag(name = "检测接口")
@RestController
@RequestMapping("/v1/learning")
@RequiredArgsConstructor
public class LearningController {

    /**
     * 接收学习请求并进行预测
     *
     * @return 预测结果对象
     */
    @GetMapping("engine")
    @Operation(summary = "引擎列表")
    public ReturnResult<List<Option<String>>> engine() {
        return ReturnResult.of(ServiceProvider.of(IdentificationEngine.class).options());
    }


}
