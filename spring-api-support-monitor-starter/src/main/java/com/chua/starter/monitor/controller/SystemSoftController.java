package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.annotations.OnRouterEvent;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.SystemSoftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 系统软件管理控制器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 */
@Slf4j
@RestController
@RequestMapping("/api/system/soft")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统软件管理", description = "系统软件管理相关接口")
public class SystemSoftController {

    private final SystemSoftService systemSoftService;

    /**
     * 分页查询软件列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param softCode 软件代码
     * @param softName 软件名称
     * @param softCategory 软件分类
     * @param softStatus 软件状态
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询软件列表", description = "根据条件分页查询系统软件列表")
    @OnRouterEvent("软件管理:查询软件列表")
    public ReturnResult<IPage<SystemSoft>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "软件代码") @RequestParam(required = false) String softCode,
            @Parameter(description = "软件名称") @RequestParam(required = false) String softName,
            @Parameter(description = "软件分类") @RequestParam(required = false) String softCategory,
            @Parameter(description = "软件状态") @RequestParam(required = false) Integer softStatus) {

        log.info("分页查询软件列表 - current: {}, size: {}, softCode: {}, softName: {}, softCategory: {}, softStatus: {}",
                current, size, softCode, softName, softCategory, softStatus);

        try {
            Page<SystemSoft> page = new Page<>(current, size);
            IPage<SystemSoft> result = systemSoftService.selectSoftPage(page, softCode, softName, softCategory, softStatus);

            log.info("分页查询软件列表成功 - 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("分页查询软件列表失败 - error: {}", e.getMessage(), e);
            return ReturnResult.error("查询软件列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询软件详情
     *
     * @param id 软件ID
     * @return 软件详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询软件详情", description = "根据ID查询软件详细信息")
    @OnRouterEvent("软件管理:查询软件详情")
    public ReturnResult<SystemSoft> getById(
            @Parameter(description = "软件ID", required = true) @PathVariable @NotNull Long id) {

        log.info("查询软件详情 - id: {}", id);

        try {
            SystemSoft systemSoft = systemSoftService.getById(id);
            if (Objects.isNull(systemSoft)) {
                log.warn("软件不存在 - id: {}", id);
                return ReturnResult.error("软件不存在");
            }

            log.info("查询软件详情成功 - id: {}, softCode: {}", id, systemSoft.getSystemSoftCode());
            return ReturnResult.ok(systemSoft);

        } catch (Exception e) {
            log.error("查询软件详情失败 - id: {}, error: {}", id, e.getMessage(), e);
            return ReturnResult.error("查询软件详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据软件代码查询软件信息
     *
     * @param softCode 软件代码
     * @return 软件信息
     */
    @GetMapping("/code/{softCode}")
    @Operation(summary = "根据代码查询软件", description = "根据软件代码查询软件信息")
    @OnRouterEvent("软件管理:根据代码查询软件")
    public ReturnResult<SystemSoft> getBySoftCode(
            @Parameter(description = "软件代码", required = true) @PathVariable @NotBlank String softCode) {

        log.info("根据软件代码查询软件信息 - softCode: {}", softCode);

        try {
            SystemSoft systemSoft = systemSoftService.selectBySoftCode(softCode);
            if (Objects.isNull(systemSoft)) {
                log.warn("软件不存在 - softCode: {}", softCode);
                return ReturnResult.error("软件不存在");
            }

            log.info("根据软件代码查询软件信息成功 - softCode: {}, id: {}", softCode, systemSoft.getSystemSoftId());
            return ReturnResult.ok(systemSoft);

        } catch (Exception e) {
            log.error("根据软件代码查询软件信息失败 - softCode: {}, error: {}", softCode, e.getMessage(), e);
            return ReturnResult.error("查询软件信息失败: " + e.getMessage());
        }
    }

