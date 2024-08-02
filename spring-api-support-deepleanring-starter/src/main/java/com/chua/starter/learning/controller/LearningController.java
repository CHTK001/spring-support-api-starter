package com.chua.starter.learning.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.ml.feature.DetectionConfiguration;
import com.chua.common.support.ml.identification.IdentificationEngine;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.SpiOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
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
    public ReturnResult<List<SpiOption>> engine() {
        return ReturnResult.ok(ServiceProvider.of(IdentificationEngine.class).options());
    }
    /**
     * 获取指定引擎的列表
     *
     * @param engine 引擎名称
     * @return 指定引擎的支持选项列表
     */
    @GetMapping("module")
    @Operation(summary = "引擎列表")
    public ReturnResult<List<SpiOption>> engine(String engine) {
        IdentificationEngine identificationEngine = ServiceProvider.of(IdentificationEngine.class).getNewExtension(engine, DetectionConfiguration.DEFAULT);
        if(null == identificationEngine) {
            return ReturnResult.ok(Collections.emptyList());
        }

        return ReturnResult.ok( identificationEngine.support());
    }
    /**
     * 根据引擎类型和模块类型获取实现类型选项
     *
     * @param engine 引擎类型
     * @param moduleType 模块类型
     * @return 实现类型选项列表
     */
    @GetMapping("implType")
    @Operation(summary = "实现类型")
    public ReturnResult<List<SpiOption>> implType(String engine, String moduleType) {
        // 获取指定引擎类型的识别引擎
        IdentificationEngine identificationEngine = ServiceProvider.of(IdentificationEngine.class).getNewExtension(engine, DetectionConfiguration.DEFAULT);
        if (null == identificationEngine) {
            // 如果识别引擎为空，则返回空列表
            return ReturnResult.ok(Collections.emptyList());
        }

        // 返回支持的实现类型选项列表
        return ReturnResult.ok(identificationEngine.implType(moduleType));
    }


}
