package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.system.pojo.message.SysMessageVO;
import com.chua.starter.system.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内消息接口
 *
 * <p>提供当前用户站内消息的分页查询、未读统计与已读标记，
 * 供前端站内消息中心（Header 铃铛/消息面板）调用。</p>
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

    /**
     * 分页查询当前用户消息
     *
     * @param sysUserId 当前用户ID（自动注入）
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
     * @param sysUserId 当前用户ID（自动注入）
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
     * @param sysUserId 当前用户ID（自动注入）
     * @param lastId    已读到的最大消息ID（为空标记全部已读）
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
