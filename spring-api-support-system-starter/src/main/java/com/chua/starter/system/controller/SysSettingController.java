package com.chua.starter.system.controller;

import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiPlatform;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysSetting;
import com.chua.starter.system.pojo.setting.SysSettingUpdate;
import com.chua.starter.system.pojo.setting.SysSettingVO;
import com.chua.starter.system.service.SysSettingService;
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
 * 系统设置接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "系统设置接口")
@RestController
@RequestMapping("/v2/setting")
@RequiredArgsConstructor
@ApiPlatform("system")
public class SysSettingController {

    private final SysSettingService sysSettingService;


    /**
     * 设置分页列表
     *
     * @param sysSetting 条件
     * @return 设置分页列表
     */
    @Operation(summary = "设置分页列表")
    @GetMapping("/page")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnPageResult<SysSetting> pageForSetting(@ParameterObject  Query<SysSetting> query, @ParameterObject  SysSetting sysSetting, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysSettingService.pageForSetting(query, sysSetting, roles));
    }
    /**
     * 设置分页列表
     *
     * @param sysSettingUpdate 条件
     * @return 设置分页列表
     */
    @Operation(summary = "设置列表")
    @GetMapping("/list")
    @ApiPlatform("system")
    public ReturnResult<List<SysSetting>> list(@ParameterObject  SysSettingUpdate sysSettingUpdate, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysSettingService.list(sysSettingUpdate, roles));
    }
    /**
     * 设置分页列表
     *
     * @return 设置分页列表
     */
    @Operation(summary = "默认设置列表")
    @GetMapping("/default")
    @ApiPlatform("system")
    @Ignore
    public ReturnResult<List<SysSettingVO>> listForDefault(@Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        List<SysSetting> aDefault = sysSettingService.list(new SysSettingUpdate("default"), roles);
        return ReturnResult.ok(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
    }

    /**
     * 删除设置
     *
     * @return 更新设置
     */
    @Operation(summary = "删除设置")
    @DeleteMapping("/delete")
    @OperateLog
    @SysLogger
    @ApiPlatform("system")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:setting:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long settingId) {
        if (null == settingId) {
            return Result.illegal("设置信息不存在");
        }

        return sysSettingService.delete(settingId);
    }

    /**
     * 更新设置
     *
     * @return 更新设置
     */
    @Operation(summary = "更新设置")
    @PutMapping("/update")
    @OperateLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysSetting sysSetting,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysSettingService.update(roles, sysSetting);
    }

    /**
     * 新增设置
     *
     * @return 新增设置
     */
    @Operation(summary = "新增设置")
    @PostMapping("/save")
    @OperateLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysSetting> save( @Parameter(hidden = true) @UserValue("roles") Set<String> roles,@Validated(AddGroup.class) @RequestBody SysSetting sysSetting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysSettingService.saveForEntity(sysSetting, roles);
    }
}
