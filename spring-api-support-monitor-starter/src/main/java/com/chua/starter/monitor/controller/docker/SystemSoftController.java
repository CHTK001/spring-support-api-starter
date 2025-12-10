package com.chua.starter.monitor.controller.docker;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.docker.SystemSoftService;
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

    /**
     * 分页查询软件列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页软件列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询软件列表", description = "分页获取软件列表")
    public ReturnResult<IPage<SystemSoft>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "软件代码") @RequestParam(required = false) String softCode,
            @Parameter(description = "软件名称") @RequestParam(required = false) String softName,
            @Parameter(description = "软件分类") @RequestParam(required = false) String category,
            @Parameter(description = "软件状态") @RequestParam(required = false) Integer status) {

        try {
            log.debug("分页查询软件列表, page: {}, size: {}", page, size);

            // 构建查询条件
            QueryWrapper<SystemSoft> queryWrapper = new QueryWrapper<>();
            if (softCode != null && !softCode.isEmpty()) {
                queryWrapper.like("system_soft_code", softCode);
            }
            if (softName != null && !softName.isEmpty()) {
                queryWrapper.like("system_soft_name", softName);
            }
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq("system_soft_category", category);
            }
            if (status != null) {
                queryWrapper.eq("system_soft_status", status);
            }

            IPage<SystemSoft> pageResult = systemSoftService.page(new Page<>(page, size), queryWrapper);
            log.debug("分页查询软件列表成功, count: {}", pageResult.getRecords().size());
            return ReturnResult.ok(pageResult);

        } catch (Exception e) {
            log.error("分页查询软件列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件查询软件列表
     *
     * @param softCode 软件代码
     * @param softName 软件名称
     * @param category 软件分类
     * @param status   软件状态
     * @return 软件列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询软件列表", description = "根据条件查询软件列表")
    public ReturnResult<List<SystemSoft>> list(
            @Parameter(description = "软件代码") @RequestParam(required = false) String softCode,
            @Parameter(description = "软件名称") @RequestParam(required = false) String softName,
            @Parameter(description = "软件分类") @RequestParam(required = false) String category,
            @Parameter(description = "软件状态") @RequestParam(required = false) Integer status) {
        return systemSoftService.selectSoftListForSoft(softCode, softName, category, status);
    }

    /**
     * 根据软件ID获取软件详细信息
     *
     * @param id 软件ID
     * @return 软件详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取软件详情", description = "根据软件ID获取软件详细信息")
    public ReturnResult<SystemSoft> getById(@Parameter(description = "软件ID") @PathVariable Integer id) {
        try {
            SystemSoft soft = systemSoftService.getById(id);
            if (soft == null) {
                return ReturnResult.error("软件不存在");
            }
            return ReturnResult.ok(soft);
        } catch (Exception e) {
            log.error("获取软件详情异常 - id: {}", id, e);
            return ReturnResult.error("获取软件详情异常: " + e.getMessage());
        }
    }

    /**
     * 根据软件代码获取软件信息
     *
     * @param softCode 软件代码
     * @return 软件信息
     */
    @GetMapping("/code/{softCode}")
    @Operation(summary = "根据代码获取软件信息", description = "根据软件代码获取软件信息")
    public ReturnResult<SystemSoft> getBySoftCode(@Parameter(description = "软件代码") @PathVariable String softCode) {
        return systemSoftService.getBySoftCodeForSoft(softCode);
    }

    /**
     * 新增软件信息
     *
     * @param systemSoft 软件信息
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增软件", description = "新增软件信息")
    public ReturnResult<SystemSoft> add(@Parameter(description = "软件信息") @RequestBody @Validated SystemSoft systemSoft) {
        systemSoft.setSystemSoftId(null); 
        return systemSoftService.saveOrUpdateForSoft(systemSoft);
    }

    /**
     * 更新软件信息
     *
     * @param systemSoft 软件信息
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新软件", description = "更新软件信息")
    public ReturnResult<SystemSoft> update(@Parameter(description = "软件信息") @RequestBody @Validated SystemSoft systemSoft) {
        if (systemSoft.getSystemSoftId() == null) {
            return ReturnResult.error("更新操作必须提供软件ID");
        }
        return systemSoftService.saveOrUpdateForSoft(systemSoft);
    }

    /**
     * 根据ID删除软件
     *
     * @param id 软件ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除软件", description = "根据ID删除软件")
    public ReturnResult<Boolean> delete(@Parameter(description = "软件ID") @PathVariable Integer id) {
        return systemSoftService.removeById(id);
    }

    /**
     * 批量删除软件
     *
     * @param ids 软件ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除软件", description = "批量删除软件")
    public ReturnResult<Boolean> deleteBatch(@Parameter(description = "软件ID列表") @RequestBody List<Integer> ids) {
        return systemSoftService.removeByIds(ids);
    }

    /**
     * 从指定仓库或默认仓库同步软件信息
     *
     * @param registryId 仓库ID，为空时使用默认仓库
     * @return 同步结果
     */
    @PostMapping("/sync")
    @Operation(summary = "同步软件信息", description = "从指定仓库或默认仓库同步软件信息")
    public ReturnResult<Integer> syncFromRegistry(
            @Parameter(description = "仓库ID，为空时使用默认仓库") @RequestParam(required = false) Integer registryId) {
        return systemSoftService.syncFromRegistry(registryId);
    }

    /**
     * 从指定仓库同步单个软件信息
     *
     * @param registryId   仓库ID
     * @param softwareName 软件名称
     * @return 同步结果
     */
    @PostMapping("/sync-single")
    @Operation(summary = "同步单个软件", description = "从指定仓库同步单个软件信息")
    public ReturnResult<SystemSoft> syncSingleSoftware(
            @Parameter(description = "仓库ID") @RequestParam Integer registryId,
            @Parameter(description = "软件名称") @RequestParam String softwareName) {
        return systemSoftService.syncSingleSoftware(registryId, softwareName);
    }

    /**
     * 将软件安装到指定服务器
     *
     * @param softId   软件ID
     * @param serverIds 服务器ID列表
     * @param imageTag  镜像标签
     * @return 安装结果
     */
    @PostMapping("/install")
    @Operation(summary = "安装软件", description = "将软件安装到指定服务器")
    public ReturnResult<Boolean> installSoftware(
            @Parameter(description = "软件ID") @RequestParam Integer softId,
            @Parameter(description = "服务器ID列表") @RequestParam List<Integer> serverIds,
            @Parameter(description = "镜像标签") @RequestParam(defaultValue = "latest") String imageTag) {
        return systemSoftService.installSoftware(softId, serverIds, imageTag);
    }

    /**
     * 获取软件的可用版本标签列表
     *
     * @param id 软件ID
     * @return 可用标签列表
     */
    @GetMapping("/{id}/tags")
    @Operation(summary = "获取软件可用标签", description = "获取软件的可用版本标签列表")
    public ReturnResult<List<String>> getAvailableTags(@Parameter(description = "软件ID") @PathVariable Integer id) {
        return systemSoftService.getAvailableTags(id);
    }

    /**
     * 检查软件代码是否已被使用
     *
     * @param softCode  软件代码
     * @param excludeId 排除的软件ID
     * @return 检查结果
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查软件代码是否存在", description = "检查软件代码是否已被使用")
    public ReturnResult<Boolean> checkSoftCodeExists(
            @Parameter(description = "软件代码") @RequestParam String softCode,
            @Parameter(description = "排除的软件ID") @RequestParam(required = false) Integer excludeId) {
        return systemSoftService.checkSoftCodeExists(softCode, excludeId);
    }

    /**
     * 批量更新软件状态
     *
     * @param softIds 软件ID列表
     * @param status  新状态
     * @return 更新结果
     */
    @PutMapping("/batch-status")
    @Operation(summary = "批量更新状态", description = "批量更新软件状态")
    public ReturnResult<Boolean> batchUpdateStatus(
            @Parameter(description = "软件ID列表") @RequestParam List<Integer> softIds,
            @Parameter(description = "新状态") @RequestParam Integer status) {
        return systemSoftService.batchUpdateStatus(softIds, status);
    }

    /**
     * 根据关键字搜索软件
     *
     * @param keyword 关键字
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索软件", description = "根据关键字搜索软件")
    public ReturnResult<List<SystemSoft>> searchByKeyword(
            @Parameter(description = "关键字") @RequestParam String keyword) {
        return systemSoftService.searchByKeyword(keyword);
    }

    /**
     * 根据分类获取软件列表
     *
     * @param category 分类
     * @return 软件列表
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取软件", description = "根据分类获取软件列表")
    public ReturnResult<List<SystemSoft>> getByCategory(@Parameter(description = "分类") @PathVariable String category) {
        return systemSoftService.getByCategory(category);
    }

    /**
     * 获取热门软件列表
     *
     * @param limit 限制数量
     * @return 热门软件列表
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门软件", description = "获取热门软件列表")
    public ReturnResult<List<SystemSoft>> getPopularSofts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        return systemSoftService.getPopularSofts(limit);
    }

    /**
     * 获取最新添加的软件列表
     *
     * @param limit 限制数量
     * @return 最新软件列表
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新软件", description = "获取最新添加的软件列表")
    public ReturnResult<List<SystemSoft>> getLatestSofts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        return systemSoftService.getLatestSofts(limit);
    }
}