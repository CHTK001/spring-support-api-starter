package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;

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
}
