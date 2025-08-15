package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.monitor.service.MonitorSysGenAlertPushConfigService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警推送配置管理
 */
@RestController
@RequestMapping("/v1/gen/server/alert/push-config")
@Tag(name = "告警推送配置管理")
@RequiredArgsConstructor
public class MonitorSysGenAlertPushConfigController {

    private final MonitorSysGenAlertPushConfigService configService;

    @PostMapping
    @Operation(summary = "创建/保存配置")
    public ReturnResult<Boolean> save(@RequestBody MonitorSysGenAlertPushConfig config) {
        return ReturnResult.ok(configService.saveOrUpdate(config));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    public ReturnResult<Boolean> delete(@PathVariable("id") Integer id) {
        return ReturnResult.ok(configService.removeById(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询配置")
    public ReturnResult<MonitorSysGenAlertPushConfig> get(@PathVariable("id") Integer id) {
        return ReturnResult.ok(configService.getById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "按服务器与类型查询启用配置")
    public ReturnResult<List<MonitorSysGenAlertPushConfig>> list(
            @Parameter(description = "服务器ID，可为空") @RequestParam(value = "serverId", required = false) Integer serverId,
            @Parameter(description = "告警类型，可为空") @RequestParam(value = "type", required = false) String type) {
        AlertType t = type == null ? null : AlertType.fromCode(type);
        return configService.listEnabledByServerAndType(serverId, t);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询配置")
    public ReturnPageResult<MonitorSysGenAlertPushConfig> page(Query<MonitorSysGenAlertPushConfig> page,
                                                               MonitorSysGenAlertPushConfig entity) {
        Page<MonitorSysGenAlertPushConfig> p = page.createPage();
        return PageResultUtils.ok(configService.page(p, Wrappers.lambdaQuery(entity)));
    }
}

