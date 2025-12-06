package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.DeleteGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.project.service.SysAiVincentStyleService;
import com.chua.starter.project.entity.SysAiVincentStyle;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文生图风格风格接口
 *
 * @author CH
 * @since 2022/7/29 16:01
 */
@RestController
@Slf4j
@Api(tags = "文生图风格风格接口")
@Tag(name = "文生图风格风格接口")
@RequestMapping("/v2/ai/vincent/style")
@RequiredArgsConstructor
public class VincentDiagramStyleController {

    final SysAiVincentStyleService aiVincentStyleService;


    /**
     * 更新ai模型
     *
     * @param sysAiVincentStyle ai模块
     * @param bindingResult     绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "更新ai模型模板")
    @GetMapping("list")
    public ReturnResult<List<SysAiVincentStyle>> list(@Validated(SelectGroup.class) @ParameterObject SysAiVincentStyle sysAiVincentStyle,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return aiVincentStyleService.listForModuleStyle(sysAiVincentStyle);
    }

    /**
     * 更新ai模型
     *
     * @param sysAiVincentStyle ai模块
     * @param bindingResult     绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "更新ai模型风格")
    @PutMapping("update")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> updateForModule(@Validated(UpdateGroup.class) @ParameterObject @RequestBody SysAiVincentStyle sysAiVincentStyle,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return aiVincentStyleService.updateForModuleStyle(sysAiVincentStyle);
    }

    @Operation(summary = "添加ai模型风格")
    @PostMapping("save")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysAiVincentStyle> saveForModule(@Validated(AddGroup.class) @ParameterObject @RequestBody SysAiVincentStyle sysAiVincentStyle, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return aiVincentStyleService.saveForModuleStyle(sysAiVincentStyle);
    }

    /**
     * 删除ai模型
     *
     * @param sysAiVincentStyleId ai模块ID
     * @param bindingResult       绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "删除ai模型风格")
    @DeleteMapping("delete")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> deleteForModule(@Validated(DeleteGroup.class) Integer sysAiVincentStyleId, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return aiVincentStyleService.deleteForModuleStyle(sysAiVincentStyleId);
    }

}
