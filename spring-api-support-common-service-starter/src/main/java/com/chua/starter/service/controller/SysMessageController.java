package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.service.SysMessageHistoryService;
import com.chua.starter.service.SysMessageService;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.entity.service.SysMessageHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private final SysMessageService sysMessageService;
    private final SysMessageHistoryService sysMessageHistoryService;

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
        List<SysMessage> messages = sysMessageService.listByUserId(userId, 0);
        return ReturnResult.ok(messages);
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
