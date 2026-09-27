package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysMessageMapper;
import com.chua.starter.system.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息服务实现
 *
 * <p>查询接收者为当前用户或广播消息（receiverId=0）的站内消息，
 * 支持分页、未读统计与批量已读标记。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    /**
     * 广播消息接收者标识（表示发送给所有用户）
     */
    private static final int BROADCAST_RECEIVER_ID = 0;

    /**
     * 消息状态：已发送
     */
    private static final int STATUS_SENT = 1;

    @Override
    /**
     * pagefor用户
    */
    public IPage<SysMessage> pageForUser(Integer sysUserId, long page, long pageSize) {
        Page<SysMessage> pager = new Page<>(Math.max(page, 1), Math.max(pageSize, 1));
        return page(pager, Wrappers.<SysMessage>lambdaQuery()
                .and(w -> w.eq(SysMessage::getSysMessageReceiverId, sysUserId)
                        .or()
                        .eq(SysMessage::getSysMessageReceiverId, BROADCAST_RECEIVER_ID))
                .eq(SysMessage::getSysMessageStatus, STATUS_SENT)
                .orderByDesc(SysMessage::getSysMessageSendTime)
                .orderByDesc(SysMessage::getSysMessageId));
    }

    @Override
    /**
     * 计算数量Unread
    */
    public long countUnread(Integer sysUserId) {
        return count(Wrappers.<SysMessage>lambdaQuery()
                .and(w -> w.eq(SysMessage::getSysMessageReceiverId, sysUserId)
                        .or()
                        .eq(SysMessage::getSysMessageReceiverId, BROADCAST_RECEIVER_ID))
                .eq(SysMessage::getSysMessageStatus, STATUS_SENT)
                .eq(SysMessage::getSysMessageRead, 0));
    }

    @Override
    /**
     * 标记读取
    */
    public ReturnResult<Boolean> markRead(Integer sysUserId, Long lastId) {
        SysMessage update = new SysMessage();
        update.setSysMessageRead(1);
        update.setSysMessageReadTime(LocalDateTime.now());
        if (lastId == null || lastId <= 0) {
            update(update, Wrappers.<SysMessage>lambdaUpdate()
                    .and(w -> w.eq(SysMessage::getSysMessageReceiverId, sysUserId)
                            .or()
                            .eq(SysMessage::getSysMessageReceiverId, BROADCAST_RECEIVER_ID))
                    .eq(SysMessage::getSysMessageRead, 0));
            return ReturnResult.ok(true);
        }
        update(update, Wrappers.<SysMessage>lambdaUpdate()
                .and(w -> w.eq(SysMessage::getSysMessageReceiverId, sysUserId)
                        .or()
                        .eq(SysMessage::getSysMessageReceiverId, BROADCAST_RECEIVER_ID))
                .le(SysMessage::getSysMessageId, lastId)
                .eq(SysMessage::getSysMessageRead, 0));
        return ReturnResult.ok(true);
    }

    @Override
    /**
     * 管理员分页
    */
    public IPage<SysMessage> pageForAdmin(Query<SysMessage> query, SysMessage condition) {
        SysMessage c = condition == null ? new SysMessage() : condition;
        return page(query.createPage(), Wrappers.<SysMessage>lambdaQuery()
                .like(StringUtils.isNotBlank(c.getSysMessageTitle()), SysMessage::getSysMessageTitle, c.getSysMessageTitle())
                .eq(StringUtils.isNotBlank(c.getSysMessageType()), SysMessage::getSysMessageType, c.getSysMessageType())
                .eq(null != c.getSysMessageStatus(), SysMessage::getSysMessageStatus, c.getSysMessageStatus())
                .eq(null != c.getSysMessageReceiverId(), SysMessage::getSysMessageReceiverId, c.getSysMessageReceiverId())
                .eq(StringUtils.isNotBlank(c.getSysMessageChannel()), SysMessage::getSysMessageChannel, c.getSysMessageChannel())
                .orderByDesc(SysMessage::getSysMessageSendTime)
                .orderByDesc(SysMessage::getSysMessageId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 发送消息
    */
    public ReturnResult<SysMessage> sendMessage(SysMessage message) {
        if (message == null || StringUtils.isBlank(message.getSysMessageTitle())) {
            return ReturnResult.illegal("消息标题不能为空");
        }
        // 接收者为空时默认广播
        if (null == message.getSysMessageReceiverId()) {
            message.setSysMessageReceiverId(BROADCAST_RECEIVER_ID);
        }
        // 已读状态默认未读
        if (null == message.getSysMessageRead()) {
            message.setSysMessageRead(0);
        }
        message.setSysMessageId(null);
        message.setSysMessageSendTime(LocalDateTime.now());
        message.setSysMessageStatus(SysMessage.Status.SENT);
        if (baseMapper.insert(message) > 0) {
            return ReturnResult.ok(message);
        }
        return ReturnResult.error("发送消息失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 撤回消息
    */
    public ReturnResult<Boolean> recallMessage(Long sysMessageId) {
        if (null == sysMessageId) {
            return ReturnResult.illegal("消息ID不能为空");
        }
        SysMessage update = new SysMessage();
        update.setSysMessageId(sysMessageId.intValue());
        update.setSysMessageStatus(SysMessage.Status.RECALLED);
        return ReturnResult.of(baseMapper.updateById(update) > 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除消息
    */
    public ReturnResult<Boolean> deleteMessage(List<Long> sysMessageIds) {
        if (sysMessageIds == null || sysMessageIds.isEmpty()) {
            return ReturnResult.illegal("消息ID不能为空");
        }
        return ReturnResult.of(baseMapper.deleteBatchIds(sysMessageIds) > 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 广播消息
    */
    public ReturnResult<Integer> broadcast(SysMessage message, List<Integer> targetUserIds) {
        if (message == null || StringUtils.isBlank(message.getSysMessageTitle())) {
            return ReturnResult.illegal("消息标题不能为空");
        }
        message.setSysMessageId(null);
        message.setSysMessageSendTime(LocalDateTime.now());
        message.setSysMessageStatus(SysMessage.Status.SENT);
        if (null == message.getSysMessageRead()) {
            message.setSysMessageRead(0);
        }
        int count = 0;
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            // 全局广播
            message.setSysMessageReceiverId(BROADCAST_RECEIVER_ID);
            baseMapper.insert(message);
            count = 1;
        } else {
            // 定向发送给指定用户
            for (Integer userId : targetUserIds) {
                if (null == userId) {
                    continue;
                }
                SysMessage row = copyForReceiver(message, userId);
                baseMapper.insert(row);
                count++;
            }
        }
        if (count == 0) {
            return ReturnResult.error("发送消息失败");
        }
        return ReturnResult.ok(count);
    }

    @Override
    /**
     * 消息统计
    */
    public java.util.Map<String, Object> statistic() {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        long total = count();
        long read = count(Wrappers.<SysMessage>lambdaQuery().eq(SysMessage::getSysMessageRead, 1));
        long unread = count(Wrappers.<SysMessage>lambdaQuery().eq(SysMessage::getSysMessageRead, 0));
        result.put("total", total);
        result.put("read", read);
        result.put("unread", unread);
        return result;
    }

    /**
     * 复制消息体并设置指定接收者
     *
     * @param source 源消息
     * @param receiverId 接收者ID
     * @return 新消息行
     */
    private SysMessage copyForReceiver(SysMessage source, Integer receiverId) {
        SysMessage row = new SysMessage();
        row.setSysMessageTitle(source.getSysMessageTitle());
        row.setSysMessageContent(source.getSysMessageContent());
        row.setSysMessageType(source.getSysMessageType());
        row.setSysMessageLevel(source.getSysMessageLevel());
        row.setSysMessageSenderId(source.getSysMessageSenderId());
        row.setSysMessageSenderName(source.getSysMessageSenderName());
        row.setSysMessageReceiverId(receiverId);
        row.setSysMessageRead(0);
        row.setSysMessageSendTime(source.getSysMessageSendTime());
        row.setSysMessageExpireTime(source.getSysMessageExpireTime());
        row.setSysMessageBizType(source.getSysMessageBizType());
        row.setSysMessageBizId(source.getSysMessageBizId());
        row.setSysMessageUrl(source.getSysMessageUrl());
        row.setSysMessageExtra(source.getSysMessageExtra());
        row.setSysMessageStatus(SysMessage.Status.SENT);
        return row;
    }
}