    /**
     * 新增软件
     *
     * @param systemSoft 软件信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "新增软件", description = "新增系统软件配置")
    @OnRouterEvent("软件管理:新增软件")
    public ReturnResult<Boolean> save(@Parameter(description = "软件信息", required = true) @RequestBody @Valid SystemSoft systemSoft) {

        log.info("新增软件 - softCode: {}, softName: {}", systemSoft.getSystemSoftCode(), systemSoft.getSystemSoftName());

        try {
            // 验证软件配置
            if (!systemSoftService.validateSoftConfig(systemSoft)) {
                log.warn("软件配置验证失败 - softCode: {}", systemSoft.getSystemSoftCode());
                return ReturnResult.error("软件配置验证失败");
            }

            boolean result = systemSoftService.save(systemSoft);
            if (result) {
                log.info("新增软件成功 - softCode: {}, id: {}", systemSoft.getSystemSoftCode(), systemSoft.getSystemSoftId());
                return ReturnResult.ok(true);
            } else {
                log.warn("新增软件失败 - softCode: {}", systemSoft.getSystemSoftCode());
                return ReturnResult.error("新增软件失败");
            }

        } catch (Exception e) {
            log.error("新增软件失败 - softCode: {}, error: {}", systemSoft.getSystemSoftCode(), e.getMessage(), e);
            return ReturnResult.error("新增软件失败: " + e.getMessage());
        }
    }

    /**
     * 更新软件
     *
     * @param systemSoft 软件信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新软件", description = "更新系统软件配置")
    @OnRouterEvent("软件管理:更新软件")
    public ReturnResult<Boolean> update(@Parameter(description = "软件信息", required = true) @RequestBody @Valid SystemSoft systemSoft) {

        log.info("更新软件 - id: {}, softCode: {}", systemSoft.getSystemSoftId(), systemSoft.getSystemSoftCode());

        try {
            // 验证软件配置
            if (!systemSoftService.validateSoftConfig(systemSoft)) {
                log.warn("软件配置验证失败 - id: {}, softCode: {}", systemSoft.getSystemSoftId(), systemSoft.getSystemSoftCode());
                return ReturnResult.error("软件配置验证失败");
            }

            boolean result = systemSoftService.updateById(systemSoft);
            if (result) {
                log.info("更新软件成功 - id: {}, softCode: {}", systemSoft.getSystemSoftId(), systemSoft.getSystemSoftCode());
                return ReturnResult.ok(true);
            } else {
                log.warn("更新软件失败 - id: {}, softCode: {}", systemSoft.getSystemSoftId(), systemSoft.getSystemSoftCode());
                return ReturnResult.error("更新软件失败");
            }

        } catch (Exception e) {
            log.error("更新软件失败 - id: {}, softCode: {}, error: {}", systemSoft.getSystemSoftId(), systemSoft.getSystemSoftCode(), e.getMessage(), e);
            return ReturnResult.error("更新软件失败: " + e.getMessage());
        }
    }

    /**
     * 删除软件
     *
     * @param id 软件ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除软件", description = "根据ID删除系统软件")
    @OnRouterEvent("软件管理:删除软件")
    public ReturnResult<Boolean> delete(
            @Parameter(description = "软件ID", required = true) @PathVariable @NotNull Long id) {

        log.info("删除软件 - id: {}", id);

        try {
            SystemSoft systemSoft = systemSoftService.getById(id);
            if (Objects.isNull(systemSoft)) {
                log.warn("软件不存在 - id: {}", id);
                return ReturnResult.error("软件不存在");
            }

            boolean result = systemSoftService.removeById(id);
            if (result) {
                log.info("删除软件成功 - id: {}, softCode: {}", id, systemSoft.getSystemSoftCode());
                return ReturnResult.ok(true);
            } else {
                log.warn("删除软件失败 - id: {}", id);
                return ReturnResult.error("删除软件失败");
            }

        } catch (Exception e) {
            log.error("删除软件失败 - id: {}, error: {}", id, e.getMessage(), e);
            return ReturnResult.error("删除软件失败: " + e.getMessage());
        }
    }


    /**
     * 同步软件镜像
     *
     * @param softId 软件ID
     * @param serverId 服务器ID
     * @return 操作结果
     */
    @PostMapping("/{softId}/sync")
    @Operation(summary = "同步软件镜像", description = "同步指定软件的镜像到服务器")
    @OnRouterEvent("软件管理:同步软件镜像")
    public ReturnResult<Boolean> syncImage(
            @Parameter(description = "软件ID", required = true) @PathVariable @NotNull Long softId,
            @Parameter(description = "服务器ID", required = true) @RequestParam @NotNull Long serverId) {

        log.info("同步软件镜像 - softId: {}, serverId: {}", softId, serverId);

        try {
            boolean result = systemSoftService.syncSoftImage(softId, serverId);
            if (result) {
                log.info("同步软件镜像成功 - softId: {}, serverId: {}", softId, serverId);
                return ReturnResult.ok(true);
            } else {
                log.warn("同步软件镜像失败 - softId: {}, serverId: {}", softId, serverId);
                return ReturnResult.error("同步软件镜像失败");
            }

        } catch (Exception e) {
            log.error("同步软件镜像失败 - softId: {}, serverId: {}, error: {}", softId, serverId, e.getMessage(), e);
            return ReturnResult.error("同步软件镜像失败: " + e.getMessage());
        }
    }

