package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysDict;
import com.chua.starter.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Set;

/**
 * 字典接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "字典接口")
@RestController
@RequestMapping("/v2/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;


    /**
     * 字典分页列表
     *
     * @param sysDict 条件
     * @return 字典分页列表
     */
    @Operation(summary = "字典分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysDict> pageForRole(@ParameterObject Query<SysDict> query, @ParameterObject SysDict sysDict, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysDictService.pageForDict(query, sysDict, roles));
    }

    /**
     * 删除字典
     *
     * @return 更新字典
     */
    @Operation(summary = "删除字典")
    @DeleteMapping("/delete")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:dict:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysDictId) {
        if (null == sysDictId) {
            return Result.illegal("字典信息不存在");
        }

        return sysDictService.delete(sysDictId);
    }


    /**
     * 更新字典
     *
     * @return 更新字典
     */
    @Operation(summary = "更新字典")
    @PutMapping("/update")
    @Permission(value = "sys:dict:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDict sysDict,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysDictService.update(roles, sysDict);
    }

    /**
     * 新增字典
     *
     * @return 新增字典
     */
    @Operation(summary = "新增字典")
    @PostMapping("/save")
    @Permission(value = "sys:dict:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysDict> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysDict sysDict,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysDictService.save(sysDict, roles);
    }
}
