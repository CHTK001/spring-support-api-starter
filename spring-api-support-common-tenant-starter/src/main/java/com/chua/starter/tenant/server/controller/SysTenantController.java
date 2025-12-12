package com.chua.starter.tenant.server.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.server.pojo.SysTenantBindServiceV1Request;
import com.chua.starter.tenant.server.service.SysTenantService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@RestController
@Slf4j
@Api(tags = "租户管理接口")
@Tag(name = "租户管理接口")
@RequestMapping("/v2/tenant")
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class SysTenantController {

    private final SysTenantService sysTenantService;

    /**
     * 查询租户列表
     *
     * @param sysTenant 租户查询条件
     * @return 租户列表
     */
    @GetMapping("list")
    @Operation(summary = "查询租户列表")
    public ReturnResult<List<SysTenant>> listForSysTenant(@ParameterObject SysTenant sysTenant) {
        return sysTenantService.listForSysTenant(sysTenant);
    }

    /**
     * 分页查询租户
     *
     * @param query     分页查询对象
     * @param sysTenant 租户查询条件
     * @return 分页结果
     */
    @GetMapping("page")
    @Operation(summary = "分页查询租户")
    @Permission("sys:tenant:page")
    public ReturnPageResult<SysTenant> pageForSysTenant(@ParameterObject Query<SysTenant> query,
                                                        @ParameterObject SysTenant sysTenant) {
        return ReturnPageResultUtils.ok(sysTenantService.pageForSysTenant(query, sysTenant));
    }

    /**
     * 获取租户详情
     *
     * @param sysTenantId 租户ID
     * @return 租户详情
     */
    @GetMapping("get")
    @Operation(summary = "获取租户详情")
    @Permission("sys:tenant:get")
    public ReturnResult<SysTenant> getForSysTenant(@Parameter(description = "租户ID") Integer sysTenantId) {
        return sysTenantService.getForSysTenant(sysTenantId);
    }

    /**
     * 保存租户
     *
     * @param sysTenant     租户对象
     * @param bindingResult 校验结果
     * @return 保存结果
     */
    @PostMapping("save")
    @Operation(summary = "保存租户")
    @Permission("sys:tenant:save")
    public ReturnResult<SysTenant> saveForSysTenant(@RequestBody @Validated(AddGroup.class) SysTenant sysTenant,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysTenantService.saveForSysTenant(sysTenant);
    }

    /**
     * 更新租户
     *
     * @param sysTenant     租户对象
     * @param bindingResult 校验结果
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新租户")
    @Permission("sys:tenant:update")
    public ReturnResult<Boolean> updateForSysTenant(@RequestBody @Validated(UpdateGroup.class) SysTenant sysTenant,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysTenantService.updateForSysTenant(sysTenant);
    }

    /**
     * 删除租户
     *
     * @param sysTenantId 租户ID
     * @return 删除结果
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除租户")
    @Permission("sys:tenant:delete")
    public ReturnResult<Boolean> deleteForSysTenant(@Parameter(description = "租户ID") Integer sysTenantId) {
        return sysTenantService.deleteForSysTenant(sysTenantId);
    }

    /**
     * 绑定租户服务
     *
     * @param request 绑定请求
     * @return 绑定结果
     */
    @PutMapping("bind/service")
    @Operation(summary = "绑定租户服务")
    @Permission("sys:tenant:bind")
    public ReturnResult<Boolean> bindServiceForSysTenant(@RequestBody SysTenantBindServiceV1Request request) {
        return sysTenantService.bindServiceForSysTenant(request);
    }

    /**
     * 更新租户状态
     *
     * @param sysTenantId 租户ID
     * @param status      状态值 (0-启用, 1-禁用)
     * @return 更新结果
     */
    @PutMapping("status")
    @Operation(summary = "更新租户状态")
    @Permission("sys:tenant:status")
    public ReturnResult<Boolean> updateStatusForSysTenant(
            @Parameter(description = "租户ID") Integer sysTenantId,
            @Parameter(description = "状态值: 0-启用, 1-禁用") Integer status) {
        return sysTenantService.updateStatusForSysTenant(sysTenantId, status);
    }
}
