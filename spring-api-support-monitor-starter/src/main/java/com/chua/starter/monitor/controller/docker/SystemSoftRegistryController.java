package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRegistry;
import com.chua.starter.monitor.service.SystemSoftRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统软件镜像仓库管理控制器
 *
 * @author CH
 * @since 2025/01/15
 * @version 1.0.0
 */
@RestController
@RequestMapping("/v1/system/soft/registry")
@Tag(name = "软件仓库管理", description = "管理Docker镜像仓库配置")
@Slf4j
@RequiredArgsConstructor
@Validated
public class SystemSoftRegistryController {

    private final SystemSoftRegistryService systemSoftRegistryService;

    /**
     * 查询仓库列表
     *
     * @return 仓库列表
     */
    @GetMapping
    @Operation(summary = "查询仓库列表", description = "获取所有镜像仓库配置列表，默认仓库排在第一位")
    public ReturnResult<List<SystemSoftRegistry>> list() {

        try {
            log.debug("查询仓库列表(默认优先)");

            List<SystemSoftRegistry> list = systemSoftRegistryService.lambdaQuery()
                    .orderByDesc(SystemSoftRegistry::getSystemSoftRegistryStatus)
                    .orderByAsc(SystemSoftRegistry::getSystemSoftRegistrySort)
                    .list();
            log.debug("查询仓库列表成功, count: {}", list.size());
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("查询仓库列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询仓库列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页仓库列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询仓库列表", description = "分页获取镜像仓库配置列表，默认仓库排在第一位")
    public ReturnResult<IPage<SystemSoftRegistry>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        try {
            log.debug("分页查询仓库列表(默认优先), page: {}, size: {}", page, size);

            IPage<SystemSoftRegistry> pageResult = systemSoftRegistryService.lambdaQuery()
                    .orderByDesc(SystemSoftRegistry::getSystemSoftRegistryStatus)
                    .orderByAsc(SystemSoftRegistry::getSystemSoftRegistrySort)
                    .page(new Page<>(page, size));
            log.debug("分页查询仓库列表成功, count: {}", pageResult.getRecords().size());
            return ReturnResult.ok(pageResult);

        } catch (Exception e) {
            log.error("分页查询仓库列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询仓库详情
     *
     * @param id 仓库ID
     * @return 仓库详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询仓库详情", description = "根据ID查询仓库详细信息")
    public ReturnResult<SystemSoftRegistry> getById(
            @Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("查询仓库详情, id: {}", id);

            SystemSoftRegistry registry = systemSoftRegistryService.getById(id);
            if (registry == null) {
                return ReturnResult.error("仓库不存在");
            }

            return ReturnResult.ok(registry);

        } catch (Exception e) {
            log.error("查询仓库详情失败, id: {}", id, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增仓库
     *
     * @param registry 仓库信息
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增仓库", description = "添加新的Docker镜像仓库配置")
    public ReturnResult<SystemSoftRegistry> create(@Valid @RequestBody SystemSoftRegistry registry) {

        try {
            log.debug("新增仓库: {}", registry.getSystemSoftRegistryName());

            // 验证配置
            ReturnResult<String> validateResult = systemSoftRegistryService.validateRegistryConfig(registry);
            if (!validateResult.isOk()) {
                return ReturnResult.error(validateResult.getMsg());
            }

            // 保存仓库
            boolean saved = systemSoftRegistryService.save(registry);
            if (saved) {
                log.info("新增仓库成功, id: {}, name: {}", registry.getSystemSoftRegistryId(), registry.getSystemSoftRegistryName());
                return ReturnResult.ok(registry, "新增成功");
            } else {
                return ReturnResult.error("新增失败");
            }

        } catch (Exception e) {
            log.error("新增仓库失败", e);
            return ReturnResult.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新仓库
     *
     * @param id       仓库ID
     * @param registry 仓库信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新仓库", description = "更新Docker镜像仓库配置")
    public ReturnResult<SystemSoftRegistry> update(
            @Parameter(description = "仓库ID") @PathVariable @NotNull Integer id,
            @Valid @RequestBody SystemSoftRegistry registry) {

        try {
            log.debug("更新仓库, id: {}", id);

            registry.setSystemSoftRegistryId(id);

            // 验证配置
            ReturnResult<String> validateResult = systemSoftRegistryService.validateRegistryConfig(registry);
            if (!validateResult.isOk()) {
                return ReturnResult.error(validateResult.getMsg());
            }

            // 更新仓库
            boolean updated = systemSoftRegistryService.updateById(registry);
            if (updated) {
                log.info("更新仓库成功, id: {}, name: {}", id, registry.getSystemSoftRegistryName());
                return ReturnResult.ok(registry, "更新成功");
            } else {
                return ReturnResult.error("更新失败，仓库不存在");
            }

        } catch (Exception e) {
            log.error("更新仓库失败, id: {}", id, e);
            return ReturnResult.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除仓库
     *
     * @param id 仓库ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除仓库", description = "删除Docker镜像仓库配置")
    public ReturnResult<Boolean> delete(@Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("删除仓库, id: {}", id);

            boolean removed = systemSoftRegistryService.removeById(id);
            if (removed) {
                log.info("删除仓库成功, id: {}", id);
                return ReturnResult.ok(true, "删除成功");
            } else {
                return ReturnResult.error("删除失败，仓库不存在");
            }

        } catch (Exception e) {
            log.error("删除仓库失败, id: {}", id, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除仓库
     *
     * @param ids 仓库ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除仓库", description = "批量删除多个Docker镜像仓库配置")
    public ReturnResult<Boolean> batchDelete(@RequestBody List<Integer> ids) {

        try {
            log.debug("批量删除仓库, ids: {}", ids);

            if (ids == null || ids.isEmpty()) {
                return ReturnResult.error("请选择要删除的仓库");
            }

            boolean removed = systemSoftRegistryService.removeByIds(ids);
            if (removed) {
                log.info("批量删除仓库成功, count: {}", ids.size());
                return ReturnResult.ok(true, "批量删除成功");
            } else {
                return ReturnResult.error("批量删除失败");
            }

        } catch (Exception e) {
            log.error("批量删除仓库失败, ids: {}", ids, e);
            return ReturnResult.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 测试仓库连接
     *
     * @param id 仓库ID
     * @return 连接测试结果
     */
    @PostMapping("/{id}/test")
    @Operation(summary = "测试仓库连接", description = "测试Docker镜像仓库连接是否正常")
    public ReturnResult<Boolean> testConnection(@Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("测试仓库连接, id: {}", id);

            ReturnResult<Boolean> result = systemSoftRegistryService.testConnectionForRegistry(id);
            log.info("仓库连接测试完成, id: {}, result: {}", id, result.isOk());
            return result;

        } catch (Exception e) {
            log.error("测试仓库连接失败, id: {}", id, e);
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 激活仓库
     *
     * @param id 仓库ID
     * @return 激活结果
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "激活仓库", description = "激活指定仓库，激活的仓库用于软件搜索")
    public ReturnResult<Boolean> activate(@Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("激活仓库, id: {}", id);

            boolean updated = systemSoftRegistryService.lambdaUpdate()
                    .eq(SystemSoftRegistry::getSystemSoftRegistryId, id)
                    .set(SystemSoftRegistry::getSystemSoftRegistryActive, 1)
                    .update();

            if (updated) {
                log.info("激活仓库成功, id: {}", id);
                return ReturnResult.ok(true, "激活成功");
            } else {
                return ReturnResult.error("激活失败，仓库不存在");
            }

        } catch (Exception e) {
            log.error("激活仓库失败, id: {}", id, e);
            return ReturnResult.error("激活失败: " + e.getMessage());
        }
    }

    /**
     * 取消激活仓库
     *
     * @param id 仓库ID
     * @return 取消激活结果
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "取消激活仓库", description = "取消激活指定仓库")
    public ReturnResult<Boolean> deactivate(@Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("取消激活仓库, id: {}", id);

            boolean updated = systemSoftRegistryService.lambdaUpdate()
                    .eq(SystemSoftRegistry::getSystemSoftRegistryId, id)
                    .set(SystemSoftRegistry::getSystemSoftRegistryActive, 0)
                    .update();

            if (updated) {
                log.info("取消激活仓库成功, id: {}", id);
                return ReturnResult.ok(true, "已取消激活");
            } else {
                return ReturnResult.error("取消激活失败，仓库不存在");
            }

        } catch (Exception e) {
            log.error("取消激活仓库失败, id: {}", id, e);
            return ReturnResult.error("取消激活失败: " + e.getMessage());
        }
    }

    /**
     * 获取激活的仓库列表
     *
     * @return 激活的仓库列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取激活的仓库", description = "获取所有激活状态的镜像仓库列表，用于软件搜索")
    public ReturnResult<List<SystemSoftRegistry>> getActive() {

        try {
            log.debug("获取激活的仓库");

            List<SystemSoftRegistry> list = systemSoftRegistryService.lambdaQuery()
                    .eq(SystemSoftRegistry::getSystemSoftRegistryActive, 1)
                    .eq(SystemSoftRegistry::getSystemSoftRegistryStatus, 1)
                    .list();

            log.debug("获取激活仓库完成, count: {}", list.size());
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("获取激活仓库失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 设置默认仓库
     *
     * @param id 仓库ID
     * @return 设置结果
     */
    @PostMapping("/{id}/default")
    @Operation(summary = "设置默认仓库", description = "将指定仓库设置为默认镜像仓库")
    public ReturnResult<Boolean> setDefault(@Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("设置默认仓库, id: {}", id);

            ReturnResult<Boolean> result = systemSoftRegistryService.setDefaultForRegistry(id);
            log.info("设置默认仓库完成, id: {}, result: {}", id, result.isOk());
            return result;

        } catch (Exception e) {
            log.error("设置默认仓库失败, id: {}", id, e);
            return ReturnResult.error("设置失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新仓库状态
     *
     * @param ids    仓库ID列表
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新仓库状态", description = "批量启用或禁用多个仓库")
    public ReturnResult<Boolean> batchUpdateStatus(
            @Parameter(description = "仓库ID列表") @RequestParam List<Integer> ids,
            @Parameter(description = "新状态，0=禁用，1=启用") @RequestParam Integer status) {

        try {
            log.debug("批量更新仓库状态, ids: {}, status: {}", ids, status);

            ReturnResult<Boolean> result = systemSoftRegistryService.batchUpdateStatus(ids, status);
            log.info("批量更新仓库状态完成, count: {}, status: {}, result: {}", ids.size(), status, result.isOk());
            return result;

        } catch (Exception e) {
            log.error("批量更新仓库状态失败, ids: {}, status: {}", ids, status, e);
            return ReturnResult.error("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认仓库
     *
     * @return 默认仓库信息
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认仓库", description = "获取当前设置的默认镜像仓库；当提供 serverId 时，返回该服务器的默认仓库")
    public ReturnResult<SystemSoftRegistry> getDefault(@RequestParam(value = "serverId", required = false) Integer serverId) {

        try {
            log.debug("获取默认仓库, serverId: {}", serverId);

            ReturnResult<SystemSoftRegistry> result = (serverId == null)
                    ? systemSoftRegistryService.getDefaultRegistry()
                    : systemSoftRegistryService.getDefaultRegistryByServerId(serverId);
            log.debug("获取默认仓库完成, result: {}", result.isOk());
            return result;

        } catch (Exception e) {
            log.error("获取默认仓库失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的仓库
     *
     * @return 启用的仓库列表
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取启用的仓库", description = "获取所有启用状态的镜像仓库列表")
    public ReturnResult<List<SystemSoftRegistry>> getEnabled() {

        try {
            log.debug("获取启用的仓库");

            ReturnResult<List<SystemSoftRegistry>> result = systemSoftRegistryService.getEnabledRegistries();
            log.debug("获取启用仓库完成, count: {}", result.getData() != null ? result.getData().size() : 0);
            return result;

        } catch (Exception e) {
            log.error("获取启用仓库失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取仓库列表
     *
     * @param type 仓库类型
     * @return 指定类型的仓库列表
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取仓库", description = "获取指定类型的镜像仓库列表")
    public ReturnResult<List<SystemSoftRegistry>> getByType(
            @Parameter(description = "仓库类型") @PathVariable String type) {

        try {
            log.debug("根据类型获取仓库, type: {}", type);

            ReturnResult<List<SystemSoftRegistry>> result = systemSoftRegistryService.getRegistriesByType(type);
            log.debug("根据类型获取仓库完成, type: {}, count: {}", type,
                    result.getData() != null ? result.getData().size() : 0);
            return result;

        } catch (Exception e) {
            log.error("根据类型获取仓库失败, type: {}", type, e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 同步仓库信息
     *
     * @param id 仓库ID
     * @return 同步结果
     */
    @PostMapping("/{id}/sync")
    @Operation(summary = "同步仓库信息", description = "从远程仓库同步最新的配置信息")
    public ReturnResult<Boolean> sync(@Parameter(description = "仓库ID") @PathVariable @NotNull Integer id) {

        try {
            log.debug("同步仓库信息, id: {}", id);

            ReturnResult<Boolean> result = systemSoftRegistryService.syncRegistryForRegistry(id);
            log.info("同步仓库信息完成, id: {}, result: {}", id, result.isOk());
            return result;

        } catch (Exception e) {
            log.error("同步仓库信息失败, id: {}", id, e);
            return ReturnResult.error("同步失败: " + e.getMessage());
        }
    }
}
