package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.monitor.entity.MsMonitor;
import com.chua.starter.monitor.entity.MsMonitorConfig;
import com.chua.starter.monitor.entity.MsMonitorGateway;
import com.chua.starter.monitor.entity.MsMonitorMetricConfig;
import com.chua.starter.monitor.entity.MsMonitorTask;
import com.chua.starter.monitor.service.MsMonitorConfigService;
import com.chua.starter.monitor.service.MsMonitorGatewayService;
import com.chua.starter.monitor.service.MsMonitorMetricConfigService;
import com.chua.starter.monitor.service.MsMonitorService;
import com.chua.starter.monitor.service.MsMonitorTaskService;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 监控模块统一接口，涵盖监控资产、采集任务、指标解析规则、网关、全局配置、
 * Redis 操作与指标查询等全部端点。
 *
 * @author CH
 * @since 2026-08-11
 */
@Slf4j
@RestController
@RequestMapping("/ms/monitor")
@Api(tags = "监控模块管理")
@Tag(name = "监控模块管理")
@RequiredArgsConstructor
public class MsMonitorController {

    /**
     * 监控资产服务
     */
    private final MsMonitorService monitorService;

    /**
     * 采集任务服务
     */
    private final MsMonitorTaskService taskService;

    /**
     * 指标解析规则服务
     */
    private final MsMonitorMetricConfigService metricConfigService;

    /**
     * 监控网关服务
     */
    private final MsMonitorGatewayService gatewayService;

    /**
     * 全局配置服务
     */
    private final MsMonitorConfigService configService;

    /**
     * Redis 模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 监控资产 CRUD ====================

    /**
     * 监控资产列表查询（分页）。
     *
     * @param page     页码（从1开始，可选，默认1）
     * @param pageSize 每页大小（可选，默认100）
     * @param entity   查询条件
     * @return 监控资产分页列表
     */
    @GetMapping
    @ApiOperation("监控资产列表")
    @Operation(summary = "监控资产列表")
    public ReturnPageResult<MsMonitor> list(
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            MsMonitor entity) {
        var query = Wrappers.<MsMonitor>lambdaQuery()
                .like(StringUtils.isNotEmpty(entity.getMonitorName()),
                        MsMonitor::getMonitorName, entity.getMonitorName())
                .eq(StringUtils.isNotEmpty(entity.getServiceType()),
                        MsMonitor::getServiceType, entity.getServiceType())
                .eq(StringUtils.isNotEmpty(entity.getServerType()),
                        MsMonitor::getServerType, entity.getServerType())
                .eq(StringUtils.isNotEmpty(entity.getMonitorGroup()),
                        MsMonitor::getMonitorGroup, entity.getMonitorGroup())
                .eq(entity.getMonitorEnabled() != null,
                        MsMonitor::getMonitorEnabled, entity.getMonitorEnabled())
                .orderByDesc(MsMonitor::getId);
        return PageResultUtils.ok(monitorService.page(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(page, pageSize), query));
    }

    /**
     * 获取监控资产详情。
     *
     * @param id 监控资产ID
     * @return 监控资产详情
     */
    @GetMapping("/{id}")
    @ApiOperation("监控资产详情")
    @Operation(summary = "监控资产详情")
    public ReturnResult<MsMonitor> detail(@ApiParam("监控资产ID") @PathVariable Long id) {
        return ReturnResult.ok(monitorService.getById(id));
    }

    /**
     * 新增监控资产。
     *
     * @param entity 监控资产实体
     * @return 是否新增成功
     */
    @PostMapping
    @ApiOperation("新增监控资产")
    @Operation(summary = "新增监控资产")
    public ReturnResult<Boolean> save(@RequestBody MsMonitor entity) {
        monitorService.save(entity);
        return ReturnResult.ok(true);
    }

