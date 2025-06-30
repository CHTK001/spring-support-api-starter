package com.chua.starter.monitor.starter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.dto.ServerProxyDto;
import com.chua.starter.monitor.starter.entity.MonitorProxy;
import com.chua.starter.monitor.starter.service.MonitorProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务器代理关联控制器（适配器模式）
 * 将前端的服务器代理关联请求映射到现有的 MonitorProxy 实体
 *
 * @author CH
 * @since 2024/12/27
 */
@RestController
@RequestMapping("v1/gen/server/proxy")
@Tag(name = "服务器代理关联管理")
@RequiredArgsConstructor
@Slf4j
public class MonitorSysGenServerProxyController {

    private final MonitorProxyService monitorProxyService;

    /**
     * 分页查询代理列表（适配前端请求）
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键字
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询代理列表")
    @GetMapping("page")
    public ReturnResult<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<MonitorProxy> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<MonitorProxy> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询启用的代理
        queryWrapper.eq(MonitorProxy::getProxyStatus, 1);

        // 如果有关键字，按名称模糊查询
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(MonitorProxy::getProxyName, keyword);
        }

        queryWrapper.orderByDesc(MonitorProxy::getProxyId);

        IPage<MonitorProxy> result = monitorProxyService.page(pageParam, queryWrapper);

        // 转换为前端期望的格式
        List<ServerProxyDto> dtoList = result.getRecords().stream()
                .map(ServerProxyDto::fromMonitorProxy)
                .collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", dtoList);
        responseData.put("total", result.getTotal());
        responseData.put("page", result.getCurrent());
        responseData.put("pageSize", result.getSize());

        return ReturnResult.success(responseData);
    }

    /**
     * 根据ID获取代理信息
     *
     * @param id 代理ID
     * @return 代理信息
     */
    @ResponseBody
    @Operation(summary = "根据ID获取代理信息")
    @GetMapping("{id}")
    public ReturnResult<ServerProxyDto> getById(@Parameter(name = "代理ID") @PathVariable Integer id) {
        MonitorProxy proxy = monitorProxyService.getById(id);
        ServerProxyDto dto = ServerProxyDto.fromMonitorProxy(proxy);
        return ReturnResult.success(dto);
    }

    /**
     * 获取所有启用的代理列表
     *
     * @return 代理列表
     */
    @ResponseBody
    @Operation(summary = "获取所有启用的代理列表")
    @GetMapping("list")
    public ReturnResult<List<ServerProxyDto>> list() {
        LambdaQueryWrapper<MonitorProxy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorProxy::getProxyStatus, 1)
                   .orderByDesc(MonitorProxy::getProxyId);

