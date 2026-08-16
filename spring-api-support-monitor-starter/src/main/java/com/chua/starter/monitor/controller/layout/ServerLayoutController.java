package com.chua.starter.monitor.controller.layout;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerLayout;
import com.chua.starter.monitor.service.layout.MonitorSysGenServerComponentLayoutService;
import com.chua.starter.monitor.service.layout.MonitorSysGenServerLayoutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 服务器布局管理接口，提供布局列表、默认布局、保存/更新/删除、
 * 从组件生成布局、应用布局、复制布局以及组件图表配置的管理能力。
 *
 * @author CH
 * @since 2026-07-28
 */
@Slf4j
@RestController
@RequestMapping("/v1/gen/server/layout")
@Api(tags = "服务器布局管理")
@Tag(name = "服务器布局管理")
@RequiredArgsConstructor
public class ServerLayoutController {

    /**
     * 请求体键：组件ID
     */
    private static final String PARAM_COMPONENT_ID = "componentId";

    /**
     * 请求体键：图表配置 JSON
     */
    private static final String PARAM_CHART_CONFIG = "chartConfig";

    /**
     * 服务器布局配置服务
     */
    private final MonitorSysGenServerLayoutService layoutService;

    /**
     * 服务器组件布局服务，用于应用布局与读写组件图表配置
     */
    private final MonitorSysGenServerComponentLayoutService componentLayoutService;

    /**
     * 获取服务器布局列表。
     *
     * @param serverId 服务器ID
     * @return 布局列表
     */
    @GetMapping("/list/{serverId}")
    @Operation(summary = "获取服务器布局列表")
    public ReturnResult<List<MonitorSysGenServerLayout>> list(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return layoutService.listByServerId(serverId);
    }

    /**
     * 获取服务器默认布局，不存在时自动创建。
     *
     * @param serverId 服务器ID
     * @return 默认布局
     */
    @GetMapping("/default/{serverId}")
    @Operation(summary = "获取服务器默认布局")
    public ReturnResult<MonitorSysGenServerLayout> getDefault(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return layoutService.getDefaultLayout(serverId);
    }

    /**
     * 保存服务器布局配置。
     *
     * @param layout 布局实体
     * @return 保存后的布局
     */
    @PostMapping("/save")
    @Operation(summary = "保存服务器布局配置")
    public ReturnResult<MonitorSysGenServerLayout> save(@RequestBody MonitorSysGenServerLayout layout) {
        return layoutService.saveLayout(layout);
    }

    /**
     * 更新服务器布局配置，版本号自动递增。
     *
     * @param layout 布局实体（必须包含布局ID）
     * @return 更新后的布局
     */
    @PutMapping("/update")
    @Operation(summary = "更新服务器布局配置")
    public ReturnResult<MonitorSysGenServerLayout> update(@RequestBody MonitorSysGenServerLayout layout) {
        return layoutService.updateLayout(layout);
    }

    /**
     * 删除服务器布局配置，默认布局不允许删除。
     *
     * @param layoutId 布局ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{layoutId}")
    @Operation(summary = "删除服务器布局配置")
    public ReturnResult<Boolean> delete(@ApiParam("布局ID") @PathVariable Integer layoutId) {
        return layoutService.deleteLayout(layoutId);
    }

    /**
     * 设置默认布局。
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 是否设置成功
     */
    @PutMapping("/default/{serverId}/{layoutId}")
    @Operation(summary = "设置默认布局")
    public ReturnResult<Boolean> setDefault(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @ApiParam("布局ID") @PathVariable Integer layoutId) {
        return layoutService.setDefaultLayout(serverId, layoutId);
    }

    /**
     * 从组件配置生成布局。
     *
     * @param serverId   服务器ID
     * @param layoutName 布局名称，可为空
     * @return 生成的布局
     */
    @PostMapping("/generate/{serverId}")
    @Operation(summary = "从组件配置生成布局")
    public ReturnResult<MonitorSysGenServerLayout> generate(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @ApiParam("布局名称") @RequestParam(required = false) String layoutName) {
        return layoutService.generateFromComponents(serverId, layoutName);
    }

    /**
     * 应用布局到服务器组件。
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 应用成功的组件数量
     */
    @PostMapping("/apply/{serverId}/{layoutId}")
    @Operation(summary = "应用布局到服务器组件")
    public ReturnResult<Integer> apply(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @ApiParam("布局ID") @PathVariable Integer layoutId) {
        return layoutService.applyToComponents(serverId, layoutId);
    }

