package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.system.mapper.SysMessageMapper;
import com.chua.starter.system.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
     * 广播消息接收者ID（表示发送给所有用户）
     */
    private static final int BROADCAST_RECEIVER_ID = 0;

    /**
     * 消息状态：已发送
     */
    private static final int STATUS_SENT = 1;

    @Override
    /** PageForUser */
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
    /** 计算数量Unread */
    public long countUnread(Integer sysUserId) {
        return count(Wrappers.<SysMessage>lambdaQuery()
                .and(w -> w.eq(SysMessage::getSysMessageReceiverId, sysUserId)
                        .or()
                        .eq(SysMessage::getSysMessageReceiverId, BROADCAST_RECEIVER_ID))
                .eq(SysMessage::getSysMessageStatus, STATUS_SENT)
                .eq(SysMessage::getSysMessageRead, 0));
    }

    @Override
    /** 标记读取 */
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
}
