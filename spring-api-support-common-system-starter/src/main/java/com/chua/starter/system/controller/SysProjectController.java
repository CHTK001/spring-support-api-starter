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
 * 密钥接口
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
     * 密钥列表
     *
     * @return 密钥列表
     */
    @Operation(summary = "角色密钥列表")
    @GetMapping("/page")
    public ReturnPageResult<SysProject> page(@ParameterObject Query<SysProject> query, @ParameterObject SysProject sysProject, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysProjectService.pageFor(query, sysProject, roles));
    }

    /**
     * 删除密钥
     *
     * @return 更新密钥
     */
    @Operation(summary = "删除密钥")
    @DeleteMapping("/delete")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:secret:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysProjectId) {
        if (null == sysProjectId) {
            return Result.illegal("密钥信息不存在");
        }

        return sysProjectService.delete(sysProjectId);
    }


    /**
     * 更新密钥
     *
     * @return 更新密钥
     */
    @Operation(summary = "更新密钥")
    @PutMapping("/update")
    @Permission(value = "sys:project:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysProject sysProject,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysProjectService.update(roles, sysProject);
    }

    /**
     * 新增密钥
     *
     * @return 新增密钥
     */
    @Operation(summary = "新增密钥")
    @PostMapping("/save")
    @Permission(value = "sys:project:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysProject> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysProject sysProject,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysProjectService.save(sysProject, roles);
    }
}
