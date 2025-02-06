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
import com.chua.starter.service.SysProjectService;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.project.ProjectDefaultFunctionV1Request;
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
 * 项目接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "项目接口")
@RestController
@RequestMapping("/v2/project")
@RequiredArgsConstructor
public class SysProjectController {

    private final SysProjectService sysProjectService;

    /**
     * 项目列表
     *
     * @return 项目列表
     */
    @Operation(summary = "角色项目列表")
    @GetMapping("/page")
    public ReturnPageResult<SysProject> page(@ParameterObject Query<SysProject> query, @ParameterObject SysProject sysProject, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysProjectService.pageFor(query, sysProject, roles));
    }


    /**
     * 获取默认项目
     *
     * @return 项目列表
     */
    @Operation(summary = "获取默认项目")
    @GetMapping("/default")
    @Permission(value = "sys:project:default", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysProject> getDefaultByType(String typeName) {
        return ReturnResult.ok(sysProjectService.getDefaultByType(typeName));
    }

    /**
     * 删除项目
     *
     * @return 更新项目
     */
    @Operation(summary = "删除项目")
    @DeleteMapping("/delete")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:secret:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysProjectId) {
        if (null == sysProjectId) {
            return Result.illegal("项目信息不存在");
        }

        return sysProjectService.delete(sysProjectId);
    }


    /**
     * 更新项目
     *
     * @return 更新项目
     */
    @Operation(summary = "更新项目")
    @PutMapping("/update")
    @Permission(value = "sys:project:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysProject sysProject,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysProjectService.update(roles, sysProject);
    }
    /**
     * 更新项目默认
     *
     * @return 更新项目
     */
    @Operation(summary = "更新项目默认")
    @PutMapping("/updateDefault")
    @Permission(value = "sys:project:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysProject> updateDefault(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody ProjectDefaultFunctionV1Request request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysProjectService.updateDefault(request);
    }

    /**
     * 新增项目
     *
     * @return 新增项目
     */
    @Operation(summary = "新增项目")
    @PostMapping("/save")
    @Permission(value = "sys:project:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysProject> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysProject sysProject,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysProjectService.save(sysProject, roles);
    }
}
