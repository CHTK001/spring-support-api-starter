package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.SysMessageHistoryService;
import com.chua.starter.service.SysMessageService;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.entity.service.SysMessageHistory;
import com.chua.starter.service.configuration.MessageCenterSettingInitializer;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统消息接口
 * 提供消息的查询、标记已读、删除等功能
 *
 * @author CH
 * @since 2024/12/04
 */
@Tag(name = "系统消息接口")
@RestController
@RequestMapping("/v2/message")
@RequiredArgsConstructor
public class SysMessageController {
    private static final int DEFAULT_PREVIEW_LIMIT = 5;
    private static final int MAX_PREVIEW_LIMIT = 20;

    private final SysMessageService sysMessageService;
    private final SysMessageHistoryService sysMessageHistoryService;
    private final SysSettingService sysSettingService;

    /**
     * 获取未读消息列表
     *
     * @param userId 当前用户ID
     * @return 未读消息列表
     */
    @Operation(summary = "获取未读消息列表")
    @GetMapping("/unread")
    public ReturnResult<List<SysMessage>> listUnread(
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        if (userId == null) {
            return ReturnResult.ok(Collections.emptyList());
        }
        List<SysMessage> unreadMessages = sysMessageService.listByUserId(userId, 0);
        int previewLimit = resolvePreviewLimit();
        if (unreadMessages.size() >= previewLimit) {
            return ReturnResult.ok(unreadMessages);
        }

        List<SysMessage> previewMessages = new ArrayList<>(unreadMessages);
        int needSupplement = previewLimit - unreadMessages.size();
        if (needSupplement <= 0) {
            return ReturnResult.ok(previewMessages);
        }

        List<SysMessageHistory> historyMessages = sysMessageHistoryService.listByUserId(userId);
        historyMessages.stream()
                .filter(Objects::nonNull)
                .limit(needSupplement)
                .map(this::convertHistoryToPreviewMessage)
                .forEach(previewMessages::add);
        return ReturnResult.ok(previewMessages);
    }

    private int resolvePreviewLimit() {
        try {
            List<com.chua.starter.entity.service.SysSetting> settings = sysSettingService.list(
                    new SysSettingUpdate(MessageCenterSettingInitializer.GROUP_CODE),
                    Collections.emptySet()
            );
            return settings.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> MessageCenterSettingInitializer.PREVIEW_LIMIT_NAME.equals(item.getSysSettingName()))
                    .map(item -> item.getSysSettingValue())
                    .findFirst()
                    .map(this::normalizePreviewLimit)
                    .orElse(DEFAULT_PREVIEW_LIMIT);
        } catch (Exception ignored) {
            return DEFAULT_PREVIEW_LIMIT;
        }
    }

    private int normalizePreviewLimit(String rawValue) {
        try {
            int parsed = Integer.parseInt(String.valueOf(rawValue).trim());
            if (parsed <= 0) {
                return DEFAULT_PREVIEW_LIMIT;
            }
            return Math.min(parsed, MAX_PREVIEW_LIMIT);
        } catch (Exception ignored) {
            return DEFAULT_PREVIEW_LIMIT;
        }
    }

    private SysMessage convertHistoryToPreviewMessage(SysMessageHistory history) {
        SysMessage message = new SysMessage();
        Integer messageId = history.getSysMessageHistoryMessageId();
        if (messageId == null) {
            messageId = -Math.abs(history.getSysMessageHistoryId());
        }
        message.setSysMessageId(messageId);
        message.setSysMessageTitle(history.getSysMessageHistoryTitle());
        message.setSysMessageContent(history.getSysMessageHistoryContent());
        message.setSysMessageType(history.getSysMessageHistoryType());
        message.setSysMessageLevel(history.getSysMessageHistoryLevel());
        message.setSysMessageSenderId(history.getSysMessageHistorySenderId());
        message.setSysMessageSenderName(history.getSysMessageHistorySenderName());
        message.setSysMessageReceiverId(history.getSysMessageHistoryReceiverId());
        message.setSysMessageReceiverName(history.getSysMessageHistoryReceiverName());
        message.setSysMessageRead(1);
        message.setSysMessageReadTime(history.getSysMessageHistoryReadTime());
        message.setSysMessageSendTime(
                history.getSysMessageHistoryReadTime() != null
                        ? history.getSysMessageHistoryReadTime()
                        : history.getSysMessageHistorySendTime()
        );
        message.setSysMessageBizType(history.getSysMessageHistoryBizType());
        message.setSysMessageBizId(history.getSysMessageHistoryBizId());
        message.setSysMessageUrl(history.getSysMessageHistoryUrl());
        message.setSysMessageExtra(history.getSysMessageHistoryExtra());
        message.setSysMessageStatus(SysMessage.Status.SENT);
        return message;
    }

    /**
     * 获取历史消息分页列表
     *
     * @param query  分页参数
     * @param userId 当前用户ID
     * @return 历史消息分页列表
     */
    @Operation(summary = "获取历史消息分页列表")
    @GetMapping("/history")
    public ReturnPageResult<SysMessageHistory> pageHistory(
            @ParameterObject Query<SysMessageHistory> query,
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        SysMessageHistory history = new SysMessageHistory();
        history.setSysMessageHistoryReceiverId(userId);
        return PageResultUtils.ok(sysMessageHistoryService.pageForHistory(query, history));
    }

    /**
     * 获取未读消息数量
     *
     * @param userId 当前用户ID
     * @return 未读消息数量
     */
    @Operation(summary = "获取未读消息数量")
    @GetMapping("/count")
    public ReturnResult<Integer> countUnread(
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        int count = sysMessageService.countUnread(userId);
        return ReturnResult.ok(count);
    }

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId    当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "标记消息为已读")
    @PostMapping("/read/{messageId}")
    public ReturnResult<Boolean> markAsRead(
            @PathVariable Integer messageId,
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return sysMessageService.markAsRead(messageId, userId);
    }

    /**
     * 批量标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @param userId     当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "批量标记消息为已读")
    @PostMapping("/read/batch")
    public ReturnResult<Boolean> batchMarkAsRead(
            @RequestBody List<Integer> messageIds,
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return sysMessageService.batchMarkAsRead(messageIds, userId);
    }

    /**
     * 标记所有消息为已读
     *
     * @param userId 当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "标记所有消息为已读")
    @PostMapping("/read/all")
    public ReturnResult<Boolean> markAllAsRead(
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return sysMessageService.markAllAsRead(userId);
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @param userId    当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除消息")
    @DeleteMapping("/{messageId}")
    public ReturnResult<Boolean> deleteMessage(
            @PathVariable Integer messageId,
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        return sysMessageService.deleteByUser(messageId, userId);
    }

    /**
     * 删除历史消息
     *
     * @param historyId 历史记录ID
     * @param userId    当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除历史消息")
    @DeleteMapping("/history/{historyId}")
    public ReturnResult<Boolean> deleteHistory(
            @PathVariable Integer historyId,
            @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        SysMessageHistory history = sysMessageHistoryService.getById(historyId);
        if (history == null) {
            return ReturnResult.ok(true);
        }
        // 检查权限
        if (history.getSysMessageHistoryReceiverId() != 0 &&
                !history.getSysMessageHistoryReceiverId().equals(userId)) {
            return ReturnResult.error("无权删除此消息");
        }
        return ReturnResult.ok(sysMessageHistoryService.removeById(historyId));
    }
}
