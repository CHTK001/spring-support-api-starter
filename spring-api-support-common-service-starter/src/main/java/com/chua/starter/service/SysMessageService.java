package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.service.SysMessage;

import java.util.List;

/**
 * 系统消息服务接口
 * 提供消息的增删改查、发送、推送等功能
 *
 * @author CH
 * @since 2024/12/04
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 分页查询消息列表
     *
     * @param query      分页查询对象
     * @param sysMessage 查询条件
     * @return 分页结果
     */
    IPage<SysMessage> pageForMessage(Query<SysMessage> query, SysMessage sysMessage);

    /**
     * 查询用户的消息列表
     *
     * @param userId 用户ID
     * @param read   是否已读，null表示全部
     * @return 消息列表
     */
    List<SysMessage> listByUserId(Integer userId, Integer read);

    /**
     * 查询用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int countUnread(Integer userId);

    /**
     * 发送消息
     * 保存消息并通过WebSocket推送
     *
     * @param message 消息对象
     * @return 发送结果
     */
    ReturnResult<SysMessage> sendMessage(SysMessage message);

    /**
     * 发送系统通知
     *
     * @param title      标题
     * @param content    内容
     * @param receiverId 接收者ID，0表示广播
     * @return 发送结果
     */
    ReturnResult<SysMessage> sendSystemNotice(String title, String content, Integer receiverId);

    /**
     * 发送广播消息（所有人）
     *
     * @param title   标题
     * @param content 内容
     * @param type    消息类型
     * @return 发送结果
     */
    ReturnResult<SysMessage> broadcast(String title, String content, String type);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 操作结果
     */
    ReturnResult<Boolean> markAsRead(Integer messageId, Integer userId);

    /**
     * 批量标记为已读
     *
     * @param messageIds 消息ID列表
     * @param userId     用户ID
     * @return 操作结果
     */
    ReturnResult<Boolean> batchMarkAsRead(List<Integer> messageIds, Integer userId);

    /**
     * 标记用户所有消息为已读
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    ReturnResult<Boolean> markAllAsRead(Integer userId);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId    操作用户ID
     * @return 操作结果
     */
    ReturnResult<Boolean> recall(Integer messageId, Integer userId);

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteMessage(Integer messageId);

    /**
     * 删除用户的消息
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteByUser(Integer messageId, Integer userId);

    /**
     * 清理过期消息
     *
     * @return 清理数量
     */
    int cleanExpiredMessages();

    /**
     * 推送消息到WebSocket
     * 如果启用了Socket.IO，则通过WebSocket推送消息
     *
     * @param message 消息对象
     */
    void pushMessage(SysMessage message);
}
