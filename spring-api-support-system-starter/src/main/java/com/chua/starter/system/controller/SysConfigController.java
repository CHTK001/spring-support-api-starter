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
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysSetting;
import com.chua.starter.system.pojo.setting.SysSettingUpdate;
import com.chua.starter.system.pojo.setting.SysSettingVO;
import com.chua.starter.system.service.SysConfigService;
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
@Tag(name = "系统配置接口")
@RestController
@RequestMapping("/v2/config")
@RequiredArgsConstructor
@ApiPlatform("system")
public class SysConfigController {

    private final SysConfigService sysConfigService;


    /**
     * 设置分页列表
     *
     * @param sysSetting 条件
     * @return 设置分页列表
     */
    @Operation(summary = "系统配置分页列表")
    @GetMapping("/page")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnPageResult<SysSetting> pageForSetting(@ParameterObject  Query<SysSetting> query, @ParameterObject  SysSetting sysSetting, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysConfigService.pageForSetting(query, sysSetting, roles));
    }
    /**
     * 系统配置分页列表
     *
     * @param sysSettingUpdate 条件
     * @return 系统配置分页列表
     */
    @Operation(summary = "系统配置列表")
    @GetMapping("/list")
    @ApiPlatform("system")
    public ReturnResult<List<SysSettingVO>> list(@ParameterObject  SysSettingUpdate sysSettingUpdate, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        List<SysSetting> list = sysConfigService.list(sysSettingUpdate, roles);
        if(!AuthConstant.hasAdmin(roles)) {
            list = list.stream().filter(it -> null != it.getSysSettingAdmin() && it.getSysSettingAdmin() != 1).toList();
        }
        return ReturnResult.ok(BeanUtils.copyPropertiesList(list, SysSettingVO.class));
    }
    /**
     * 系统配置分页列表
     *
     * @return 系统配置分页列表
     */
    @Operation(summary = "默认系统配置列表")
    @GetMapping("/default")
    @ApiPlatform("system")
    @Ignore
    public ReturnResult<List<SysSettingVO>> listForDefault(@Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        List<SysSetting> aDefault = sysConfigService.list(new SysSettingUpdate("default"), roles);
        return ReturnResult.ok(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
    }

    /**
     * 删除系统配置
     *
     * @return 更新系统配置
     */
    @Operation(summary = "删除系统配置")
    @DeleteMapping("/delete")
    @OperateLog
    @UserLogger
    @ApiPlatform("system")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:setting:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysSettingId) {
        if (null == sysSettingId) {
            return Result.illegal("系统配置信息不存在");
        }

        return sysConfigService.delete(sysSettingId);
    }

    /**
     * 更新系统配置
     *
     * @return 更新系统配置
     */
    @Operation(summary = "更新系统配置")
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

        return sysConfigService.update(roles, sysSetting);
    }

    /**
     * 新增系统配置
     *
     * @return 新增系统配置
     */
    @Operation(summary = "新增系统配置")
    @PostMapping("/save")
    @OperateLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysSetting> save( @Parameter(hidden = true) @UserValue("roles") Set<String> roles,@Validated(AddGroup.class) @RequestBody SysSetting sysSetting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysConfigService.saveForEntity(sysSetting, roles);
    }
}