    /**
     * 拉取软件镜像
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @param imageTag 镜像标签
     * @return 操作结果
     */
    @PostMapping("/{softId}/pull")
    @Operation(summary = "拉取软件镜像", description = "拉取指定软件的镜像到服务器")
    @OnRouterEvent("软件管理:拉取软件镜像")
    public ReturnResult<Boolean> pullImage(
            @Parameter(description = "软件ID", required = true) @PathVariable @NotNull Long softId,
            @Parameter(description = "服务器ID", required = true) @RequestParam @NotNull Long serverId,
            @Parameter(description = "镜像标签", required = true) @RequestParam @NotBlank String imageTag) {

        log.info("拉取软件镜像 - softId: {}, serverId: {}, imageTag: {}", softId, serverId, imageTag);

        try {
            boolean result = systemSoftService.pullSoftImage(softId, serverId, imageTag);
            if (result) {
                log.info("拉取软件镜像成功 - softId: {}, serverId: {}, imageTag: {}", softId, serverId, imageTag);
                return ReturnResult.ok(true);
            } else {
                log.warn("拉取软件镜像失败 - softId: {}, serverId: {}, imageTag: {}", softId, serverId, imageTag);
                return ReturnResult.error("拉取软件镜像失败");
            }

        } catch (Exception e) {
            log.error("拉取软件镜像失败 - softId: {}, serverId: {}, imageTag: {}, error: {}",
                    softId, serverId, imageTag, e.getMessage(), e);
            return ReturnResult.error("拉取软件镜像失败: " + e.getMessage());
        }
    }

    /**
     * 删除软件镜像
     *
     * @param softId 软件ID
     * @param serverId 服务器ID
     * @param imageId 镜像ID
     * @return 操作结果
     */
    @DeleteMapping("/{softId}/image/{imageId}")
    @Operation(summary = "删除软件镜像", description = "删除指定的软件镜像")
    @OnRouterEvent("软件管理:删除软件镜像")
    public ReturnResult<Boolean> deleteImage(
            @Parameter(description = "软件ID", required = true) @PathVariable @NotNull Long softId,
            @Parameter(description = "服务器ID", required = true) @RequestParam @NotNull Long serverId,
            @Parameter(description = "镜像ID", required = true) @PathVariable @NotBlank String imageId) {

        log.info("删除软件镜像 - softId: {}, serverId: {}, imageId: {}", softId, serverId, imageId);

        try {
            boolean result = systemSoftService.deleteSoftImage(softId, serverId, imageId);
            if (result) {
                log.info("删除软件镜像成功 - softId: {}, serverId: {}, imageId: {}", softId, serverId, imageId);
                return ReturnResult.ok(true);
            } else {
                log.warn("删除软件镜像失败 - softId: {}, serverId: {}, imageId: {}", softId, serverId, imageId);
                return ReturnResult.error("删除软件镜像失败");
            }

        } catch (Exception e) {
            log.error("删除软件镜像失败 - softId: {}, serverId: {}, imageId: {}, error: {}",
                    softId, serverId, imageId, e.getMessage(), e);
            return ReturnResult.error("删除软件镜像失败: " + e.getMessage());
        }
    }


}