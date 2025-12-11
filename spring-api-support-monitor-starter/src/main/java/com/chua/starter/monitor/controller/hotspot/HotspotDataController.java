package com.chua.starter.monitor.controller.hotspot;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.HotspotReportData;
import com.chua.starter.monitor.service.hotspot.HotspotDataService;
import com.chua.starter.monitor.service.hotspot.impl.HotspotDataServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hotspot Agent 数据接收控制器
 * 接收和处理 Hotspot Agent 上报的监控数据
 *
 * @author CH
 * @since 2024/12/11
 * @version 4.0.0.34
 */
@Slf4j
@RestController
@RequestMapping("/v1/hotspot")
@Api(tags = "Hotspot Agent 数据管理")
@RequiredArgsConstructor
public class HotspotDataController {

    private final HotspotDataService hotspotDataService;
    private final HotspotDataServiceImpl hotspotDataServiceImpl;

    /**
     * 接收 Hotspot Agent 上报数据
     *
     * @param reportData 上报数据
     * @return 处理结果
     */
    @PostMapping("/report")
    @ApiOperation("接收 Hotspot Agent 上报数据")
    public ReturnResult<Boolean> receiveReportForHotspot(
            @ApiParam("上报数据") @RequestBody HotspotReportData reportData) {
        try {
            log.debug("收到 Hotspot Agent 上报数据: appName={}, type={}",
                    reportData.getAppName(), reportData.getReportType());

            boolean result = hotspotDataService.handleReport(reportData);
            if (result) {
                return ReturnResult.success(true);
            } else {
                return ReturnResult.error("数据处理失败");
            }
        } catch (Exception e) {
            log.error("处理 Hotspot Agent 上报数据失败", e);
            return ReturnResult.error("数据处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有已注册的应用列表
     *
     * @return 应用名称列表
     */
    @GetMapping("/apps")
    @ApiOperation("获取所有已注册的应用列表")
    public ReturnResult<List<String>> getAllAppsForHotspot() {
        try {
            List<String> apps = hotspotDataServiceImpl.getAllAppNames();
            return ReturnResult.success(apps);
        } catch (Exception e) {
            log.error("获取应用列表失败", e);
            return ReturnResult.error("获取应用列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取链路追踪数据
     *
     * @param appName 应用名称
     * @return 链路追踪数据列表
     */
    @GetMapping("/trace/{appName}")
    @ApiOperation("获取链路追踪数据")
    public ReturnResult<List<HotspotReportData.TraceData>> getTraceDataForHotspot(
            @ApiParam("应用名称") @PathVariable String appName) {
        try {
            List<HotspotReportData.TraceData> data = hotspotDataServiceImpl.getTraceData(appName);
            return ReturnResult.success(data);
        } catch (Exception e) {
            log.error("获取链路追踪数据失败: appName={}", appName, e);
            return ReturnResult.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取 SQL 监控数据
     *
     * @param appName 应用名称
     * @return SQL 数据列表
     */
    @GetMapping("/sql/{appName}")
    @ApiOperation("获取 SQL 监控数据")
    public ReturnResult<List<HotspotReportData.SqlData>> getSqlDataForHotspot(
            @ApiParam("应用名称") @PathVariable String appName) {
        try {
            List<HotspotReportData.SqlData> data = hotspotDataServiceImpl.getSqlData(appName);
            return ReturnResult.success(data);
        } catch (Exception e) {
            log.error("获取 SQL 监控数据失败: appName={}", appName, e);
            return ReturnResult.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志数据
     *
     * @param appName 应用名称
     * @return 日志数据列表
     */
    @GetMapping("/log/{appName}")
    @ApiOperation("获取日志数据")
    public ReturnResult<List<HotspotReportData.LogData>> getLogDataForHotspot(
            @ApiParam("应用名称") @PathVariable String appName) {
        try {
            List<HotspotReportData.LogData> data = hotspotDataServiceImpl.getLogData(appName);
            return ReturnResult.success(data);
        } catch (Exception e) {
            log.error("获取日志数据失败: appName={}", appName, e);
            return ReturnResult.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取异常数据
     *
     * @param appName 应用名称
     * @return 异常数据列表
     */
    @GetMapping("/exception/{appName}")
    @ApiOperation("获取异常数据")
    public ReturnResult<List<HotspotReportData.ExceptionData>> getExceptionDataForHotspot(
            @ApiParam("应用名称") @PathVariable String appName) {
        try {
            List<HotspotReportData.ExceptionData> data = hotspotDataServiceImpl.getExceptionData(appName);
            return ReturnResult.success(data);
        } catch (Exception e) {
            log.error("获取异常数据失败: appName={}", appName, e);
            return ReturnResult.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务实例数据
     *
     * @param appName 应用名称
     * @return 服务实例数据列表
     */
    @GetMapping("/server/{appName}")
    @ApiOperation("获取服务实例数据")
    public ReturnResult<List<HotspotReportData.ServerData>> getServerDataForHotspot(
            @ApiParam("应用名称") @PathVariable String appName) {
        try {
            List<HotspotReportData.ServerData> data = hotspotDataServiceImpl.getServerData(appName);
            return ReturnResult.success(data);
        } catch (Exception e) {
            log.error("获取服务实例数据失败: appName={}", appName, e);
            return ReturnResult.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定应用的缓存数据
     *
     * @param appName 应用名称
     * @return 处理结果
     */
    @DeleteMapping("/cache/{appName}")
    @ApiOperation("清除指定应用的缓存数据")
    public ReturnResult<Boolean> clearCacheForHotspot(
            @ApiParam("应用名称") @PathVariable String appName) {
        try {
            hotspotDataServiceImpl.clearCache(appName);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("清除缓存失败: appName={}", appName, e);
            return ReturnResult.error("清除缓存失败: " + e.getMessage());
        }
    }
}
