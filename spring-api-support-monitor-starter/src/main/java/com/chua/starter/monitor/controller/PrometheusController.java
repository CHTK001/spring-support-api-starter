package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.PrometheusGenQueryRequest;
import com.chua.starter.monitor.pojo.request.PrometheusQueryRequest;
import com.chua.starter.monitor.service.impl.PrometheusService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * prometheus服务
 *
 * @author CH
 * @since 2025/5/29 16:26
 */
@RestController
@Slf4j
@Api(tags = "prometheus服务")
@Tag(name = "prometheus服务")
@RequestMapping("/v2/prometheus")
@RequiredArgsConstructor
public class PrometheusController {

    final PrometheusService prometheusService;


    /**
     * 是否在线
     *
     * @param prometheusAddress prometheus地址
     * @return
     */
    @GetMapping("isOnline")
    @Operation(summary = "是否在线")
    public ReturnResult<Boolean> isOnline(String monitorSysGenId) {
        return ReturnResult.preconditioning(prometheusService.isOnline(monitorSysGenId)).toResult();
    }

    /**
     * 重载
     *
     * @param prometheusAddress prometheus地址
     * @return
     */
    @GetMapping("reload")
    @Operation(summary = "重载")
    public ReturnResult<Boolean> reload(String monitorSysGenId) {
        return ReturnResult.preconditioning(prometheusService.reload(monitorSysGenId)).toResult();
    }


    /**
     * 查询
     *
     * @param request request
     * @return
     */
    @PostMapping("query")
    @Operation(summary = "查询")
    public ReturnResult<String> query(@RequestBody PrometheusQueryRequest request) {
        return ReturnResult.preconditioning(prometheusService.queryPrometheus(request.getPrometheusAddress(), request.getPromQL())).toResult();
    }


    /**
     * 查询
     *
     * @param request request
     * @return
     */
    @PostMapping("queryRange")
    @Operation(summary = "查询")
    public ReturnResult<JsonNode> queryRange(@RequestBody PrometheusQueryRequest request) {
        return ReturnResult.preconditioning(prometheusService.queryRange(request.getPrometheusAddress(), request.getPromQL(), request.getStart(), request.getEnd(), request.getStep())).toResult();
    }

    /**
     * 查询
     *
     * @param request request
     * @return
     */
    @PostMapping("queryRangeGen")
    @Operation(summary = "查询")
    public ReturnResult<JsonNode> queryRange(@RequestBody PrometheusGenQueryRequest request) {
        return ReturnResult.preconditioning(prometheusService.queryRange(request)).toResult();
    }

    /**
     * 查询
     *
     * @param request request
     * @return
     */
    @PostMapping("queryGen")
    @Operation(summary = "查询")
    public ReturnResult<JsonNode> query(@RequestBody PrometheusGenQueryRequest request) {
        return ReturnResult.preconditioning(prometheusService.query(request)).toResult();
    }
}
