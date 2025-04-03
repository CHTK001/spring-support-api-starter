package com.chua.starter.school.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.service.SchoolSyncConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 学校同步Controller
 *
 * @author CH
 */
@RestController
@RequestMapping("/school/sync")
@Tag(name = "学校同步", description = "学校数据同步相关接口")
public class SchoolSyncController {

    @Autowired
    private SchoolSyncConfigService syncConfigService;

    /**
     * 分页查询同步配置
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 同步配置列表
     */
    @GetMapping("/config/list")
    @Operation(summary = "分页查询同步配置", description = "分页查询学校同步配置列表")
    public ResponseEntity<IPage<SchoolSyncConfig>> listSyncConfig(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(syncConfigService.pageSyncConfig(pageNum, pageSize));
    }

    /**
     * 获取同步配置详情
     *
     * @param id 配置ID
     * @return 同步配置详情
     */
    @GetMapping("/config/{id}")
    @Operation(summary = "获取同步配置详情", description = "根据配置ID获取同步配置详情")
    public ResponseEntity<SchoolSyncConfig> getSyncConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        return ResponseEntity.ok(syncConfigService.getSyncConfigDetail(id));
    }

    /**
     * 保存同步配置
     *
     * @param config 同步配置
     * @return 是否保存成功
     */
    @PostMapping("/config/save")
    @Operation(summary = "保存同步配置", description = "保存或更新同步配置")
    public ResponseEntity<Boolean> saveSyncConfig(@RequestBody SchoolSyncConfig config) {
        return ResponseEntity.ok(syncConfigService.saveSyncConfig(config));
    }

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    @DeleteMapping("/config/{id}")
    @Operation(summary = "删除同步配置", description = "根据配置ID删除同步配置")
    public ResponseEntity<Boolean> deleteSyncConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        return ResponseEntity.ok(syncConfigService.deleteSyncConfig(id));
    }

    /**
     * 执行同步
     *
     * @param id 配置ID
     * @return 是否执行成功
     */
    @PostMapping("/execute/{id}")
    @Operation(summary = "执行同步", description = "手动执行指定配置的同步任务")
    public ResponseEntity<Boolean> executeSync(@Parameter(description = "配置ID") @PathVariable Long id) {
        return ResponseEntity.ok(syncConfigService.executeSync(id));
    }

    /**
     * 同步指定学校
     *
     * @param schoolId 高考网学校ID
     * @return 是否同步成功
     */
    @PostMapping("/school/{schoolId}")
    @Operation(summary = "同步指定学校", description = "手动同步指定高考网学校ID的学校数据")
    public ResponseEntity<Boolean> syncSchool(@Parameter(description = "高考网学校ID") @PathVariable String schoolId) {
        return ResponseEntity.ok(syncConfigService.syncSchool(schoolId));
    }
} 