    /**
     * 复制布局到其他服务器。
     *
     * @param sourceLayoutId 源布局ID
     * @param targetServerId 目标服务器ID
     * @param layoutName     新布局名称，可为空
     * @return 复制生成的新布局
     */
    @PostMapping("/copy/{sourceLayoutId}/{targetServerId}")
    @Operation(summary = "复制布局到其他服务器")
    public ReturnResult<MonitorSysGenServerLayout> copy(
            @ApiParam("源布局ID") @PathVariable Integer sourceLayoutId,
            @ApiParam("目标服务器ID") @PathVariable Integer targetServerId,
            @ApiParam("布局名称") @RequestParam(required = false) String layoutName) {
        return layoutService.copyLayout(sourceLayoutId, targetServerId, layoutName);
    }

    /**
     * 获取布局详情。
     *
     * @param layoutId 布局ID
     * @return 布局详情
     */
    @GetMapping("/detail/{layoutId}")
    @Operation(summary = "获取布局详情")
    public ReturnResult<MonitorSysGenServerLayout> detail(@ApiParam("布局ID") @PathVariable Integer layoutId) {
        return layoutService.getDetail(layoutId);
    }

    /**
     * 根据类型获取布局列表。
     *
     * @param layoutType 布局类型（default/generated/copied/custom）
     * @return 布局列表
     */
    @GetMapping("/type/{layoutType}")
    @Operation(summary = "根据类型获取布局列表")
    public ReturnResult<List<MonitorSysGenServerLayout>> listByType(@ApiParam("布局类型") @PathVariable String layoutType) {
        return layoutService.listByType(layoutType);
    }

    /**
     * 创建默认布局。
     *
     * @param serverId 服务器ID
     * @return 创建的默认布局
     */
    @PostMapping("/create-default/{serverId}")
    @Operation(summary = "创建默认布局")
    public ReturnResult<MonitorSysGenServerLayout> createDefault(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return layoutService.createDefaultLayout(serverId);
    }

    /**
     * 保存组件图表配置。请求体格式：{"componentId": 1, "chartConfig": "{...}"}。
     *
     * @param serverId 服务器ID
     * @param body     请求体，包含 componentId 与 chartConfig
     * @return 是否保存成功
     */
    @PostMapping("/component/config/{serverId}")
    @Operation(summary = "保存组件图表配置")
    public ReturnResult<Boolean> saveComponentConfig(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @RequestBody Map<String, Object> body) {
        Object componentId = body.get(PARAM_COMPONENT_ID);
        Object chartConfig = body.get(PARAM_CHART_CONFIG);
        if (componentId == null) {
            return ReturnResult.illegal("componentId 不能为空");
        }
        return componentLayoutService.saveChartConfig(
                serverId,
                Integer.valueOf(componentId.toString()),
                chartConfig == null ? null : chartConfig.toString());
    }

    /**
     * 获取组件图表配置。
     *
     * @param serverId    服务器ID
     * @param componentId 组件ID
     * @return 图表配置 JSON 字符串
     */
    @GetMapping("/component/config/{serverId}/{componentId}")
    @Operation(summary = "获取组件图表配置")
    public ReturnResult<String> getComponentConfig(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @ApiParam("组件ID") @PathVariable Integer componentId) {
        return componentLayoutService.getChartConfig(serverId, componentId);
    }

    /**
     * 批量保存组件图表配置。请求体为数组：[{"componentId": 1, "chartConfig": "{...}"}, ...]。
     *
     * @param serverId 服务器ID
     * @param configs  配置列表
     * @return 保存成功的数量
     */
    @PostMapping("/component/config/batch/{serverId}")
    @Operation(summary = "批量保存组件图表配置")
    public ReturnResult<Integer> batchSaveComponentConfig(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @RequestBody List<Map<String, Object>> configs) {
        if (configs == null || configs.isEmpty()) {
            return ReturnResult.ok(0);
        }
        int saved = 0;
        for (Map<String, Object> config : configs) {
            Object componentId = config.get(PARAM_COMPONENT_ID);
            if (componentId == null) {
                continue;
            }
            Object chartConfig = config.get(PARAM_CHART_CONFIG);
            ReturnResult<Boolean> result = componentLayoutService.saveChartConfig(
                    serverId,
                    Integer.valueOf(componentId.toString()),
                    chartConfig == null ? null : chartConfig.toString());
            if (result.isOk() && Boolean.TRUE.equals(result.getData())) {
                saved++;
            }
        }
        return ReturnResult.ok(saved);
    }
}