    /**
     * 更新监控资产。
     *
     * @param entity 监控资产实体
     * @return 是否更新成功
     */
    @PutMapping("/{id}")
    @ApiOperation("更新监控资产")
    @Operation(summary = "更新监控资产")
    public ReturnResult<Boolean> update(@PathVariable Long id, @RequestBody MsMonitor entity) {
        entity.setId(id);
        return ReturnResult.ok(monitorService.updateById(entity));
    }

    /**
     * 删除监控资产。
     *
     * @param id 监控资产ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除监控资产")
    @Operation(summary = "删除监控资产")
    public ReturnResult<Boolean> remove(@ApiParam("监控资产ID") @PathVariable Long id) {
        return ReturnResult.ok(monitorService.removeById(id));
    }

    // ==================== 采集任务 CRUD ====================

    /**
     * 采集任务列表查询（支持按监控资产ID筛选）。
     *
     * @param page      页码（从1开始，可选，默认1）
     * @param pageSize  每页大小（可选，默认100）
     * @param entity    查询条件
     * @param monitorId 监控资产ID（可选）
     * @return 采集任务分页列表
     */
    @GetMapping("/task")
    @ApiOperation("采集任务列表")
    @Operation(summary = "采集任务列表")
    public ReturnPageResult<MsMonitorTask> listTask(
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            MsMonitorTask entity,
            @ApiParam("监控资产ID") @RequestParam(required = false) Long monitorId) {
        var query = Wrappers.<MsMonitorTask>lambdaQuery()
                .like(StringUtils.isNotEmpty(entity.getTaskName()),
                        MsMonitorTask::getTaskName, entity.getTaskName())
                .eq(monitorId != null,
                        MsMonitorTask::getMonitorId, monitorId)
                .eq(StringUtils.isNotEmpty(entity.getTaskType()),
                        MsMonitorTask::getTaskType, entity.getTaskType())
                .eq(entity.getTaskEnabled() != null,
                        MsMonitorTask::getTaskEnabled, entity.getTaskEnabled())
                .eq(StringUtils.isNotEmpty(entity.getTaskStatus()),
                        MsMonitorTask::getTaskStatus, entity.getTaskStatus())
                .orderByDesc(MsMonitorTask::getId);
        return PageResultUtils.ok(taskService.page(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(page, pageSize), query));
    }

    /**
     * 获取采集任务详情。
     *
     * @param id 任务ID
     * @return 采集任务详情
     */
    @GetMapping("/task/{id}")
    @ApiOperation("采集任务详情")
    @Operation(summary = "采集任务详情")
    public ReturnResult<MsMonitorTask> detailTask(@ApiParam("任务ID") @PathVariable Long id) {
        return ReturnResult.ok(taskService.getById(id));
    }

    /**
     * 新增采集任务。
     *
     * @param entity 采集任务实体
     * @return 是否新增成功
     */
    @PostMapping("/task")
    @ApiOperation("新增采集任务")
    @Operation(summary = "新增采集任务")
    public ReturnResult<Boolean> saveTask(@RequestBody MsMonitorTask entity) {
        taskService.save(entity);
        return ReturnResult.ok(true);
    }

    /**
     * 更新采集任务。
     *
     * @param id     任务ID
     * @param entity 采集任务实体
     * @return 是否更新成功
     */
    @PutMapping("/task/{id}")
    @ApiOperation("更新采集任务")
    @Operation(summary = "更新采集任务")
    public ReturnResult<Boolean> updateTask(@PathVariable Long id,
                                            @RequestBody MsMonitorTask entity) {
        entity.setId(id);
        return ReturnResult.ok(taskService.updateById(entity));
    }

    /**
     * 删除采集任务。
     *
     * @param id 任务ID
     * @return 是否删除成功
     */
    @DeleteMapping("/task/{id}")
    @ApiOperation("删除采集任务")
    @Operation(summary = "删除采集任务")
    public ReturnResult<Boolean> removeTask(@ApiParam("任务ID") @PathVariable Long id) {
        return ReturnResult.ok(taskService.removeById(id));
    }

