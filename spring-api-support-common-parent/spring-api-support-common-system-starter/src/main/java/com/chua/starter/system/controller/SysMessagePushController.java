package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.pojo.message.MessageContentSendRequest;
import com.chua.starter.system.pojo.message.MessageSendRequest;
import com.chua.starter.system.pojo.message.SendByCodeRequest;
import com.chua.starter.system.service.SysMessagePushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息发送接口
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "消息发送接口")
@RestController
@RequestMapping("/v2/message")
@RequiredArgsConstructor
public class SysMessagePushController {

    /**
     * 消息推送服务
     */
    private final SysMessagePushService sysMessagePushService;

    /**
     * 按模板ID发送消息
     */
    @Operation(summary = "按模板ID发送消息")
    @PostMapping("/send")
    @SysLog(module = "发送")
    @Permission(value = "sys:message:send", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<MessageResponse>> send(@RequestBody MessageSendRequest request) {
        return sysMessagePushService.send(request);
    }

    /**
     * 按模板编码发送消息(前端发送弹窗使用)
     */
    @Operation(summary = "按模板编码发送消息")
    @PostMapping("/send/by-code")
    @SysLog(module = "发送")
    @Permission(value = "sys:message:send", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<MessageResponse>> sendByCode(@RequestBody SendByCodeRequest request) {
        return sysMessagePushService.sendByCode(request);
    }

    /**
     * 按内容发送消息(不经模板，用于补发与连通性测试)
     */
    @Operation(summary = "按内容发送消息")
    @PostMapping("/send/content")
    @SysLog(module = "发送")
    @Permission(value = "sys:message:send", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<MessageResponse>> sendContent(@RequestBody MessageContentSendRequest request) {
        return sysMessagePushService.sendContent(request);
    }

    /**
     * 查询某条消息的送达/已读统计
     */
    @Operation(summary = "消息送达/已读统计")
    @PostMapping("/recipient/stats")
    @Permission(value = "sys:message:send", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> recipientStats(@RequestParam Long sysMessageId) {
        return sysMessagePushService.recipientStats(sysMessageId);
    }
}
