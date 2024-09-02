package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysTemplateGroup;
import com.chua.starter.system.service.SysTemplateGroupService;
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

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 模板分组接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "模板分组接口")
@RestController
@RequestMapping("/v2/template/group")
@RequiredArgsConstructor
public class SysTemplateGroupController {

    private final SysTemplateGroupService sysTemplateGroupService;


    /**
     * 模板分组分页列表
     *
     * @param sysTemplateGroup 条件
     * @return 模板分组分页列表
     */
    @Operation(summary = "模板分组分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysTemplateGroup> pageForRole(@ParameterObject Query<SysTemplateGroup> query, @ParameterObject SysTemplateGroup sysTemplateGroup, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysTemplateGroupService.page(query, sysTemplateGroup, roles));
    }

    /**
     * 删除模板分组
     *
     * @return 更新模板分组
     */
    @Operation(summary = "删除模板分组")
    @DeleteMapping("/delete")
    @OperateLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:template:group:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysTemplateGroupId) {
        if (null == sysTemplateGroupId) {
            return Result.illegal("模板分组信息不存在");
        }

        return sysTemplateGroupService.delete(sysTemplateGroupId);
    }


    /**
     * 更新模板分组
     *
     * @return 更新模板分组
     */
    @Operation(summary = "更新模板分组")
    @PutMapping("/update")
    @OperateLog
    @Permission(value = "sys:template:group:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysTemplateGroup sysTemplateGroup,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateGroupService.update(roles, sysTemplateGroup);
    }

    /**
     * 新增模板分组
     *
     * @return 新增模板分组
     */
    @Operation(summary = "新增模板分组")
    @PostMapping("/save")
    @OperateLog
    @Permission(value = "sys:template:group:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysTemplateGroup> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysTemplateGroup sysTemplateGroup,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateGroupService.save(sysTemplateGroup, roles);
    }
}