    /**
     * 启用或停用采集任务。
     *
     * @param id     任务ID
     * @param enabled 启用状态（1启用/0停用）
     * @return 是否更新成功
     */
    @PutMapping("/task/{id}/enable")
    @ApiOperation("启用/停用采集任务")
    @Operation(summary = "启用/停用采集任务")
    public ReturnResult<Boolean> enableTask(@ApiParam("任务ID") @PathVariable Long id,
                                            @ApiParam("启用状态（1启用/0停用）")
                                            @RequestParam Integer enabled) {
        return ReturnResult.ok(taskService.updateTaskEnabled(id, enabled));
    }

    // ==================== 指标解析规则 CRUD ====================

    /**
     * 指标解析规则列表查询（支持按任务ID筛选）。
     *
     * @param page     页码（从1开始，可选，默认1）
     * @param pageSize 每页大小（可选，默认100）
     * @param entity   查询条件
     * @param taskId   任务ID（可选）
     * @return 解析规则分页列表
     */
    @GetMapping("/metric-config")
    @ApiOperation("指标解析规则列表")
    @Operation(summary = "指标解析规则列表")
    public ReturnPageResult<MsMonitorMetricConfig> listMetricConfig(
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            MsMonitorMetricConfig entity,
            @ApiParam("任务ID") @RequestParam(required = false) Long taskId) {
        var query = Wrappers.<MsMonitorMetricConfig>lambdaQuery()
                .like(StringUtils.isNotEmpty(entity.getMetricName()),
                        MsMonitorMetricConfig::getMetricName, entity.getMetricName())
                .eq(taskId != null,
                        MsMonitorMetricConfig::getTaskId, taskId)
                .eq(StringUtils.isNotEmpty(entity.getMetricType()),
                        MsMonitorMetricConfig::getMetricType, entity.getMetricType())
                .orderByDesc(MsMonitorMetricConfig::getId);
        return PageResultUtils.ok(metricConfigService.page(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(page, pageSize), query));
    }

    /**
     * 新增指标解析规则。
     *
     * @param entity 解析规则实体
     * @return 是否新增成功
     */
    @PostMapping("/metric-config")
    @ApiOperation("新增指标解析规则")
    @Operation(summary = "新增指标解析规则")
    public ReturnResult<Boolean> saveMetricConfig(@RequestBody MsMonitorMetricConfig entity) {
        metricConfigService.save(entity);
        return ReturnResult.ok(true);
    }

    /**
     * 更新指标解析规则。
     *
     * @param id     规则ID
     * @param entity 解析规则实体
     * @return 是否更新成功
     */
    @PutMapping("/metric-config/{id}")
    @ApiOperation("更新指标解析规则")
    @Operation(summary = "更新指标解析规则")
    public ReturnResult<Boolean> updateMetricConfig(@PathVariable Long id,
                                                    @RequestBody MsMonitorMetricConfig entity) {
        entity.setId(id);
        return ReturnResult.ok(metricConfigService.updateById(entity));
    }

    /**
     * 删除指标解析规则。
     *
     * @param id 规则ID
     * @return 是否删除成功
     */
    @DeleteMapping("/metric-config/{id}")
    @ApiOperation("删除指标解析规则")
    @Operation(summary = "删除指标解析规则")
    public ReturnResult<Boolean> removeMetricConfig(@ApiParam("规则ID") @PathVariable Long id) {
        return ReturnResult.ok(metricConfigService.removeById(id));
    }

    // ==================== 监控网关 CRUD ====================

