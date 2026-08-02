package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.online.OnlineUserVO;

import java.util.List;

/**
 * 在线用户服务接口
 *
 * @author CH
 * @since 2024/12/15
 */
public interface SysOnlineService {

    /**
     * 获取在线用户列表
     *
     * @param username 用户名（可选，模糊匹配）
     * @param ip       IP地址（可选，模糊匹配）
     * @return 在线用户列表
     */
    List<OnlineUserVO> getOnlineUsers(String username, String ip);

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    Integer getOnlineCount();

    /**
     * 强制下线指定用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    ReturnResult<Boolean> kickUser(String userId);

    /**
     * 批量强制下线用户
     *
     * @param userIds 用户ID列表
     * @return 成功下线的用户数量
     */
    ReturnResult<Integer> kickUsers(List<String> userIds);
}
