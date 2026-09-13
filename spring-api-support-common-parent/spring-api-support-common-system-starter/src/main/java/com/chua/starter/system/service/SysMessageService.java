package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;
import java.util.Map;

/**
* 系统消息服务接口
*
* <p>提供当前用户站内消息的分页查询、未读数统计与已读标记能力，
* 供登录后的站内消息中心使用。</p>
*
* @since 4.0.0.42
* @author CH
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
    * 分页查询当前用户消息
    *
    * <p>查询条件：接收者为当前用户或广播消息（receiverId=0），按发送时间倒序。</p>
    *
    * @param sysUserId 当前用户标识
    * @param page      页码（从1开始）
    * @param pageSize  每页条数
    * @return 消息分页对象
     */
    IPage<SysMessage> pageForUser(Integer sysUserId, long page, long pageSize);

    /**
    * 统计当前用户未读消息数
    *
    * @param sysUserId 当前用户标识
    * @return 未读数量
     */
    long countUnread(Integer sysUserId);

    /**
    * 标记消息已读
    *
    * <p>lastId 为空时标记全部已读；否则标记 id &lt;= lastId 的消息已读。</p>
    *
    * @param sysUserId 当前用户标识
    * @param lastId    已读到的最大消息标识（可为空）
    * @return 处理结果
     */
    ReturnResult<Boolean> markRead(Integer sysUserId, Long lastId);

    /**
    * 管理员分页查询全部消息
    *
    * @param query     分页参数
    * @param condition 查询条件(标题/类型/状态/接收者)
    * @return 消息分页结果
     */
    IPage<SysMessage> pageForAdmin(Query<SysMessage> query, SysMessage condition);

    /**
    * 管理员发送消息
    *
    * <p>服务端强制设置发送时间与状态为已发送。</p>
    *
    * @param message 消息体(含标题/内容/类型/级别/接收者/跳转链接)
    * @return 发送后的消息
     */
    ReturnResult<SysMessage> sendMessage(SysMessage message);

    /**
    * 撤回消息(状态置为已撤回)
    *
    * @param sysMessageId 消息ID
    * @return 撤回结果
     */
    ReturnResult<Boolean> recallMessage(Long sysMessageId);

    /**
    * 删除消息(支持批量)
    *
    * @param sysMessageIds 消息ID集合
    * @return 删除结果
     */
    ReturnResult<Boolean> deleteMessage(List<Long> sysMessageIds);

    /**
    * 管理员创建并广播消息
    *
    * <p>targetUserIds 为空时创建一条全局广播消息(receiverId=0);
    * 否则为每个目标用户创建一条定向消息。</p>
    *
    * @param message       消息内容
    * @param targetUserIds 目标用户ID列表(为空则广播给所有用户)
    * @return 创建的消息数量
     */
    ReturnResult<Integer> broadcast(SysMessage message, List<Integer> targetUserIds);

    /**
    * 消息统计: 总数/已读/未读
    *
    * @return 统计结果
     */
    Map<String, Object> statistic();
}
