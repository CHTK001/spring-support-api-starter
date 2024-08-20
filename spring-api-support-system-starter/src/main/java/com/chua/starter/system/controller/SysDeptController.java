package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.pojo.dept.SysDeptVO;
import com.chua.starter.system.service.SysDeptService;
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

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 组织机构接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "组织机构接口")
@RestController
@RequestMapping("/v2/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;


    /**
     * 组织机构列表
     *
     * @param sysDept 条件
     * @return 组织机构分页列表
     */
    @Operation(summary = "组织机构列表")
    @GetMapping("/list")
    public ReturnResult<List<SysDeptVO>> list(@ParameterObject SysDept sysDept, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysDeptService.list(sysDept, roles));
    }

    /**
     * 删除组织机构
     *
     * @return 更新组织机构
     */
    @Operation(summary = "删除组织机构")
    @DeleteMapping("/delete")
    @OperateLog
    @SysLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:dept:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long deptId) {
        if (null == deptId) {
            return Result.illegal("组织机构信息不存在");
        }

        return sysDeptService.delete(deptId);
    }


    /**
     * 更新组织机构
     *
     * @return 更新组织机构
     */
    @Operation(summary = "更新组织机构")
    @PutMapping("/update")
    @OperateLog
    @Permission(value = "sys:dept:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDept SysDept,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysDeptService.update(roles, SysDept);
    }

    /**
     * 新增组织机构
     *
     * @return 新增组织机构
     */
    @Operation(summary = "新增组织机构")
    @PostMapping("/save")
    @OperateLog
    @Permission(value = "sys:dept:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysDept> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysDept SysDept,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysDeptService.save(SysDept, roles);
    }
}
