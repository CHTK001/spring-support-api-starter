package com.chua.starter.system.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.protocol.Protocol;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.pojo.online.OnlineUserVO;
import com.chua.starter.system.service.SysOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在线用户服务实现
 * <p>通过OAuth客户端获取在线用户信息，避免直接操作Redis</p>
 *
 * @author CH
 * @since 2024/12/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOnlineServiceImpl implements SysOnlineService {

    @Override
    public List<OnlineUserVO> getOnlineUsers(String username, String ip) {
        List<OnlineUserVO> onlineUsers = new ArrayList<>();

        try {
            // 通过OAuth客户端获取在线用户列表
            Protocol.OnlineUserResult result = AuthClientExecute.getInstance()
                    .getOnlineUsers(username, ip, 1, 1000);
            
            if (result != null && result.getUsers() != null) {
                for (Protocol.OnlineUserInfo info : result.getUsers()) {
                    onlineUsers.add(convertToOnlineUserVO(info));
                }
            }
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
        }

        // 按登录时间倒序排列
        onlineUsers.sort((a, b) -> {
            if (a.getLoginTime() == null) return 1;
            if (b.getLoginTime() == null) return -1;
            return b.getLoginTime().compareTo(a.getLoginTime());
        });

        return onlineUsers;
    }

    @Override
    public Integer getOnlineCount() {
        try {
            // 通过OAuth客户端获取在线用户数量
            Protocol.OnlineUserResult result = AuthClientExecute.getInstance()
                    .getOnlineUsers(null, null, 1, 1);
            return result != null ? result.getTotal() : 0;
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return 0;
        }
    }

    @Override
    public ReturnResult<Boolean> kickUser(String userId) {
        try {
            // 通过OAuth客户端强制下线用户
            LoginAuthResult result = AuthClientExecute.getInstance().logout(userId, null, LogoutType.KICK);
            if (result != null && result.getCode() == SystemConstant.HTTP_STATUS_OK) {
                log.info("用户 {} 已被强制下线", userId);
                return ReturnResult.ok(true);
            }
            return ReturnResult.error("强制下线失败");
        } catch (Exception e) {
            log.error("强制下线用户失败: userId={}", userId, e);
            return ReturnResult.error("强制下线失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> kickUsers(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return ReturnResult.ok(0);
        }

        AtomicInteger successCount = new AtomicInteger(0);
        for (String userId : userIds) {
            try {
                ReturnResult<Boolean> result = kickUser(userId);
                if (result.isOk() && Boolean.TRUE.equals(result.getData())) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.warn("强制下线用户失败: userId={}", userId, e);
            }
        }

        return ReturnResult.ok(successCount.get());
    }

    /**
     * 将Protocol中的用户信息转换为VO对象
     */
    private OnlineUserVO convertToOnlineUserVO(Protocol.OnlineUserInfo info) {
        OnlineUserVO vo = new OnlineUserVO();
        vo.setUserId(info.getUserId());
        vo.setUsername(info.getUsername());
        vo.setNickname(info.getNickname());
        vo.setLoginIp(info.getLoginIp());
        vo.setLoginAddress(info.getLoginAddress());
        vo.setBrowser(info.getBrowser());
        vo.setOs(info.getOs());
        vo.setLoginType(info.getLoginType());
        vo.setToken(info.getToken());

        // 解析登录时间
        if (info.getLoginTime() != null) {
            vo.setLoginTime(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(info.getLoginTime()),
                    ZoneId.systemDefault()));
        }

        return vo;
    }
}
