package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.system.pojo.message.SysMessageVO;
import com.chua.starter.system.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 站内消息接口
 *
 * <p>提供当前用户站内消息的分页查询、未读统计与已读标记，
 * 供前端站内消息中心（头部 铃铛/消息面板）调用。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Tag(name = "站内消息接口")
@RestController
@RequestMapping("/v2/message/sys-message")
@RequiredArgsConstructor
public class SysMessageController {

    /** SYS消息服务 */
    private final SysMessageService sysMessageService;

    /** 消息模板服务 */
    private final com.chua.starter.system.service.SysMessageTemplateService sysMessageTemplateService;

    /**
     * 分页查询当前用户消息
     *
     * @param sysUserId 当前用户标识（自动注入）
     * @param page      页码
     * @param pageSize  每页条数
     * @return 消息分页结果（data 为消息列表，total 为总数）
     */
    @Operation(summary = "站内消息分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysMessageVO> page(
            @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        IPage<SysMessage> pager = sysMessageService.pageForUser(sysUserId == null ? 0 : sysUserId, page, pageSize);
        IPage<SysMessageVO> voPage = pager.convert(this::toVO);
        return PageResultUtils.ok(voPage);
    }

    /**
     * 统计当前用户未读消息数
     *
     * @param sysUserId 当前用户标识（自动注入）
     * @return 未读数量
     */
    @Operation(summary = "站内消息未读数")
    @GetMapping("/unread-count")
    public ReturnResult<Long> unreadCount(
            @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return ReturnResult.ok(sysMessageService.countUnread(sysUserId == null ? 0 : sysUserId));
    }

    /**
     * 标记消息已读
     *
     * @param sysUserId 当前用户标识（自动注入）
     * @param lastId    已读到的最大消息标识（为空标记全部已读）
     * @return 处理结果
     */
    @Operation(summary = "标记站内消息已读")
    @PutMapping("/read")
    public ReturnResult<Boolean> markRead(
            @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
            @RequestParam(required = false) Long lastId) {
        return sysMessageService.markRead(sysUserId == null ? 0 : sysUserId, lastId);
    }

    /**
     * 管理员分页查询全部消息
     *
     * @param query     分页参数
     * @param condition 查询条件(标题/类型/状态/接收者)
     * @return 消息分页结果
     */
    @Operation(summary = "管理员-消息分页列表")
    @GetMapping("/admin/page")
    @Permission(value = "sys:message:page", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysMessage> adminPage(@ParameterObject Query<SysMessage> query,
                                                  @ParameterObject SysMessage condition) {
        return PageResultUtils.ok(sysMessageService.pageForAdmin(query, condition));
    }

    /**
     * 管理员发送消息
     *
     * @param sysUserId 当前管理员用户标识(自动注入)
     * @param message   消息体(标题/内容/类型/级别/接收者/跳转链接)
     * @return 发送后的消息
     */
    @Operation(summary = "管理员-发送消息")
    @PostMapping("/admin/send")
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:message:send", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysMessage> adminSend(
            @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
            @RequestParam(required = false) String templateCode,
            @RequestBody SysMessage message) {
        message.setSysMessageSenderId(sysUserId);
        // 若指定模板编码, 从模板渲染标题/内容并替换变量
        if (null != templateCode && !templateCode.trim().isEmpty()) {
            java.util.Map<String, String> variables = new java.util.HashMap<>();
            variables.put("username", message.getSysMessageReceiverName() == null ? "" : message.getSysMessageReceiverName());
            variables.put("date", java.time.LocalDate.now().toString());
            String[] rendered = sysMessageTemplateService.renderByCode(templateCode.trim(), variables);
            if (null != rendered) {
                if (null == message.getSysMessageTitle()) {
                    message.setSysMessageTitle(rendered[0]);
                }
                if (null == message.getSysMessageContent()) {
                    message.setSysMessageContent(rendered[1]);
                }
            }
        }
        return sysMessageService.sendMessage(message);
    }

    /**
     * 管理员撤回消息
     *
     * @param sysMessageId 消息ID
     * @return 撤回结果
     */
    @Operation(summary = "管理员-撤回消息")
    @PutMapping("/admin/recall")
    @SysLog
    @Permission(value = "sys:message:recall", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> adminRecall(@RequestParam Long sysMessageId) {
        return sysMessageService.recallMessage(sysMessageId);
    }

    /**
     * 管理员删除消息(支持批量, 多个ID用逗号分隔)
     *
     * @param sysMessageId 消息ID, 支持 "1,2,3" 逗号分隔
     * @return 删除结果
     */
    @Operation(summary = "管理员-删除消息")
    @DeleteMapping("/admin/delete")
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:message:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> adminDelete(@RequestParam String sysMessageId) {
        if (null == sysMessageId || sysMessageId.trim().isEmpty()) {
            return ReturnResult.illegal("消息ID不能为空");
        }
        List<Long> ids = Arrays.stream(sysMessageId.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
        return sysMessageService.deleteMessage(ids);
    }

    /**
     * 消息实体转前端视图
     *
     * @param message 消息实体
     * @return 前端视图对象
     */
    private SysMessageVO toVO(SysMessage message) {
        SysMessageVO vo = new SysMessageVO();
        vo.setId(message.getSysMessageId() == null ? null : message.getSysMessageId().longValue());
        vo.setSubject(message.getSysMessageTitle());
        vo.setContent(message.getSysMessageContent());
        vo.setSendTime(message.getSysMessageSendTime());
        vo.setReadStatus(message.getSysMessageRead());
        vo.setType(message.getSysMessageType());
        vo.setLevel(message.getSysMessageLevel());
        vo.setUrl(message.getSysMessageUrl());
        return vo;
    }
}
