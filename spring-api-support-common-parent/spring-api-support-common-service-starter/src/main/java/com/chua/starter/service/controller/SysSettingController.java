package com.chua.starter.service.controller;

import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.common.support.spi.annotations.SpiIgnore;
import com.chua.common.support.utils.BeanUtils;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.api.annotations.ApiPlatform;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysSettingHistoryService;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingHistory;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import com.chua.starter.service.pojo.setting.SysSettingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
* 系统设置接口(缓存)
* @since 2024/8/7
* @author CH
 */
@Tag(name = "系统全局设置接口")
@RestController
@RequestMapping("/v2/setting")
@RequiredArgsConstructor
public class SysSettingController {
    /** Setting_列表_缓存_键_前缀 */
    public static final String SETTING_LIST_CACHE_KEY_PREFIX = "sys:setting:v2:";
    /** Runtime_公共_codec_请求_键 */
    private static final String RUNTIME_PUBLIC_CODEC_REQUEST_KEY = "CodecRequestKey";
    /** Admin */
    private static final String ADMIN = "ADMIN";

    /** SYS设置服务 */
    private final SysSettingService sysSettingService;
    /** SYS设置历史服务 */
    private final SysSettingHistoryService sysSettingHistoryService;


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
    public ReturnPageResult<SysSetting> pageForSetting(
            @ParameterObject Query<SysSetting> query,
            @ParameterObject SysSetting sysSetting,
            @RequestParam(required = false) String sysSettingValueType,
            @RequestParam(required = false) Integer sysSettingInSystem,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if (null != sysSettingValueType && !sysSettingValueType.isBlank()) {
            sysSetting.setSysSettingValueType(sysSettingValueType);
        }
        if (null != sysSettingInSystem) {
            sysSetting.setSysSettingInSystem(sysSettingInSystem);
        }
        return PageResultUtils.ok(sysSettingService.pageForSetting(query, sysSetting, roles));
    }
    /**
    * 设置分页列表
    *
    * @return 设置分页列表
     */
    @Operation(summary = "默认设置列表")
    @GetMapping("/default")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'sys:setting:default'")
    public ReturnResult<List<SysSettingVO>> listForDefault() {
        List<SysSetting> aDefault = sysSettingService.list(new SysSettingUpdate("default,sso,default-1", null), Collections.emptySet());
        return ReturnResult.ok(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
    }

    /**
    * 设置分页列表
    *
    * @param sysSettingUpdate 条件
    * @return 设置分页列表
    * @param true true
     */
    @Operation(summary = "设置列表")
    @GetMapping("/list")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "T(com.chua.starter.service.controller.SysSettingController).SETTING_LIST_CACHE_KEY_PREFIX + #sysSettingUpdate.sysSettingGroup")
    public ReturnResult<List<SysSettingVO>> list(@ParameterObject SysSettingUpdate sysSettingUpdate, @Parameter(hidden = true) @UserValue("roles") HashSet<String> roles) {
        Set<String> safeRoles = null == roles ? Collections.emptySet() : roles;
        List<SysSetting> list = sysSettingService.list(sysSettingUpdate, roles);
        if(!AuthConstant.isAdmin(safeRoles)) {
            list = list.stream()
                    .filter(it -> {
                        if (RUNTIME_PUBLIC_CODEC_REQUEST_KEY.equals(it.getSysSettingName())) {
                            return true;
                        }
                        return null == it.getSysSettingAdmin() || it.getSysSettingAdmin() != 1;
                    })
                    .toList();
        }
        return ReturnResult.ok(BeanUtils.copyPropertiesList(list, SysSettingVO.class));
    }

    /**
    * 删除设置
    *
    * @return 更新设置
    * @param true true
     */
    @Operation(summary = "删除设置")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(@Parameter(hidden = true) @UserValue("roles") Set<String> roles, Long sysSettingId) {
        if (null == sysSettingId) {
            return ReturnResult.illegal("设置信息不存在");
        }

        return sysSettingService.delete(sysSettingId);
    }

    /**
    * 更新设置
    *
    * @return 更新设置
     */
    @Operation(summary = "更新设置")
    @PutMapping("/updateBatch")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> updateBatch(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody List<SysSetting> sysSetting,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingService.updateBatch(roles, sysSetting);
    }

