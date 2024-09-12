package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.service.SysTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * 模板接口(缓存)
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "模板接口")
@RestController
@RequestMapping("/v2/template")
@RequiredArgsConstructor
public class SysTemplateController {

    private final SysTemplateService sysTemplateService;


    /**
     * 模板分组列表
     *
     * @return 模板分组列表
     */
    @Operation(summary = "模板分组列表")
    @GetMapping("/list")
    public ReturnResult<List<SysTemplate>> list(@ParameterObject Query<SysDictItem> query, Integer sysDictItem2, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictItem2) {
            return ReturnResult.illegal("模板分组不能为空");
        }
        return ReturnResult.ok(sysTemplateService.listForItem(query, sysDictItem2, roles));
    }

    /**
     * 模板分页列表
     *
     * @param sysTemplate 条件
     * @return 模板分页列表
     */
    @Operation(summary = "模板分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysTemplate> pageForRole(@ParameterObject Query<SysTemplate> query,
                                                     @Validated(SelectGroup.class) @ParameterObject SysTemplate sysTemplate,
                                                     @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                     BindingResult bindingResult
                                                     ) {
        if (bindingResult.hasErrors()) {
            return ReturnPageResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return PageResultUtils.ok(sysTemplateService.page(query, sysTemplate, roles));
    }

    /**
     * 删除模板
     *
     * @return 更新模板
     */
    @Operation(summary = "删除模板")
    @DeleteMapping("/delete")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:template:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysTemplateId) {
        if (null == sysTemplateId) {
            return Result.illegal("模板信息不存在");
        }

        return sysTemplateService.delete(sysTemplateId);
    }


    /**
     * 还原模板
     *
     * @return 还原模板
     */
    @Operation(summary = "还原模板")
    @PutMapping("/rollback")
    @Permission(value = "sys:template:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> rollback(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysTemplate sysTemplate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateService.rollback(roles, sysTemplate);
    }

    /**
     * 更新模板
     *
     * @return 更新模板
     */
    @Operation(summary = "更新模板")
    @PutMapping("/update")
    @Permission(value = "sys:template:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysTemplate sysTemplate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateService.update(roles, sysTemplate);
    }

    /**
     * 新增模板
     *
     * @return 新增模板
     */
    @Operation(summary = "新增模板")
    @PostMapping("/save")
    @Permission(value = "sys:template:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysTemplate> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysTemplate sysTemplate,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateService.save(sysTemplate, roles);
    }
}
