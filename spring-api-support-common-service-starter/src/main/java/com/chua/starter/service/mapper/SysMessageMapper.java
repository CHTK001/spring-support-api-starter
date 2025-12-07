package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统消息Mapper接口
 *
 * @author CH
 * @since 2024/12/04
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    /**
     * 查询用户的消息列表
     *
     * @param userId 用户ID
     * @param read   是否已读
     * @return 消息列表
     */
    List<SysMessage> selectByUserId(@Param("userId") Integer userId, @Param("read") Integer read);

    /**
     * 统计用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnreadByUserId(@Param("userId") Integer userId);

    /**
     * 批量标记为已读
     *
     * @param messageIds 消息ID列表
     * @param userId     用户ID
     * @return 更新数量
     */
    int batchMarkAsRead(@Param("messageIds") List<Integer> messageIds, @Param("userId") Integer userId);

    /**
     * 标记用户所有消息为已读
     *
     * @param userId 用户ID
     * @return 更新数量
     */
    int markAllAsReadByUserId(@Param("userId") Integer userId);

    /**
     * 删除过期消息
     *
     * @return 删除数量
     */
    int deleteExpiredMessages();
}
