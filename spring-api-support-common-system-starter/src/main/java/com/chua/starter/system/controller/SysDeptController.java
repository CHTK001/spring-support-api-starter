package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.pojo.dept.SysDeptVO;
import com.chua.starter.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 缁勭粐鏈烘瀯鎺ュ彛
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "缁勭粐鏈烘瀯鎺ュ彛")
@RestController
@RequestMapping("/v2/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;


    /**
     * 缁勭粐鏈烘瀯鍒楄〃
     *
     * @param sysDept 鏉′欢
     * @return 缁勭粐鏈烘瀯鍒嗛〉鍒楄〃
     */
    @Operation(summary = "缁勭粐鏈烘瀯鍒楄〃")
    @GetMapping("/list")
    public ReturnResult<List<SysDeptVO>> list(@ParameterObject SysDept sysDept, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysDeptService.list(sysDept, roles));
    }

    /**
     * 鍒犻櫎缁勭粐鏈烘瀯
     *
     * @return 鏇存柊缁勭粐鏈烘瀯
     */
    @Operation(summary = "鍒犻櫎缁勭粐鏈烘瀯")
    @DeleteMapping("/delete")
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:dept:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long deptId) {
        if (null == deptId) {
            return Result.illegal("缁勭粐鏈烘瀯淇℃伅涓嶅瓨鍦");
        }

        return sysDeptService.delete(deptId);
    }


    /**
     * 鏇存柊缁勭粐鏈烘瀯
     *
     * @return 鏇存柊缁勭粐鏈烘瀯
     */
    @Operation(summary = "鏇存柊缁勭粐鏈烘瀯")
    @PutMapping("/update")
    @Permission(value = "sys:dept:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDept SysDept,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysDeptService.update(roles, SysDept);
    }

    /**
     * 鏂板缁勭粐鏈烘瀯
     *
     * @return 鏂板缁勭粐鏈烘瀯
     */
    @Operation(summary = "鏂板缁勭粐鏈烘瀯")
    @PostMapping("/save")
    @Permission(value = "sys:dept:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysDept> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysDept SysDept,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeptService.save(SysDept, roles);
    }
}
