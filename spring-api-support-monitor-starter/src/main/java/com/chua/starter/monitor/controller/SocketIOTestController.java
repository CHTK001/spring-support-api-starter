package com.chua.starter.monitor.controller;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Socket.IO 测试控制器
 *
 * @author CH
 * @since 2024/12/23
 */
@Api(tags = "Socket.IO测试")
@RestController
@RequestMapping("/socketio")
@Slf4j
@RequiredArgsConstructor
public class SocketIOTestController {

    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 测试发送消息到 gen/server 主题
     */
    @ApiOperation("测试发送消息")
    @PostMapping("/test/send")
    public ReturnResult<String> testSendMessage(
            @ApiParam("消息内容") @RequestBody Map<String, Object> messageData) {

        try {
            // 创建测试消息
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("test_message")
                    .serverId((Integer) messageData.get("serverId"))
                    .data(messageData.get("data"))
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 发送到 gen/server 主题
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server", messageJson);

            log.info("测试消息发送成功: {}", messageJson);
            return ReturnResult.success("消息发送成功");

        } catch (Exception e) {
            log.error("发送测试消息失败: {}", e.getMessage(), e);
            return ReturnResult.error("消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 测试 SSH 连接消息
     */
    @ApiOperation("测试SSH连接消息")
    @PostMapping("/test/ssh-connect")
    public ReturnResult<String> testSSHConnect(
            @ApiParam("服务器ID") @RequestParam Integer serverId) {

        try {
            // 创建 SSH 连接消息
            ServerWebSocketMessage message = ServerWebSocketMessage.createSSHConnectMessage(
                serverId, "测试服务器", "192.168.1.100", 22);

            // 发送到 gen/server 主题
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server", messageJson);

            log.info("SSH连接测试消息发送成功: {}", messageJson);
            return ReturnResult.success("SSH连接消息发送成功");

        } catch (Exception e) {
            log.error("发送SSH连接测试消息失败: {}", e.getMessage(), e);
            return ReturnResult.error("SSH连接消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 测试 SSH 数据消息
     */
    @ApiOperation("测试SSH数据消息")
    @PostMapping("/test/ssh-data")
    public ReturnResult<String> testSSHData(
            @ApiParam("服务器ID") @RequestParam Integer serverId,
            @ApiParam("数据内容") @RequestParam String data) {

        try {
            // 创建 SSH 数据消息
            ServerWebSocketMessage message = ServerWebSocketMessage.createSSHDataMessage(
                serverId, "测试服务器", data);

            // 发送到 gen/server 主题
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server", messageJson);

            log.info("SSH数据测试消息发送成功: {}", messageJson);
            return ReturnResult.success("SSH数据消息发送成功");

        } catch (Exception e) {
            log.error("发送SSH数据测试消息失败: {}", e.getMessage(), e);
            return ReturnResult.error("SSH数据消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取在线会话信息
     */
    @ApiOperation("获取在线会话信息")
    @GetMapping("/sessions")
    public ReturnResult<Map<String, Object>> getOnlineSessions() {

        try {
            // 获取在线会话数量
            var sessions = socketSessionTemplate.getOnlineSession("gen/server");

            Map<String, Object> result = Map.of(
                "topic", "gen/server",
                "sessionCount", sessions.size(),
                "timestamp", System.currentTimeMillis()
            );

            return ReturnResult.success(result);

        } catch (Exception e) {
            log.error("获取在线会话信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取会话信息失败: " + e.getMessage());
        }
    }
}
