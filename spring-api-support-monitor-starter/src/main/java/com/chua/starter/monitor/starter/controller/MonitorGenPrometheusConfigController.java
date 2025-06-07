package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenPrometheusConfig;
import com.chua.starter.monitor.service.MonitorSysGenPrometheusConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * prometheus配置管理
 *
 * @author CH
 * @since 2025/6/6 16:44
 */
@RestController
@SuppressWarnings("ALL")
@Tag(name = "promethus接口")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v2/prometheus/config")
public class MonitorGenPrometheusConfigController {

    final MonitorSysGenPrometheusConfigService monitorSysGenPrometheusConfigService;


    /**
     * 分享配置
     *
     * @return 分享结果
     */
    @GetMapping("/share")
    @Operation(summary = "分享配置")
    public ReturnResult<List<MonitorSysGenPrometheusConfig>> sharePrometheusConfig() {
        return monitorSysGenPrometheusConfigService.sharePrometheusConfig();
    }

    /**
     * 获取配置
     *
     * @param genId genId
     * @return 配置
     */
    @GetMapping("/list")
    @Operation(summary = "获取配置")
    public ReturnResult<List<MonitorSysGenPrometheusConfig>> listPrometheusConfig(Integer genId, String monitorSysGenPrometheusConfigType) {
        return ReturnResult.success(monitorSysGenPrometheusConfigService.listPrometheusConfig(genId, monitorSysGenPrometheusConfigType));
    }

    /**
     * 添加配置
     *
     * @param monitorSysGenPrometheusConfig 配置
     * @return 添加结果
     */
    @PostMapping("/save")
    @Operation(summary = "添加配置")
    public ReturnResult<Boolean> savePrometheusConfig(@RequestBody MonitorSysGenPrometheusConfig monitorSysGenPrometheusConfig) {
        return monitorSysGenPrometheusConfigService.savePrometheusConfig(monitorSysGenPrometheusConfig);
    }

    /**
     * 更新配置
     *
     * @param monitorSysGenPromethusConfig 配置
     * @return 更新结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新配置")
    public ReturnResult<Boolean> updatePrometheusConfig(@RequestBody MonitorSysGenPrometheusConfig monitorSysGenPrometheusConfig) {
        return monitorSysGenPrometheusConfigService.updatePrometheusConfig(monitorSysGenPrometheusConfig);
    }

    /**
     * 删除配置
     *
     * @param id id
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除配置")
    public ReturnResult<Boolean> deletePrometheusConfig(Integer id) {
        return monitorSysGenPrometheusConfigService.deletePrometheusConfig(id);
    }


    /**
     * 分享配置
     *
     * @param id id
     * @return 分享结果
     */
    @GetMapping("/share/{id}")
    @Operation(summary = "分享配置")
    public ReturnResult<Boolean> sharePrometheusConfig(@PathVariable Integer id) {
        return monitorSysGenPrometheusConfigService.sharePrometheusConfig(id);
    }
}
