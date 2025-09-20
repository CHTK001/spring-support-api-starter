package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.SystemSoftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统软件管理控制器
 *
 * @author CH
 * @since 2025/01/15
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/system-soft")
@RequiredArgsConstructor
@Tag(name = "系统软件管理", description = "系统软件管理相关接口")
public class SystemSoftController {

    private final SystemSoftService systemSoftService;

    @GetMapping("/list")
    @Operation(summary = "查询软件列表", description = "根据条件查询软件列表")
    public ReturnResult<List<SystemSoft>> list(
            @Parameter(description = "软件代码") @RequestParam(required = false) String softCode,
            @Parameter(description = "软件名称") @RequestParam(required = false) String softName,
            @Parameter(description = "软件分类") @RequestParam(required = false) String category,
            @Parameter(description = "软件状态") @RequestParam(required = false) Integer status) {
        return systemSoftService.selectSoftList(softCode, softName, category, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取软件详情", description = "根据软件ID获取软件详细信息")
    public ReturnResult<SystemSoft> getById(@Parameter(description = "软件ID") @PathVariable Integer id) {
        try {
            // 这里暂时使用简单的实现，后续可以优化
            return ReturnResult.ok(null);
        } catch (Exception e) {
            log.error("获取软件详情异常 - id: {}", id, e);
            return ReturnResult.error("获取软件详情异常: " + e.getMessage());
        }
    }

    @GetMapping("/code/{softCode}")
    @Operation(summary = "根据代码获取软件信息", description = "根据软件代码获取软件信息")
    public ReturnResult<SystemSoft> getBySoftCode(@Parameter(description = "软件代码") @PathVariable String softCode) {
        return systemSoftService.getBySoftCode(softCode);
    }

    @PostMapping
    @Operation(summary = "新增软件", description = "新增软件信息")
    public ReturnResult<SystemSoft> add(@Parameter(description = "软件信息") @RequestBody @Validated SystemSoft systemSoft) {
        systemSoft.setSystemSoftId(null); // 确保是新增操作
        return systemSoftService.saveOrUpdate(systemSoft);
    }

    @PutMapping
    @Operation(summary = "更新软件", description = "更新软件信息")
    public ReturnResult<SystemSoft> update(@Parameter(description = "软件信息") @RequestBody @Validated SystemSoft systemSoft) {
        if (systemSoft.getSystemSoftId() == null) {
            return ReturnResult.error("更新操作必须提供软件ID");
        }
        return systemSoftService.saveOrUpdate(systemSoft);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除软件", description = "根据ID删除软件")
    public ReturnResult<Boolean> delete(@Parameter(description = "软件ID") @PathVariable Integer id) {
        return systemSoftService.removeById(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除软件", description = "批量删除软件")
    public ReturnResult<Boolean> deleteBatch(@Parameter(description = "软件ID列表") @RequestBody List<Integer> ids) {
        return systemSoftService.removeByIds(ids);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步软件信息", description = "从指定仓库或默认仓库同步软件信息")
    public ReturnResult<Integer> syncFromRegistry(
            @Parameter(description = "仓库ID，为空时使用默认仓库") @RequestParam(required = false) Integer registryId) {
        return systemSoftService.syncFromRegistry(registryId);
    }

    @PostMapping("/sync-single")
    @Operation(summary = "同步单个软件", description = "从指定仓库同步单个软件信息")
    public ReturnResult<SystemSoft> syncSingleSoftware(
            @Parameter(description = "仓库ID") @RequestParam Integer registryId,
            @Parameter(description = "软件名称") @RequestParam String softwareName) {
        return systemSoftService.syncSingleSoftware(registryId, softwareName);
    }

    @PostMapping("/install")
    @Operation(summary = "安装软件", description = "将软件安装到指定服务器")
    public ReturnResult<Boolean> installSoftware(
            @Parameter(description = "软件ID") @RequestParam Integer softId,
            @Parameter(description = "服务器ID列表") @RequestParam List<Integer> serverIds,
            @Parameter(description = "镜像标签") @RequestParam(defaultValue = "latest") String imageTag) {
        return systemSoftService.installSoftware(softId, serverIds, imageTag);
    }

    @GetMapping("/{id}/tags")
    @Operation(summary = "获取软件可用标签", description = "获取软件的可用版本标签列表")
    public ReturnResult<List<String>> getAvailableTags(@Parameter(description = "软件ID") @PathVariable Integer id) {
        return systemSoftService.getAvailableTags(id);
    }

    @GetMapping("/check-code")
    @Operation(summary = "检查软件代码是否存在", description = "检查软件代码是否已被使用")
    public ReturnResult<Boolean> checkSoftCodeExists(
            @Parameter(description = "软件代码") @RequestParam String softCode,
            @Parameter(description = "排除的软件ID") @RequestParam(required = false) Integer excludeId) {
        return systemSoftService.checkSoftCodeExists(softCode, excludeId);
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量更新状态", description = "批量更新软件状态")
    public ReturnResult<Boolean> batchUpdateStatus(
            @Parameter(description = "软件ID列表") @RequestParam List<Integer> softIds,
            @Parameter(description = "新状态") @RequestParam Integer status) {
        return systemSoftService.batchUpdateStatus(softIds, status);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索软件", description = "根据关键字搜索软件")
    public ReturnResult<List<SystemSoft>> searchByKeyword(
            @Parameter(description = "关键字") @RequestParam String keyword) {
        return systemSoftService.searchByKeyword(keyword);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取软件", description = "根据分类获取软件列表")
    public ReturnResult<List<SystemSoft>> getByCategory(@Parameter(description = "分类") @PathVariable String category) {
        return systemSoftService.getByCategory(category);
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门软件", description = "获取热门软件列表")
    public ReturnResult<List<SystemSoft>> getPopularSofts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        return systemSoftService.getPopularSofts(limit);
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新软件", description = "获取最新添加的软件列表")
    public ReturnResult<List<SystemSoft>> getLatestSofts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        return systemSoftService.getLatestSofts(limit);
    }
}