    /**
     * 监控网关列表查询（支持按监控资产ID筛选）。
     *
     * @param page      页码（从1开始，可选，默认1）
     * @param pageSize  每页大小（可选，默认100）
     * @param entity    查询条件
     * @param monitorId 监控资产ID（可选）
     * @return 网关分页列表
     */
    @GetMapping("/gateway")
    @ApiOperation("监控网关列表")
    @Operation(summary = "监控网关列表")
    public ReturnPageResult<MsMonitorGateway> listGateway(
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            MsMonitorGateway entity,
            @ApiParam("监控资产ID") @RequestParam(required = false) Long monitorId) {
        var query = Wrappers.<MsMonitorGateway>lambdaQuery()
                .like(StringUtils.isNotEmpty(entity.getGatewayName()),
                        MsMonitorGateway::getGatewayName, entity.getGatewayName())
                .eq(monitorId != null,
                        MsMonitorGateway::getMonitorId, monitorId)
                .eq(StringUtils.isNotEmpty(entity.getGatewayStatus()),
                        MsMonitorGateway::getGatewayStatus, entity.getGatewayStatus())
                .orderByDesc(MsMonitorGateway::getId);
        return PageResultUtils.ok(gatewayService.page(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(page, pageSize), query));
    }

    /**
     * 新增监控网关。
     *
     * @param entity 网关实体
     * @return 是否新增成功
     */
    @PostMapping("/gateway")
    @ApiOperation("新增监控网关")
    @Operation(summary = "新增监控网关")
    public ReturnResult<Boolean> saveGateway(@RequestBody MsMonitorGateway entity) {
        gatewayService.save(entity);
        return ReturnResult.ok(true);
    }

    /**
     * 删除监控网关。
     *
     * @param id 网关ID
     * @return 是否删除成功
     */
    @DeleteMapping("/gateway/{id}")
    @ApiOperation("删除监控网关")
    @Operation(summary = "删除监控网关")
    public ReturnResult<Boolean> removeGateway(@ApiParam("网关ID") @PathVariable Long id) {
        return ReturnResult.ok(gatewayService.removeById(id));
    }

    // ==================== 全局配置 ====================

    /**
     * 读取全部配置项，返回键值对Map。
     *
     * @return 配置键值对
     */
    @GetMapping("/config")
    @ApiOperation("读取全局配置")
    @Operation(summary = "读取全局配置")
    public ReturnResult<Map<String, String>> getConfig() {
        return ReturnResult.ok(configService.getAllConfigMap());
    }

    /**
     * 批量保存或更新配置项。
     *
     * @param configs 配置列表，包含键值对、类型、分组
     * @return 是否保存成功
     */
    @PostMapping("/config")
    @ApiOperation("批量保存全局配置")
    @Operation(summary = "批量保存全局配置")
    public ReturnResult<Boolean> saveConfig(@RequestBody List<MsMonitorConfig> configs) {
        configService.saveBatch(configs);
        return ReturnResult.ok(true);
    }

    // ==================== Redis 操作 ====================

    /**
     * 扫描Redis中符合模式的key。
     *
     * @param taskId 任务ID
     * @param pattern key匹配模式，例如 *
     * @return key列表
     */
    @GetMapping("/operation/redis/{taskId}/keys")
    @ApiOperation("扫描Redis key")
    @Operation(summary = "扫描Redis key")
    public ReturnResult<Map<String, List<String>>> scanRedisKeys(
            @ApiParam("任务ID") @PathVariable Long taskId,
            @ApiParam("匹配模式") @RequestParam(defaultValue = "*") String pattern) {
        try {
            Set<Object> keys = redisTemplate.keys(pattern);
            var keyList = new ArrayList<String>();
            if (keys != null) {
                for (Object key : keys) {
                    keyList.add(key.toString());
                }
            }
            var result = new LinkedHashMap<String, List<String>>();
            result.put("keys", keyList);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("扫描Redis key失败，taskId={}, pattern={}", taskId, pattern, e);
            return ReturnResult.illegal("扫描Redis key失败：" + e.getMessage());
        }
    }

