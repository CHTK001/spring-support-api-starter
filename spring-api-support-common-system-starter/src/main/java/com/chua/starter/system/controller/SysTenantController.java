package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.pojo.tenant.SysTenantServiceBindV1Request;
import com.chua.starter.system.service.SysTenantService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 租户接口
 *
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "租户接口")
@Tag(name = "租户接口")
@RequestMapping("/v2/tenant")
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class SysTenantController {


    final SysTenantService sysTenantService;

    /**
     * 分页
     *
     * @param query
     * @return
     */
    @GetMapping("page")
    @Operation(summary = "分页查询服务")
    @Permission("sys:tanant:page")
    public ReturnPageResult<SysTenant> page(@ParameterObject Query<SysTenant> query, @ParameterObject SysTenant sysTenant) {
        return ReturnPageResultUtils.ok(sysTenantService.pageFor(query, sysTenant));
    }


    /**
     * 保存
     *
     * @param sysTenant
     * @return
     */
    @PostMapping("save")
    @Operation(summary = "保存服务")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysTenant> save(@RequestBody @Validated(AddGroup.class) SysTenant sysTenant, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysTenantService.saveFor(sysTenant);
    }


    /**
     * 更新
     *
     * @param sysTenant
     * @return
     */
    @PutMapping("update")
    @Operation(summary = "更新服务")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysTenant sysTenant, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysTenantService.updateFor(sysTenant);
    }

    /**
     * 删除
     *
     * @param sysServiceItemId
     * @return
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除服务")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysServiceId) {
        return sysTenantService.deleteFor(sysServiceId);
    }

    /**
     * 绑定服务模块
     *
     * @param sysServiceId
     * @return
     */
    @PutMapping("bind")
    @Operation(summary = "绑定服务模块")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> bind(@ParameterObject @RequestBody SysTenantServiceBindV1Request request) {
        return sysTenantService.bind(request);
    }

}
