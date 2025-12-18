package com.chua.starter.monitor.controller.skywalking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSkywalkingConfig;
import com.chua.starter.monitor.service.skywalking.MonitorSkywalkingConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SkyWalking 配置管理控制器
 *
 * @author CH
 * @since 2024-12-18
 */
@RestController
@RequestMapping("/v1/skywalking/config")
@Api(tags = "SkyWalking 配置管理")
@Tag(name = "SkyWalking 配置管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSkywalkingConfigController {

    private final MonitorSkywalkingConfigService skywalkingConfigService;

    /**
     * 分页查询配置列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询配置列表")
    @ApiOperation("分页查询配置列表")
    public ReturnResult<Page<MonitorSkywalkingConfig>> page(
            @RequestParam(value = "page", defaultValue = "1") @Parameter(description = "页码") @ApiParam("页码") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "每页大小") @ApiParam("每页大小") Integer pageSize,
            @RequestParam(value = "name", required = false) @Parameter(description = "配置名称") @ApiParam("配置名称") String name,
            @RequestParam(value = "status", required = false) @Parameter(description = "状态") @ApiParam("状态") Integer status) {
        
        LambdaQueryWrapper<MonitorSkywalkingConfig> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(MonitorSkywalkingConfig::getSkywalkingConfigName, name);
        }
        if (status != null) {
            wrapper.eq(MonitorSkywalkingConfig::getSkywalkingConfigStatus, status);
        }
        wrapper.orderByDesc(MonitorSkywalkingConfig::getCreateTime);
        
        Page<MonitorSkywalkingConfig> result = skywalkingConfigService.page(new Page<>(page, pageSize), wrapper);
        return ReturnResult.ok(result);
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取配置详情")
    @ApiOperation("获取配置详情")
    public ReturnResult<MonitorSkywalkingConfig> getById(
            @PathVariable("id") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer id) {
        MonitorSkywalkingConfig config = skywalkingConfigService.getById(id);
        if (config == null) {
            return ReturnResult.illegal("配置不存在");
        }
        return ReturnResult.ok(config);
    }

    /**
     * 获取所有启用的配置
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的配置")
    @ApiOperation("获取所有启用的配置")
    public ReturnResult<List<MonitorSkywalkingConfig>> getEnabledConfigs() {
        return ReturnResult.ok(skywalkingConfigService.getEnabledConfigs());
    }

    /**
     * 获取所有配置列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有配置列表")
    @ApiOperation("获取所有配置列表")
    public ReturnResult<List<MonitorSkywalkingConfig>> list() {
        return ReturnResult.ok(skywalkingConfigService.list());
    }

    /**
     * 新增配置
     */
    @PostMapping("/save")
    @Operation(summary = "新增配置")
    @ApiOperation("新增配置")
    public ReturnResult<MonitorSkywalkingConfig> save(
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") MonitorSkywalkingConfig config) {
        log.info("新增 SkyWalking 配置: {}", config.getSkywalkingConfigName());
        boolean result = skywalkingConfigService.save(config);
        if (result) {
            return ReturnResult.ok(config);
        }
        return ReturnResult.illegal("保存失败");
    }

    /**
     * 更新配置
     */
    @PutMapping("/update")
    @Operation(summary = "更新配置")
    @ApiOperation("更新配置")
    public ReturnResult<Boolean> update(
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") MonitorSkywalkingConfig config) {
        log.info("更新 SkyWalking 配置: id={}", config.getSkywalkingConfigId());
        boolean result = skywalkingConfigService.updateById(config);
        return result ? ReturnResult.ok(true) : ReturnResult.illegal("更新失败");
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除配置")
    @ApiOperation("删除配置")
    public ReturnResult<Boolean> delete(
            @RequestParam("id") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer id) {
        log.info("删除 SkyWalking 配置: id={}", id);
        boolean result = skywalkingConfigService.removeById(id);
        return result ? ReturnResult.ok(true) : ReturnResult.illegal("删除失败");
    }

    /**
     * 测试连接
     */
    @PostMapping("/test")
    @Operation(summary = "测试连接")
    @ApiOperation("测试连接")
    public ReturnResult<Boolean> testConnection(
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") MonitorSkywalkingConfig config) {
        log.info("测试 SkyWalking 连接: host={}, port={}", config.getSkywalkingConfigHost(), config.getSkywalkingConfigPort());
        return skywalkingConfigService.testConnection(config);
    }

    /**
     * 测试已保存配置的连接
     */
    @PostMapping("/test/{id}")
    @Operation(summary = "测试已保存配置的连接")
    @ApiOperation("测试已保存配置的连接")
    public ReturnResult<Boolean> testConnectionById(
            @PathVariable("id") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer id) {
        log.info("测试 SkyWalking 连接: configId={}", id);
        return skywalkingConfigService.testConnection(id);
    }

    /**
     * 切换配置状态
     */
    @PutMapping("/toggle-status/{id}")
    @Operation(summary = "切换配置状态")
    @ApiOperation("切换配置状态")
    public ReturnResult<Boolean> toggleStatus(
            @PathVariable("id") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer id) {
        MonitorSkywalkingConfig config = skywalkingConfigService.getById(id);
        if (config == null) {
            return ReturnResult.illegal("配置不存在");
        }
        config.setSkywalkingConfigStatus(config.isEnabled() ? 0 : 1);
        boolean result = skywalkingConfigService.updateById(config);
        return result ? ReturnResult.ok(true) : ReturnResult.illegal("操作失败");
    }
}