        List<MonitorProxy> proxies = monitorProxyService.list(queryWrapper);
        List<ServerProxyDto> dtoList = proxies.stream()
                .map(ServerProxyDto::fromMonitorProxy)
                .collect(Collectors.toList());
        return ReturnResult.success(dtoList);
    }

    /**
     * 根据服务器ID获取代理关联列表（适配前端请求）
     * 实际上返回所有可用的代理列表供前端选择
     *
     * @param serverId 服务器ID
     * @return 代理列表
     */
    @ResponseBody
    @Operation(summary = "根据服务器ID获取可用代理列表")
    @GetMapping("server/{serverId}")
    public ReturnResult<List<ServerProxyDto>> getByServerId(@Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        // 返回所有启用的代理供前端选择
        LambdaQueryWrapper<MonitorProxy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorProxy::getProxyStatus, 1)
                   .orderByDesc(MonitorProxy::getProxyId);

        List<MonitorProxy> proxies = monitorProxyService.list(queryWrapper);
        List<ServerProxyDto> dtoList = proxies.stream()
                .map(ServerProxyDto::fromMonitorProxy)
                .collect(Collectors.toList());
        return ReturnResult.success(dtoList);
    }

    /**
     * 根据代理类型获取代理列表
     *
     * @param proxyType 代理类型
     * @return 代理列表
     */
    @ResponseBody
    @Operation(summary = "根据代理类型获取代理列表")
    @GetMapping("type/{proxyType}")
    public ReturnResult<List<ServerProxyDto>> getByProxyType(@Parameter(name = "代理类型") @PathVariable String proxyType) {
        LambdaQueryWrapper<MonitorProxy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorProxy::getProxyStatus, 1)
                   .eq(MonitorProxy::getProxyProtocol, proxyType)
                   .orderByDesc(MonitorProxy::getProxyId);

        List<MonitorProxy> proxies = monitorProxyService.list(queryWrapper);
        List<ServerProxyDto> dtoList = proxies.stream()
                .map(ServerProxyDto::fromMonitorProxy)
                .collect(Collectors.toList());
        return ReturnResult.success(dtoList);
    }

    /**
     * 测试代理连接
     *
     * @param id 代理ID
     * @return 测试结果
     */
    @ResponseBody
    @Operation(summary = "测试代理连接")
    @PostMapping("test/{id}")
    public ReturnResult<Boolean> testConnection(@Parameter(name = "代理ID") @PathVariable Integer id) {
        try {
            MonitorProxy proxy = monitorProxyService.getById(id);
            if (proxy == null) {
                return ReturnResult.error("代理不存在");
            }
            
            // TODO: 实现代理连接测试逻辑
            log.info("测试代理连接: {}", proxy);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("测试代理连接失败: id={}", id, e);
            return ReturnResult.error("测试代理连接失败: " + e.getMessage());
        }
    }

    /**
     * 启用代理
     *
     * @param id 代理ID
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "启用代理")
    @PostMapping("enable/{id}")
    public ReturnResult<Boolean> enable(@Parameter(name = "代理ID") @PathVariable Integer id) {
        try {
            MonitorProxy proxy = monitorProxyService.getById(id);
            if (proxy == null) {
                return ReturnResult.error("代理不存在");
            }
            
            proxy.setProxyStatus(1);
            boolean result = monitorProxyService.updateById(proxy);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("启用代理失败: id={}", id, e);
            return ReturnResult.error("启用代理失败: " + e.getMessage());
        }
    }

    /**
     * 禁用代理
     *
     * @param id 代理ID
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "禁用代理")
    @PostMapping("disable/{id}")
    public ReturnResult<Boolean> disable(@Parameter(name = "代理ID") @PathVariable Integer id) {
        try {
            MonitorProxy proxy = monitorProxyService.getById(id);
            if (proxy == null) {
                return ReturnResult.error("代理不存在");
            }
            
            proxy.setProxyStatus(0);
            boolean result = monitorProxyService.updateById(proxy);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("禁用代理失败: id={}", id, e);
            return ReturnResult.error("禁用代理失败: " + e.getMessage());
        }
    }

    /**
     * 获取代理统计信息
     *
     * @return 统计信息
     */
    @ResponseBody
    @Operation(summary = "获取代理统计信息")
    @GetMapping("statistics")
    public ReturnResult<Object> getStatistics() {
        try {
            // 总数
            long totalCount = monitorProxyService.count();

            // 启用数量
            LambdaQueryWrapper<MonitorProxy> enabledWrapper = new LambdaQueryWrapper<>();
            enabledWrapper.eq(MonitorProxy::getProxyStatus, 1);
            long enabledCount = monitorProxyService.count(enabledWrapper);

            // 禁用数量
            long disabledCount = totalCount - enabledCount;

            // 构建统计信息Map
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalCount", totalCount);
            statistics.put("enabledCount", enabledCount);
            statistics.put("disabledCount", disabledCount);

            return ReturnResult.success(statistics);
        } catch (Exception e) {
            log.error("获取代理统计信息失败", e);
            return ReturnResult.error("获取代理统计信息失败: " + e.getMessage());
        }
    }
}
