package com.chua.starter.school.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiGroup;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.school.dto.SchoolSyncConfigDTO;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.service.SchoolSyncConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学校同步配置Controller
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/school/sync")
@Tag(name = "学校同步配置", description = "学校同步配置相关接口")
@RequiredArgsConstructor
public class SchoolSyncConfigController {

    final SchoolSyncConfigService schoolSyncConfigService;


    /**
     * 执行同步
     *
     * @param schoolSyncConfigId 学校同步配置ID
     * @return 是否成功
     */
    @GetMapping("/execute")
    @Operation(summary = "执行同步", description = "根据学校同步配置ID执行同步")
    public ReturnResult<Boolean> executeSync(@Parameter(description = "学校同步配置ID") @RequestParam Long schoolSyncConfigId) {
        return ReturnResult.ok(schoolSyncConfigService.executeSync(schoolSyncConfigId));
    }
    /**
     * 新增学校同步配置
     *
     * @param schoolSyncConfig 学校同步配置
     * @return 是否成功
     */
    @PostMapping("/save")
    @Operation(summary = "新增学校同步配置", description = "新增学校同步配置")
    public ReturnResult<Boolean> saveSchoolSyncConfig(
            @Validated(AddGroup.class) @ParameterObject @RequestBody SchoolSyncConfig schoolSyncConfig, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return schoolSyncConfigService.saveSyncConfig(schoolSyncConfig);
    }

    /**
     * 更新学校同步配置
     *
     * @param schoolSyncConfig 学校同步配置
     * @return 是否成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新学校同步配置", description = "更新学校同步配置")
    public ReturnResult<Boolean> updateSchoolSyncConfig(
            @Validated(UpdateGroup.class) @ParameterObject @RequestBody SchoolSyncConfig schoolSyncConfig, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return schoolSyncConfigService.updateSyncConfig(schoolSyncConfig);
    }

    /**
     * 分页查询学校同步配置列表
     *
     * @param query 查询条件
     * @return 学校同步配置列表
     */
    @PostMapping("/page")
    @ApiGroup(IgnoreGroup.class)
    @Operation(summary = "分页查询学校同步配置列表", description = "根据条件分页查询学校同步配置列表")
    public ReturnPageResult<SchoolSyncConfig> page(@ParameterObject @RequestBody SchoolSyncConfigDTO query) {
        return schoolSyncConfigService.pageSyncConfig(query);
    }


    /**
     * 删除学校同步配置
     *
     * @param schoolSyncConfigId 学校同步配置ID
     * @return 是否成功
     */
    @DeleteMapping("/{schoolSyncConfigId}")
    @Operation(summary = "删除学校同步配置", description = "根据学校同步配置ID删除配置")
    public ReturnResult<Boolean> deleteSchoolSyncConfig(@Parameter(description = "学校同步配置ID") @PathVariable Long schoolSyncConfigId) {
        return schoolSyncConfigService.deleteSyncConfig(schoolSyncConfigId);
    }
} 