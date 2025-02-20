package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.system.entity.SysServiceModule;
import com.chua.starter.system.service.SysServiceModuleService;
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

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 服务模块接口
 *
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "服务模块接口")
@Tag(name = "服务模块接口")
@RequestMapping("/v2/service/module")
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class SysServiceModuleController {


    final SysServiceModuleService sysServiceModuleService;

    /**
     * 分页
     *
     * @param query
     * @return
     */
    @GetMapping("list")
    @Operation(summary = "查询服务模块列表")
    public ReturnResult<List<SysServiceModule>> list(@ParameterObject SysServiceModule sysServiceItem) {
        return sysServiceModuleService.listFor(sysServiceItem);
    }

    /**
     * 分页
     *
     * @param query
     * @return
     */
    @GetMapping("page")
    @Operation(summary = "分页查询服务模块")
    @Permission("sys:service:module:page")
    public ReturnPageResult<SysServiceModule> page(@ParameterObject Query<SysServiceModule> query, @ParameterObject SysServiceModule sysServiceItem) {
        return ReturnPageResultUtils.ok(sysServiceModuleService.pageFor(query, sysServiceItem));
    }


    /**
     * 保存
     *
     * @param sysServiceItem
     * @return
     */
    @PostMapping("save")
    @Operation(summary = "保存服务模块")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysServiceModule> save(@RequestBody @Validated(AddGroup.class) SysServiceModule sysServiceItem, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysServiceModuleService.saveFor(sysServiceItem);
    }


    /**
     * 更新
     *
     * @param sysServiceItem
     * @return
     */
    @PutMapping("update")
    @Operation(summary = "更新服务模块")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysServiceModule sysServiceItem, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysServiceModuleService.updateFor(sysServiceItem);
    }

    /**
     * 删除
     *
     * @param sysServiceModuleId
     * @return
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除服务模块")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysServiceModuleId) {
        return sysServiceModuleService.deleteFor(sysServiceModuleId);
    }

}
