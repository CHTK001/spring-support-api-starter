package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.system.entity.SysService;
import com.chua.starter.system.pojo.service.SysServiceBindV1Request;
import com.chua.starter.system.service.SysServiceService;
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
 * 服务接口
 *
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "服务接口")
@Tag(name = "服务接口")
@RequestMapping("/v2/service")
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class SysServiceController {


    final SysServiceService sysServiceService;

    /**
     * 分页
     *
     * @param query
     * @return
     */
    @GetMapping("list")
    @Operation(summary = "查询服务")
    public ReturnResult<List<SysService>> list(@ParameterObject SysService sysService) {
        return sysServiceService.listFor(sysService);
    }

    /**
     * 分页
     *
     * @param query
     * @return
     */
    @GetMapping("page")
    @Operation(summary = "分页查询服务")
    @Permission("sys:service:page")
    public ReturnPageResult<SysService> page(@ParameterObject Query<SysService> query, @ParameterObject SysService sysService) {
        return ReturnPageResultUtils.ok(sysServiceService.pageFor(query, sysService));
    }


    /**
     * 保存
     *
     * @param sysService
     * @return
     */
    @PostMapping("save")
    @Operation(summary = "保存服务")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysService> save(@RequestBody @Validated(AddGroup.class) SysService sysService, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysServiceService.saveFor(sysService);
    }


    /**
     * 更新
     *
     * @param sysService
     * @return
     */
    @PutMapping("update")
    @Operation(summary = "更新服务")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysService sysService, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysServiceService.updateFor(sysService);
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
        return sysServiceService.deleteFor(sysServiceId);
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
    public ReturnResult<Boolean> bind(@ParameterObject @RequestBody SysServiceBindV1Request request) {
        return sysServiceService.bind(request);
    }

}
