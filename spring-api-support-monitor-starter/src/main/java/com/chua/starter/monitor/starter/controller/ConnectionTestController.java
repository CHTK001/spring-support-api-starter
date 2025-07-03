package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.ConnectionTestResultDTO;
import com.chua.starter.monitor.starter.service.ConnectionTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 连接测试控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/connection-test")
@RequiredArgsConstructor
@Api(tags = "连接测试管理")
@Tag(name = "连接测试管理", description = "提供各种类型的连接测试功能")
public class ConnectionTestController {

    private final ConnectionTestService connectionTestService;

    /**
     * 测试HTTP连接
     */
    @PostMapping("/http")
    @ApiOperation("测试HTTP连接")
    @Operation(summary = "测试HTTP连接", description = "测试指定HTTP/HTTPS地址的连接状态")
    public ReturnResult<ConnectionTestResultDTO> testHttpConnection(
            @ApiParam(value = "HTTP地址", required = true)
            @Parameter(description = "HTTP地址", required = true)
            @RequestParam String url) {
        
        log.info("测试HTTP连接: {}", url);
        return connectionTestService.testHttpConnection(url);
    }

    /**
     * 测试Socket连接
     */
    @PostMapping("/socket")
    @ApiOperation("测试Socket连接")
    @Operation(summary = "测试Socket连接", description = "测试指定主机和端口的Socket连接状态")
    public ReturnResult<ConnectionTestResultDTO> testSocketConnection(
            @ApiParam(value = "主机地址", required = true)
            @Parameter(description = "主机地址", required = true)
            @RequestParam String host,
            
            @ApiParam(value = "端口号", required = true)
            @Parameter(description = "端口号", required = true)
            @RequestParam int port) {
        
        log.info("测试Socket连接: {}:{}", host, port);
        return connectionTestService.testSocketConnection(host, port);
    }

    /**
     * 测试Prometheus连接
     */
    @PostMapping("/prometheus")
    @ApiOperation("测试Prometheus连接")
    @Operation(summary = "测试Prometheus连接", description = "测试Prometheus服务器的连接状态")
    public ReturnResult<ConnectionTestResultDTO> testPrometheusConnection(
            @ApiParam(value = "Prometheus地址", required = true)
            @Parameter(description = "Prometheus地址", required = true)
            @RequestParam String prometheusUrl) {
        
        log.info("测试Prometheus连接: {}", prometheusUrl);
        return connectionTestService.testPrometheusConnection(prometheusUrl);
    }

    /**
     * 通用连接测试
     */
    @PostMapping("/general")
    @ApiOperation("通用连接测试")
    @Operation(summary = "通用连接测试", description = "根据地址格式自动判断连接类型并进行测试")
    public ReturnResult<ConnectionTestResultDTO> testConnection(
            @ApiParam(value = "连接地址", required = true, 
                     example = "http://example.com, https://prometheus:9090, socket://host:port, host:port")
            @Parameter(description = "连接地址，支持多种格式：http://、https://、socket://、host:port", 
                      required = true)
            @RequestParam String address) {
        
        log.info("通用连接测试: {}", address);
        return connectionTestService.testConnection(address);
    }

    /**
     * 批量连接测试
     */
    @PostMapping("/batch")
    @ApiOperation("批量连接测试")
    @Operation(summary = "批量连接测试", description = "批量测试多个地址的连接状态")
    public ReturnResult<java.util.List<ConnectionTestResultDTO>> batchTestConnection(
            @ApiParam(value = "连接地址列表", required = true)
            @Parameter(description = "连接地址列表", required = true)
            @RequestBody java.util.List<String> addresses) {
        
        log.info("批量连接测试，地址数量: {}", addresses.size());
        
        java.util.List<ConnectionTestResultDTO> results = new java.util.ArrayList<>();
        for (String address : addresses) {
            try {
                ReturnResult<ConnectionTestResultDTO> result = connectionTestService.testConnection(address);
                if (result.isOk() && result.getData() != null) {
                    results.add(result.getData());
                } else {
                    // 创建失败结果
                    ConnectionTestResultDTO failedResult = ConnectionTestResultDTO.builder()
                            .address(address)
                            .success(false)
                            .message(result.getMsg())
                            .testTime(java.time.LocalDateTime.now())
                            .build();
                    results.add(failedResult);
                }
            } catch (Exception e) {
                log.error("批量测试地址 {} 时发生异常", address, e);
                ConnectionTestResultDTO errorResult = ConnectionTestResultDTO.builder()
                        .address(address)
                        .success(false)
                        .errorMessage("测试异常: " + e.getMessage())
                        .message("测试异常")
                        .testTime(java.time.LocalDateTime.now())
                        .build();
                results.add(errorResult);
            }
        }
        
        return ReturnResult.ok(results);
    }

    /**
     * 获取连接测试帮助信息
     */
    @GetMapping("/help")
    @ApiOperation("获取连接测试帮助信息")
    @Operation(summary = "获取连接测试帮助信息", description = "获取支持的连接格式和使用说明")
    public ReturnResult<java.util.Map<String, Object>> getTestHelp() {
        
        java.util.Map<String, Object> help = new java.util.HashMap<>();
        
        help.put("supportedFormats", java.util.Arrays.asList(
                "http://example.com",
                "https://example.com",
                "socket://host:port",
                "host:port",
                "prometheus-server:9090"
        ));
        
        help.put("examples", java.util.Map.of(
                "HTTP测试", "http://www.baidu.com",
                "HTTPS测试", "https://www.google.com",
                "Socket测试", "socket://127.0.0.1:22",
                "Prometheus测试", "http://localhost:9090",
                "简化格式", "127.0.0.1:3306"
        ));
        
        help.put("notes", java.util.Arrays.asList(
                "HTTP/HTTPS测试使用HEAD请求方法",
                "Socket测试尝试建立TCP连接",
                "Prometheus测试会检查健康检查端点",
                "默认超时时间为5秒",
                "支持自动格式识别"
        ));
        
        return ReturnResult.ok(help);
    }
}
