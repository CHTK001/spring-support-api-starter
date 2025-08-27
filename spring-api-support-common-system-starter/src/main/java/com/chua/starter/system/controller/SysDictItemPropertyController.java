package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.service.SysDictItemPropertyService;
import com.chua.starter.system.pojo.dict.DictItemPropertyV1Request;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典子项接口(缓存)
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "字典子项配置项接口")
@RestController
@RequestMapping("/v2/dict/item/property")
@RequiredArgsConstructor
public class SysDictItemPropertyController {

    private final SysDictItemPropertyService sysDictItemPropertyService;

    /**
     * 保存或更新
     *
     * @param request 配置
     * @return 结果
     */
    @PutMapping("/saveOrUpdate")
    @Operation(summary = "保存或更新")
    public ReturnResult<Boolean> saveOrUpdate(@RequestBody @ParameterObject DictItemPropertyV1Request request) {
        return sysDictItemPropertyService.saveOrUpdate(request);
    }
}
