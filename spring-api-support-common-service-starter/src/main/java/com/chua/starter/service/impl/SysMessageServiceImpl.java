package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysMessageHistoryService;
import com.chua.starter.service.SysMessageService;
import com.chua.starter.service.mapper.SysMessageMapper;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.entity.service.SysMessageHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息服务实现类
 * 未读消息存储在sys_message表，读取后转入历史记录并删除
 *
 * @author CH
 * @since 2024/12/04
 */
@Slf4j
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Autowired(required = false)
    private SysMessageHistoryService messageHistoryService;

    @Autowired(required = false)
    private SysMessagePushService pushService;

    @Override
    public IPage<SysMessage> pageForMessage(Query<SysMessage> query, SysMessage sysMessage) {
        return this.page(query.createFullPage(), Wrappers.<SysMessage>lambdaQuery()
                .like(StringUtils.isNotBlank(sysMessage.getSysMessageTitle()), 
                        SysMessage::getSysMessageTitle, sysMessage.getSysMessageTitle())
                .eq(StringUtils.isNotBlank(sysMessage.getSysMessageType()), 
                        SysMessage::getSysMessageType, sysMessage.getSysMessageType())
                .eq(sysMessage.getSysMessageStatus() != null, 
                        SysMessage::getSysMessageStatus, sysMessage.getSysMessageStatus())
                .and(sysMessage.getSysMessageReceiverId() != null, w -> w
                        .eq(SysMessage::getSysMessageReceiverId, sysMessage.getSysMessageReceiverId())
                        .or().eq(SysMessage::getSysMessageReceiverId, 0)));
    }

    @Override
    public List<SysMessage> listByUserId(Integer userId, Integer read) {
        return baseMapper.selectByUserId(userId, read);
    }

    @Override
    public int countUnread(Integer userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysMessage> sendMessage(SysMessage message) {
        try {
            // 设置默认值
            if (message.getSysMessageSendTime() == null) {
                message.setSysMessageSendTime(LocalDateTime.now());
            }
            if (message.getSysMessageStatus() == null) {
                message.setSysMessageStatus(SysMessage.Status.SENT);
            }
            if (message.getSysMessageRead() == null) {
                message.setSysMessageRead(0);
            }
            if (StringUtils.isBlank(message.getSysMessageLevel())) {
                message.setSysMessageLevel(SysMessage.Level.NORMAL);
            }
            if (StringUtils.isBlank(message.getSysMessageType())) {
                message.setSysMessageType(SysMessage.Type.SYSTEM);
            }

            // 保存消息
            this.save(message);

            // 推送消息
            pushMessage(message);

            log.info("消息发送成功: messageId={}, receiverId={}", 
                    message.getSysMessageId(), message.getSysMessageReceiverId());
            return ReturnResult.ok(message);
        } catch (Exception e) {
            log.error("消息发送失败: {}", e.getMessage(), e);
            return ReturnResult.error("消息发送失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<SysMessage> sendSystemNotice(String title, String content, Integer receiverId) {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle(title);
        message.setSysMessageContent(content);
        message.setSysMessageType(SysMessage.Type.SYSTEM);
        message.setSysMessageLevel(SysMessage.Level.NORMAL);
        message.setSysMessageSenderId(0);
        message.setSysMessageSenderName("系统");
        message.setSysMessageReceiverId(receiverId != null ? receiverId : 0);
        return sendMessage(message);
    }

    @Override
    public ReturnResult<SysMessage> broadcast(String title, String content, String type) {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle(title);
        message.setSysMessageContent(content);
        message.setSysMessageType(StringUtils.isNotBlank(type) ? type : SysMessage.Type.NOTICE);
        message.setSysMessageLevel(SysMessage.Level.NORMAL);
        message.setSysMessageSenderId(0);
        message.setSysMessageSenderName("系统");
        message.setSysMessageReceiverId(0);
        return sendMessage(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> markAsRead(Integer messageId, Integer userId) {
        try {
            SysMessage message = this.getById(messageId);
            if (message == null) {
                return ReturnResult.error("消息不存在");
            }

            // 检查权限
            if (message.getSysMessageReceiverId() != 0 && 
                    !message.getSysMessageReceiverId().equals(userId)) {
                return ReturnResult.error("无权操作此消息");
            }

            // 转入历史记录
            SysMessageHistory history = SysMessageHistory.fromMessage(message);
            history.setSysMessageHistoryReadTime(LocalDateTime.now());
            if (messageHistoryService != null) {
                messageHistoryService.save(history);
            }

            // 删除原消息
            this.removeById(messageId);

            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("标记已读失败: messageId={}, userId={}", messageId, userId, e);
            return ReturnResult.error("标记已读失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchMarkAsRead(List<Integer> messageIds, Integer userId) {
        try {
            if (messageIds == null || messageIds.isEmpty()) {
                return ReturnResult.ok(true);
            }
            baseMapper.batchMarkAsRead(messageIds, userId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("批量标记已读失败: userId={}", userId, e);
            return ReturnResult.error("批量标记已读失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> markAllAsRead(Integer userId) {
        try {
            baseMapper.markAllAsReadByUserId(userId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("全部标记已读失败: userId={}", userId, e);
            return ReturnResult.error("全部标记已读失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> recall(Integer messageId, Integer userId) {
        try {
            SysMessage message = this.getById(messageId);
            if (message == null) {
                return ReturnResult.error("消息不存在");
            }

            // 检查权限：只能撤回自己发送的消息
            if (!message.getSysMessageSenderId().equals(userId)) {
                return ReturnResult.error("无权撤回此消息");
            }

            message.setSysMessageStatus(SysMessage.Status.RECALLED);
            this.updateById(message);

            // 推送撤回通知
            pushRecallNotice(message);

            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("撤回消息失败: messageId={}", messageId, e);
            return ReturnResult.error("撤回消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteMessage(Integer messageId) {
        try {
            this.removeById(messageId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("删除消息失败: messageId={}", messageId, e);
            return ReturnResult.error("删除消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteByUser(Integer messageId, Integer userId) {
        try {
            SysMessage message = this.getById(messageId);
            if (message == null) {
                return ReturnResult.ok(true);
            }

            // 检查权限
            if (message.getSysMessageReceiverId() != 0 && 
                    !message.getSysMessageReceiverId().equals(userId)) {
                return ReturnResult.error("无权删除此消息");
            }

            this.removeById(messageId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("删除消息失败: messageId={}, userId={}", messageId, userId, e);
            return ReturnResult.error("删除消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredMessages() {
        return baseMapper.deleteExpiredMessages();
    }

    @Override
    public void pushMessage(SysMessage message) {
        if (pushService == null) {
            log.debug("Socket.IO未启用，跳过消息推送");
            return;
        }
        pushService.pushMessage(message);
    }

    /**
     * 推送撤回通知
     *
     * @param message 消息对象
     */
    private void pushRecallNotice(SysMessage message) {
        if (pushService == null) {
            return;
        }
        pushService.pushRecallNotice(message);
    }
}
