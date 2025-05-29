package com.chua.starter.monitor.starter.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorProxyPlugin;
import com.chua.starter.monitor.starter.service.MonitorProxyPluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代理组件
 * @author CH
 * @since 2024/5/13
 */
@RestController
@RequestMapping("v1/proxy/plugin")
@Tag(name = "代理组件")
@RequiredArgsConstructor
public class MonitorProxyPluginController  {

    private final MonitorProxyPluginService monitorProxyPluginService;


    /**
     * 查询代理所有的组件
     * @param proxyId
     * @return
     */
    @Operation(summary = "查询代理所有的组件")
    @GetMapping("list")
    public ReturnResult<List<MonitorProxyPlugin>> get(String proxyId) {
        return ReturnResult.success(monitorProxyPluginService.list(Wrappers.<MonitorProxyPlugin>lambdaQuery()
                .eq(MonitorProxyPlugin::getProxyId, proxyId)));
    }

    /**
     * 更新组件配置
     * @param entity
     * @return
     */
    @Operation(summary = "更新组件配置")
    @PutMapping("update")
    public ReturnResult<Boolean> update(@RequestBody MonitorProxyPlugin entity) {
        return ReturnResult.of(monitorProxyPluginService.updateById(entity));
    }

    /**
     * 删除组件配置
     * @param id
     * @return
     */
    @Operation(summary = "删除组件配置")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> delete(@RequestParam("proxyPluginId") String proxyPluginId) {
        return ReturnResult.of(monitorProxyPluginService.removeById(proxyPluginId));
    }

    /**
     * 添加组件配置
     * @param entity
     * @return
     * */
    @Operation(summary = "添加组件配置")
    @PostMapping("save")
    public ReturnResult<Boolean> save(@RequestBody MonitorProxyPlugin entity) {
        return ReturnResult.of(monitorProxyPluginService.save(entity));
    }
}
