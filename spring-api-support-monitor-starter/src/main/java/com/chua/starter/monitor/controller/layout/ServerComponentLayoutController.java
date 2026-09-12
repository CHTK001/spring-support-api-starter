package com.chua.starter.monitor.controller.layout;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import com.chua.starter.monitor.service.layout.MonitorSysGenServerComponentLayoutService;
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
* 服务器组件布局管理接口，提供组件布局的查询、保存、更新、删除、
* 批量位置更新、启用禁用、统计、复制与重置等能力。
*
* @since 2026-07-28
* @author CH
 */
@Slf4j
@RestController
@RequestMapping("/monitor/server/component/layout")
@Api(tags = "服务器组件布局管理")
@Tag(name = "服务器组件布局管理")
@RequiredArgsConstructor
public class ServerComponentLayoutController {

    /**
    * 请求体键：源服务器标识
     */
    private static final String PARAM_SOURCE_SERVER_ID = "sourceServerId";

    /**
    * 请求体键：目标服务器标识
     */
    private static final String PARAM_TARGET_SERVER_ID = "targetServerId";

    /**
    * 服务器组件布局服务
     */
    private final MonitorSysGenServerComponentLayoutService componentLayoutService;

    /**
    * 获取服务器组件布局列表。
    *
    * @param serverId 服务器标识
    * @return 组件布局列表
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "获取服务器组件布局列表")
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> listByServer(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {
        return componentLayoutService.listByServer(serverId);
    }

    /**
    * 获取启用的组件布局列表。
    *
    * @param serverId 服务器标识
    * @return 启用状态的组件布局列表
     */
    @GetMapping("/server/{serverId}/enabled")
    @Operation(summary = "获取启用的组件布局列表")
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> listEnabled(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {
        return componentLayoutService.listEnabledByServer(serverId);
    }

    /**
    * 获取指定组件布局。
    *
    * @param serverId    服务器标识
    * @param componentId 组件标识
    * @return 组件布局记录
     */
    @GetMapping("/server/{serverId}/component/{componentId}")
    @Operation(summary = "获取指定组件布局")
    public ReturnResult<MonitorSysGenServerComponentLayout> getByComponent(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @ApiParam("组件ID") @PathVariable Integer componentId) {
        return componentLayoutService.getByComponent(serverId, componentId);
    }

    /**
    * 保存组件布局配置。
    *
    * @param layout 组件布局实体
    * @return 保存后的组件布局
     */
    @PostMapping
    @Operation(summary = "保存组件布局配置")
    public ReturnResult<MonitorSysGenServerComponentLayout> save(@RequestBody MonitorSysGenServerComponentLayout layout) {
        return componentLayoutService.saveComponentLayout(layout);
    }

    /**
    * 更新组件布局配置。
    *
    * @param layout 组件布局实体（必须包含组件布局标识）
    * @return 更新后的组件布局
     */
    @PutMapping
    @Operation(summary = "更新组件布局配置")
    public ReturnResult<MonitorSysGenServerComponentLayout> update(@RequestBody MonitorSysGenServerComponentLayout layout) {
        if (layout.getMonitorSysGenServerComponentLayoutId() == null) {
            return ReturnResult.illegal("组件布局ID不能为空");
        }
        componentLayoutService.updateById(layout);
        return ReturnResult.ok(componentLayoutService.getById(layout.getMonitorSysGenServerComponentLayoutId()));
    }

    /**
    * 删除组件布局配置。
    *
    * @param layoutId 组件布局标识
    * @return 是否删除成功
    * @param @ApiParam("组件布局ID" @ApiParam("组件布局ID"
     */
    @DeleteMapping("/{layoutId}")
    @Operation(summary = "删除组件布局配置")
    public ReturnResult<Boolean> delete(@ApiParam("组件布局ID") @PathVariable Integer layoutId) {
        return ReturnResult.ok(componentLayoutService.removeById(layoutId));
    }

    /**
    * 按组件删除布局。
    *
    * @param componentId 组件标识
    * @return 是否删除成功
    * @param @ApiParam("组件ID" @ApiParam("组件ID"
     */
    @DeleteMapping("/component/{componentId}")
    @Operation(summary = "按组件删除布局")
    public ReturnResult<Boolean> deleteByComponent(@ApiParam("组件ID") @PathVariable Integer componentId) {
        return componentLayoutService.deleteByComponent(componentId);
    }

    /**
    * 按服务器删除全部布局。
    *
    * @param serverId 服务器标识
    * @return 是否删除成功
    * @param @ApiParam("服务器ID" @ApiParam("服务器ID"
     */
    @DeleteMapping("/server/{serverId}")
    @Operation(summary = "按服务器删除全部布局")
    public ReturnResult<Boolean> deleteByServer(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return componentLayoutService.deleteByServer(serverId);
    }

    /**
    * 批量更新组件位置。
    *
    * @param layouts 组件布局集合，每项必须包含组件布局标识
    * @return 更新成功的数量
     */
    @PostMapping("/batch/position")
    @Operation(summary = "批量更新位置")
    public ReturnResult<Integer> batchUpdatePosition(@RequestBody List<MonitorSysGenServerComponentLayout> layouts) {
        return componentLayoutService.batchUpdatePosition(layouts);
    }

    /**
    * 创建组件布局。
    *
    * @param layout 组件布局实体
    * @return 创建后的组件布局
     */
    @PostMapping("/create")
    @Operation(summary = "创建组件布局")
    public ReturnResult<MonitorSysGenServerComponentLayout> create(@RequestBody MonitorSysGenServerComponentLayout layout) {
        return componentLayoutService.saveComponentLayout(layout);
    }

    /**
    * 更新组件位置。
    *
    * @param layoutId 组件布局标识
    * @param layout   携带位置信息的实体
    * @return 是否更新成功
     */
    @PutMapping("/{layoutId}/position")
    @Operation(summary = "更新组件位置")
    public ReturnResult<Boolean> updatePosition(
            @ApiParam("组件布局ID") @PathVariable Integer layoutId,
            @RequestBody MonitorSysGenServerComponentLayout layout) {
        return componentLayoutService.updatePosition(layoutId, layout);
    }

    /**
    * 启用/禁用布局。
    *
    * @param layoutId 组件布局标识
    * @param status   状态值，1-启用，0-禁用
    * @return 是否更新成功
     */
    @PutMapping("/{layoutId}/status")
    @Operation(summary = "启用/禁用布局")
    public ReturnResult<Boolean> updateStatus(
            @ApiParam("组件布局ID") @PathVariable Integer layoutId,
            @ApiParam("状态（0禁用/1启用）") @RequestParam Integer status) {
        return componentLayoutService.updateStatus(layoutId, status);
    }

    /**
    * 获取布局统计。
    *
    * @param serverId 服务器标识
    * @return 统计结果，包含 total/已启用/已禁用
    * @param @ApiParam("服务器ID" @ApiParam("服务器ID"
     */
    @GetMapping("/server/{serverId}/statistics")
    @Operation(summary = "获取布局统计")
    public ReturnResult<Map<String, Object>> statistics(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return componentLayoutService.statistics(serverId);
    }

    /**
    * 复制布局到其他服务器。请求体格式：{"源服务端id": 1, "Target服务端id": 2}。
    *
    * @param body 请求体，包含源服务器标识与目标服务器标识
    * @return 复制的组件布局数量
     */
    @PostMapping("/copy")
    @Operation(summary = "复制布局到其他服务器")
    public ReturnResult<Integer> copy(@RequestBody Map<String, Integer> body) {
        Integer sourceServerId = body.get(PARAM_SOURCE_SERVER_ID);
        Integer targetServerId = body.get(PARAM_TARGET_SERVER_ID);
        return componentLayoutService.copyToServer(sourceServerId, targetServerId);
    }

    /**
    * 重置服务器布局。
    *
    * @param serverId 服务器标识
    * @return 删除的记录数量
    * @param @ApiParam("服务器ID" @ApiParam("服务器ID"
     */
    @PostMapping("/server/{serverId}/reset")
    @Operation(summary = "重置服务器布局")
    public ReturnResult<Integer> reset(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return componentLayoutService.resetServer(serverId);
    }
}
