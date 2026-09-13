package com.chua.starter.system.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.system.pojo.message.MessageBroadcastRequest;
import com.chua.starter.system.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* 站内消息管理端接口
*
* <p>管理员查询、发送(广播/定向)、删除消息及消息统计。</p>
*
* @since 4.0.0.43
* @author CH
 */
@Tag(name = "站内消息管理接口")
@RestController
@RequestMapping("/v2/message/admin")
@RequiredArgsConstructor
public class SysMessageAdminController {

    /** SYS消息服务 */
    private final SysMessageService sysMessageService;

    /**
    * 管理员分页查询全部消息
    *
    * @param query     分页参数
    * @param condition 查询条件
    * @return 消息分页
     */
    @Operation(summary = "消息分页列表(管理端)")
    @GetMapping("/page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysMessage> page(@ParameterObject Query<SysMessage> query,
                                             @ParameterObject SysMessage condition) {
        return PageResultUtils.ok(sysMessageService.pageForAdmin(query, condition));
    }

    /**
    * 管理员创建并广播/定向发送消息
    *
    * @param request 请求体
    * @return 创建的消息数量
     */
    @Operation(summary = "发送消息(管理端)")
    @PostMapping("/save")
    @Permission(value = "sys:message:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Integer> save(@RequestBody MessageBroadcastRequest request) {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle(request.getTitle());
        message.setSysMessageContent(request.getContent());
        message.setSysMessageType(request.getType());
        message.setSysMessageLevel(request.getLevel());
        message.setSysMessageUrl(request.getUrl());
        message.setSysMessageSenderId(0);
        message.setSysMessageSenderName("系统");
        return sysMessageService.broadcast(message, request.getTargetUserIds());
    }

    /**
    * 删除消息
    *
    * @param sysMessageId 消息ID
    * @return 删除结果
     */
    @Operation(summary = "删除消息(管理端)")
    @DeleteMapping("/delete")
    @Permission(value = "sys:message:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(@RequestParam Integer sysMessageId) {
        if (null == sysMessageId) {
            return ReturnResult.illegal("消息ID不能为空");
        }
        return sysMessageService.deleteMessage(Collections.singletonList(sysMessageId.longValue()));
    }

    /**
    * 消息统计
    *
    * @return 统计结果(总数/已读/未读)
     */
    @Operation(summary = "消息统计(管理端)")
    @GetMapping("/statistic")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> statistic() {
        return ReturnResult.ok(sysMessageService.statistic());
    }
}
