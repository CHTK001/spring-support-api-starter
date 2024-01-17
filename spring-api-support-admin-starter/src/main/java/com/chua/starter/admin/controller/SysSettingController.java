package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.admin.service.SysSettingService;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置
 */
@Tag(name = "系统设置接口")
@RestController
@RequestMapping("/v2/setting")
@RequiredArgsConstructor
public class SysSettingController {

    private final SysSettingService sysSettingService;
    /**
     * 分页查询设置列表
     *
     * @return 分页查询设置列表
     */
    @Operation(summary = "分页查询设置列表")
    @GetMapping("page")
    public ReturnPageResult<SysSetting> page(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return PageResultUtils.ok(sysSettingService.page(new Page<>(page, pageSize),
                        Wrappers.<SysSetting>lambdaQuery()
                                .like(StringUtils.isNotBlank(keyword), SysSetting::getSettingName, keyword)
                )
        );
    }
    /**
     * 添加设置
     *
     * @return 添加设置
     */
    @Operation(summary = "添加设置")
    @PostMapping("save")
    public ReturnResult<SysSetting> save(@RequestBody @Validated(AddGroup.class) SysSetting sysSetting , BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(sysSettingService.count(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSettingName, sysSetting.getSettingName())) > 0L) {
            return Result.illegal("配置已存在");
        }

        sysSettingService.saveSetting(sysSetting);
        return Result.ok(sysSetting);
    }
    /**
     * 更新设置
     *
     * @return 更新设置
     */
    @Operation(summary = "更新设置")
    @PutMapping("update")
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class)SysSetting sysSetting, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(sysSettingService.count(Wrappers.<SysSetting>lambdaQuery().ne(SysSetting::getSettingId, sysSetting.getSettingId()).eq(SysSetting::getSettingName, sysSetting.getSettingName())) > 0L) {
            return Result.illegal("配置已存在");
        }

        return Result.of(sysSettingService.updateSetting(sysSetting));
    }
    /**
     * 删除设置
     *
     * @return 删除设置
     */
    @Operation(summary = "删除设置")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> delete(Integer settingId) {
        if(null == settingId) {
            return Result.illegal("信息不存在");
        }


        return Result.of(sysSettingService.deleteSetting(settingId));
    }
}