    /**
    * 更新设置
    *
    * @return 更新设置
     */
    @Operation(summary = "更新设置")
    @PutMapping("/update")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysSetting sysSetting,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingService.update(roles, sysSetting);
    }

    /**
    * 新增设置
    *
    * @return 新增设置
    * @param true true
     */
    @Operation(summary = "新增设置")
    @PostMapping("/save")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysSetting> save( @Parameter(hidden = true) @UserValue("roles") Set<String> roles,@Validated(AddGroup.class) @RequestBody SysSetting sysSetting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingService.saveForEntity(sysSetting, roles);
    }

    /**
    * 导出配置
    *
    * @param group 配置分组（可选）
    * @return JSON配置字符串
    * @param false false
     */
    @Operation(summary = "导出配置")
    @GetMapping("/export")
    @ApiPlatform("system")
    @Permission(role = {SUPER_ADMIN})
    public ReturnResult<String> exportSettings(@RequestParam(required = false) String group) {
        return sysSettingService.exportSettings(group);
    }

    /**
    * 导入配置
    *
    * @param json 配置JSON字符串
    * @param overwrite 是否覆盖已存在的配置
    * @return 导入结果，返回批次号
     */
    @Operation(summary = "导入配置")
    @PostMapping("/import")
    @ApiPlatform("system")
    @Permission(role = {SUPER_ADMIN})
    public ReturnResult<String> importSettings(
            @RequestBody String json,
            @RequestParam(defaultValue = "false") boolean overwrite,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysSettingService.importSettings(json, overwrite, roles);
    }

    /**
    * 查询配置变更历史
    *
    * @param query 分页参数
    * @param settingId 配置标识（可选）
    * @param group 配置分组（可选）
    * @return 变更历史分页列表
     */
    @Operation(summary = "查询配置变更历史")
    @GetMapping("/history/page")
    @ApiPlatform("system")
    @Permission(role = {SUPER_ADMIN})
    public ReturnPageResult<SysSettingHistory> pageHistory(
            @ParameterObject Query<SysSettingHistory> query,
            @RequestParam(required = false) Integer settingId,
            @RequestParam(required = false) String group) {
        return PageResultUtils.ok(sysSettingHistoryService.pageHistory(query, settingId, group));
    }

    /**
    * 根据批次号查询变更历史
    *
    * @param batchNo 批次号
    * @return 变更历史列表
     */
    @Operation(summary = "根据批次号查询变更历史")
    @GetMapping("/history/batch")
    @ApiPlatform("system")
    @Permission(role = {SUPER_ADMIN})
    public ReturnResult<List<SysSettingHistory>> listByBatchNo(@RequestParam String batchNo) {
        return ReturnResult.ok(sysSettingHistoryService.listByBatchNo(batchNo));
    }

    /**
    * 回滚到指定批次
    *
    * @param batchNo 批次号
    * @return 回滚结果
     */
    @Operation(summary = "回滚到指定批次")
    @PostMapping("/history/rollback/batch")
    @ApiPlatform("system")
    @Permission(role = {SUPER_ADMIN})
    public ReturnResult<Boolean> rollbackToBatch(
            @RequestParam String batchNo,
            @Parameter(hidden = true) @UserValue("userId") String operatorId,
            @Parameter(hidden = true) @UserValue("username") String operatorName) {
        return sysSettingHistoryService.rollbackToBatch(batchNo, operatorId, operatorName);
    }

    /**
    * 回滚到指定历史记录
    *
    * @param historyId 历史记录标识
    * @return 回滚结果
     */
    @Operation(summary = "回滚单条配置到指定历史记录")
    @PostMapping("/history/rollback/single")
    @ApiPlatform("system")
    @Permission(role = {SUPER_ADMIN})
    public ReturnResult<Boolean> rollbackToHistory(
            @RequestParam Long historyId,
            @Parameter(hidden = true) @UserValue("userId") String operatorId,
            @Parameter(hidden = true) @UserValue("username") String operatorName) {
        return sysSettingHistoryService.rollbackToHistory(historyId, operatorId, operatorName);
    }
}
