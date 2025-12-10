package com.chua.starter.monitor.controller;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.FileSystemSseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 文件系统SSE控制器
 * 提供文件系统相关的Server-Sent Events服务
 *
 * @author CH
 * @since 2024/12/13
 */
@RestController
@Slf4j
@Api(tags = "文件系统SSE接口")
@Tag(name = "文件系统SSE接口")
@RequestMapping("/v1/filesystem/sse")
@RequiredArgsConstructor
public class FileSystemSseController {

    private final FileSystemSseService fileSystemSseService;

    /**
     * 建立文件系统SSE连接
     * 客户端通过此接口建立长连接，接收文件系统相关的实时消息
     *
     * @param clientId 客户端ID，用于标识不同的客户端连接
     * @return SseEmitter 服务端推送事件发射器
     */
    @ApiOperation("建立文件系统SSE连接")
    @Operation(summary = "建立文件系统SSE连接", description = "客户端通过此接口建立长连接，接收文件系统相关的实时消息")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
            @ApiParam("客户端ID") @Parameter(description = "客户端ID，用于标识不同的客户端连接") 
            @RequestParam(required = false) String clientId) {
        
        try {
            log.info("文件系统SSE连接请求: clientId={}", clientId);
            return fileSystemSseService.createConnection(clientId);
        } catch (Exception e) {
            log.error("创建文件系统SSE连接失败: clientId={}", clientId, e);
            throw new RuntimeException("创建SSE连接失败: " + e.getMessage());
        }
    }

    /**
     * 订阅特定类型的文件系统事件
     * 客户端可以选择性订阅感兴趣的事件类型
     *
     * @param clientId 客户端ID
     * @param eventTypes 要订阅的事件类型列表，多个类型用逗号分隔
     * @return 订阅结果
     */
    @ApiOperation("订阅文件系统事件")
    @Operation(summary = "订阅文件系统事件", description = "客户端可以选择性订阅感兴趣的事件类型")
    @PostMapping("/subscribe")
    public ReturnResult<String> subscribe(
            @ApiParam("客户端ID") @Parameter(description = "客户端ID") 
            @RequestParam String clientId,
            @ApiParam("事件类型列表") @Parameter(description = "要订阅的事件类型列表，多个类型用逗号分隔") 
            @RequestParam String eventTypes) {
        
        try {
            log.info("文件系统SSE订阅请求: clientId={}, eventTypes={}", clientId, eventTypes);
            fileSystemSseService.subscribe(clientId, eventTypes);
            return ReturnResult.success("订阅成功");
        } catch (Exception e) {
            log.error("订阅文件系统事件失败: clientId={}, eventTypes={}", clientId, eventTypes, e);
            return ReturnResult.error("订阅失败: " + e.getMessage());
        }
    }

    /**
     * 取消订阅特定类型的文件系统事件
     *
     * @param clientId 客户端ID
     * @param eventTypes 要取消订阅的事件类型列表，多个类型用逗号分隔
     * @return 取消订阅结果
     */
    @ApiOperation("取消订阅文件系统事件")
    @Operation(summary = "取消订阅文件系统事件", description = "取消订阅特定类型的文件系统事件")
    @PostMapping("/unsubscribe")
    public ReturnResult<String> unsubscribe(
            @ApiParam("客户端ID") @Parameter(description = "客户端ID") 
            @RequestParam String clientId,
            @ApiParam("事件类型列表") @Parameter(description = "要取消订阅的事件类型列表，多个类型用逗号分隔") 
            @RequestParam String eventTypes) {
        
        try {
            log.info("文件系统SSE取消订阅请求: clientId={}, eventTypes={}", clientId, eventTypes);
            fileSystemSseService.unsubscribe(clientId, eventTypes);
            return ReturnResult.success("取消订阅成功");
        } catch (Exception e) {
            log.error("取消订阅文件系统事件失败: clientId={}, eventTypes={}", clientId, eventTypes, e);
            return ReturnResult.error("取消订阅失败: " + e.getMessage());
        }
    }

    /**
     * 断开SSE连接
     *
     * @param clientId 客户端ID
     * @return 断开连接结果
     */
    @ApiOperation("断开SSE连接")
    @Operation(summary = "断开SSE连接", description = "主动断开SSE连接")
    @PostMapping("/disconnect")
    public ReturnResult<String> disconnect(
            @ApiParam("客户端ID") @Parameter(description = "客户端ID") 
            @RequestParam String clientId) {
        
        try {
            log.info("文件系统SSE断开连接请求: clientId={}", clientId);
            fileSystemSseService.disconnect(clientId);
            return ReturnResult.success("断开连接成功");
        } catch (Exception e) {
            log.error("断开文件系统SSE连接失败: clientId={}", clientId, e);
            return ReturnResult.error("断开连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前连接状态
     *
     * @return 连接状态信息
     */
    @ApiOperation("获取连接状态")
    @Operation(summary = "获取连接状态", description = "获取当前SSE连接的状态信息")
    @GetMapping("/status")
    public ReturnResult<Map<String, Object>> getStatus() {
        try {
            Map<String, Object> status = fileSystemSseService.getConnectionStatus();
            return ReturnResult.success(status);
        } catch (Exception e) {
            log.error("获取文件系统SSE连接状态失败", e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    /**
     * 测试发送消息
     * 用于测试SSE连接是否正常工作
     *
     * @param clientId 客户端ID
     * @param message 测试消息内容
     * @return 发送结果
     */
    @ApiOperation("测试发送消息")
    @Operation(summary = "测试发送消息", description = "用于测试SSE连接是否正常工作")
    @PostMapping("/test")
    public ReturnResult<String> testSendMessage(
            @ApiParam("客户端ID") @Parameter(description = "客户端ID") 
            @RequestParam(required = false) String clientId,
            @ApiParam("测试消息") @Parameter(description = "测试消息内容") 
            @RequestBody Map<String, Object> message) {
        
        try {
            log.info("文件系统SSE测试消息: clientId={}, message={}", clientId, Json.toJson(message));
            fileSystemSseService.sendTestMessage(clientId, message);
            return ReturnResult.success("测试消息发送成功");
        } catch (Exception e) {
            log.error("发送文件系统SSE测试消息失败: clientId={}", clientId, e);
            return ReturnResult.error("发送测试消息失败: " + e.getMessage());
        }
    }

    /**
     * 心跳检测
     * 客户端定期调用此接口保持连接活跃
     *
     * @param clientId 客户端ID
     * @return 心跳响应
     */
    @ApiOperation("心跳检测")
    @Operation(summary = "心跳检测", description = "客户端定期调用此接口保持连接活跃")
    @PostMapping("/heartbeat")
    public ReturnResult<String> heartbeat(
            @ApiParam("客户端ID") @Parameter(description = "客户端ID") 
            @RequestParam String clientId) {
        
        try {
            fileSystemSseService.updateHeartbeat(clientId);
            return ReturnResult.success("心跳正常");
        } catch (Exception e) {
            log.error("文件系统SSE心跳检测失败: clientId={}", clientId, e);
            return ReturnResult.error("心跳检测失败: " + e.getMessage());
        }
    }
}