    /**
     * 获取Redis中指定key的值。
     *
     * @param taskId 任务ID
     * @param key    Redis key
     * @return key对应的值
     */
    @GetMapping("/operation/redis/{taskId}/kv/{key}")
    @ApiOperation("获取Redis值")
    @Operation(summary = "获取Redis值")
    public ReturnResult<Object> getRedisValue(
            @ApiParam("任务ID") @PathVariable Long taskId,
            @ApiParam("Redis key") @PathVariable String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return ReturnResult.ok(value);
        } catch (Exception e) {
            log.error("获取Redis值失败，taskId={}, key={}", taskId, key, e);
            return ReturnResult.illegal("获取Redis值失败：" + e.getMessage());
        }
    }

    /**
     * 设置Redis中指定key的值，可选择性设置过期时间。
     *
     * @param taskId 任务ID
     * @param key    Redis key
     * @param value  值
     * @param ttl    过期时间（秒），可选，0或不传表示不设置过期
     * @return 是否设置成功
     */
    @PutMapping("/operation/redis/{taskId}/kv/{key}")
    @ApiOperation("设置Redis值")
    @Operation(summary = "设置Redis值")
    public ReturnResult<Boolean> setRedisValue(
            @ApiParam("任务ID") @PathVariable Long taskId,
            @ApiParam("Redis key") @PathVariable String key,
            @ApiParam("值") @RequestBody String value,
            @ApiParam("过期时间（秒）") @RequestParam(defaultValue = "0") Long ttl) {
        try {
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("设置Redis值失败，taskId={}, key={}", taskId, key, e);
            return ReturnResult.illegal("设置Redis值失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除Redis中的key。
     *
     * @param taskId 任务ID
     * @param keys   待删除的key列表
     * @return 是否删除成功
     */
    @DeleteMapping("/operation/redis/{taskId}/keys")
    @ApiOperation("批量删除Redis key")
    @Operation(summary = "批量删除Redis key")
    public ReturnResult<Boolean> deleteRedisKeys(
            @ApiParam("任务ID") @PathVariable Long taskId,
            @ApiParam("待删除的key列表") @RequestParam List<String> keys) {
        try {
            if (keys == null || keys.isEmpty()) {
                return ReturnResult.illegal("key列表不能为空");
            }
            redisTemplate.delete(keys);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("批量删除Redis key失败，taskId={}", taskId, e);
            return ReturnResult.illegal("批量删除Redis key失败：" + e.getMessage());
        }
    }

    /**
     * 设置Redis中指定key的过期时间。
     *
     * @param taskId     任务ID
     * @param key        Redis key
     * @param ttlSeconds 过期时间（秒）
     * @return 是否设置成功
     */
    @PostMapping("/operation/redis/{taskId}/expire/{key}")
    @ApiOperation("设置Redis key过期时间")
    @Operation(summary = "设置Redis key过期时间")
    public ReturnResult<Boolean> setRedisExpire(
            @ApiParam("任务ID") @PathVariable Long taskId,
            @ApiParam("Redis key") @PathVariable String key,
            @ApiParam("过期时间（秒）") @RequestParam Long ttlSeconds) {
        try {
            Boolean result = redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
            return ReturnResult.ok(result != null && result);
        } catch (Exception e) {
            log.error("设置Redis过期时间失败，taskId={}, key={}", taskId, key, e);
            return ReturnResult.illegal("设置Redis过期时间失败：" + e.getMessage());
        }
    }

    // ==================== 指标查询 ====================

    /**
     * 查询最新指标数据（示例数据）。
     *
     * @param type      指标类型（cpu/memory/disk/network/jvm/redis）
     * @param monitorId 监控资产ID
     * @return 最新指标数据
     */
    @GetMapping("/metric/{type}/{monitorId}")
    @ApiOperation("查询最新指标")
    @Operation(summary = "查询最新指标")
    public ReturnResult<Map<String, Object>> getLatestMetric(
            @ApiParam("指标类型") @PathVariable String type,
            @ApiParam("监控资产ID") @PathVariable Long monitorId) {
        var result = new LinkedHashMap<String, Object>();
        result.put("monitorId", monitorId);
        result.put("type", type);
        result.putAll(generateMockMetric(type));
        return ReturnResult.ok(result);
    }

    /**
     * 查询历史指标数据（示例数据）。
     *
     * @param type      指标类型（cpu/memory/disk/network/jvm/redis）
     * @param monitorId 监控资产ID
     * @param hours     查询时间范围（小时）
     * @return 历史指标数据列表
     */
    @GetMapping("/metric/{type}/history/{monitorId}")
    @ApiOperation("查询历史指标")
    @Operation(summary = "查询历史指标")
    public ReturnResult<List<Map<String, Object>>> getHistoryMetric(
            @ApiParam("指标类型") @PathVariable String type,
            @ApiParam("监控资产ID") @PathVariable Long monitorId,
            @ApiParam("查询时间范围（小时）") @RequestParam(defaultValue = "1") Integer hours) {
        var history = new ArrayList<Map<String, Object>>();
        int sampleCount = hours * 4;
        for (int i = 0; i < sampleCount; i++) {
            var item = new LinkedHashMap<String, Object>();
            item.put("timestamp", System.currentTimeMillis()
                    - (long) (sampleCount - i) * 15 * 60 * 1000L);
            item.put("monitorId", monitorId);
            item.put("type", type);
            item.putAll(generateMockMetric(type));
            history.add(item);
        }
        return ReturnResult.ok(history);
    }

    /**
     * 生成模拟指标数据，后续可替换为真实采集器数据。
     *
     * @param type 指标类型
     * @return 模拟指标Map
     */
    private Map<String, Object> generateMockMetric(String type) {
        var metric = new LinkedHashMap<String, Object>();
        double random = Math.random();
        switch (type) {
            case "cpu":
                metric.put("cpuUsage", Math.round(random * 100 * 100.0) / 100.0);
                metric.put("load1", Math.round(random * 5 * 100.0) / 100.0);
                metric.put("load5", Math.round(random * 5 * 100.0) / 100.0);
                metric.put("load15", Math.round(random * 5 * 100.0) / 100.0);
                break;
            case "memory":
                metric.put("total", 16384L);
                metric.put("used", Math.round(16384 * random * 1000.0) / 1000.0);
                metric.put("free", Math.round(16384 * (1 - random) * 1000.0) / 1000.0);
                metric.put("usage", Math.round(random * 100 * 100.0) / 100.0);
                break;
            case "disk":
                metric.put("total", 512000L);
                metric.put("used", Math.round(512000 * random * 1000.0) / 1000.0);
                metric.put("free", Math.round(512000 * (1 - random) * 1000.0) / 1000.0);
                metric.put("usage", Math.round(random * 100 * 100.0) / 100.0);
                break;
            case "network":
                metric.put("rxBytes", Math.round(random * 1000000 * 100.0) / 100.0);
                metric.put("txBytes", Math.round(random * 500000 * 100.0) / 100.0);
                metric.put("rxPackets", Math.round(random * 50000));
                metric.put("txPackets", Math.round(random * 30000));
                break;
            case "jvm":
                metric.put("heapUsed", Math.round(random * 1024 * 1000.0) / 1000.0);
                metric.put("heapMax", 1024L);
                metric.put("threads", (int) (random * 200));
                metric.put("gcCount", (int) (random * 100));
                break;
            case "redis":
                metric.put("connectedClients", (int) (random * 100));
                metric.put("usedMemory", Math.round(random * 512 * 1000.0) / 1000.0);
                metric.put("keySpaceHits", (int) (random * 10000));
                metric.put("keySpaceMisses", (int) (random * 1000));
                metric.put("opsPerSec", Math.round(random * 5000 * 100.0) / 100.0);
                break;
            default:
                metric.put("error", "未知指标类型：" + type);
                break;
        }
        return metric;
    }
}
