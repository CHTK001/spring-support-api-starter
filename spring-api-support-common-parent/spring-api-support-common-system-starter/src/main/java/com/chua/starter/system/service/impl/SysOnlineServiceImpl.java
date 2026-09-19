package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.protocol.Protocol;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
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
import java.util.stream.Collectors;

/**
 * 在线用户服务实现
 * <p>通过OAuth客户端获取在线用户信息，避免直接操作Redis</p>
 *
 * @since 2024/12/15
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOnlineServiceImpl implements SysOnlineService {

    /**
     * SYS用户 Mapper(用于按部门/角色过滤在线用户)
    */
    private final SysUserMapper sysUserMapper;

    /**
     * SYS用户角色 Mapper
    */
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    /**
     * 获取online用户
    */
    public List<OnlineUserVO> getOnlineUsers(String username, String ip, String roleCode, Integer deptId) {
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

        // 按角色编码过滤
        if (null != roleCode && !roleCode.trim().isEmpty()) {
            onlineUsers = onlineUsers.stream()
                    .filter(vo -> matchRole(vo.getUserId(), roleCode.trim()))
                    .collect(Collectors.toList());
        }
        // 按部门过滤
        if (null != deptId) {
            onlineUsers = onlineUsers.stream()
                    .filter(vo -> matchDept(vo.getUserId(), deptId))
                    .collect(Collectors.toList());
        }

        // 按登录时间倒序排列
        onlineUsers.sort((a, b) -> {
            if (a.getLoginTime() == null) {
                return 1;
            }
            if (b.getLoginTime() == null) {
                return -1;
            }
            return b.getLoginTime().compareTo(a.getLoginTime());
        });

        return onlineUsers;
    }

    /**
     * 判断用户是否拥有指定角色编码
     * @param userId 用户ID，不允许为 null
     * @param roleCode role编码，不允许为 null
     * @return 是否成功（true 表示成功）
     */
    private boolean matchRole(String userId, String roleCode) {
        Integer uid = parseUserId(userId);
        if (null == uid) {
            return false;
        }
        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(uid);
        return roleCodes != null && roleCodes.contains(roleCode);
    }

    /**
     * 判断用户是否属于指定部门
     * @param userId 用户ID，不允许为 null
     * @param deptId deptID，不允许为 null
     * @return 是否成功（true 表示成功）
     */
    private boolean matchDept(String userId, Integer deptId) {
        Integer uid = parseUserId(userId);
        if (null == uid) {
            return false;
        }
        SysUser user = sysUserMapper.selectById(uid);
        return user != null && deptId.equals(user.getSysDeptId());
    }

    /**
     * 解析用户ID
     * @param userId 用户ID，不允许为 null
     * @return Integer 对象
     */
    private Integer parseUserId(String userId) {
        try {
            return Integer.parseInt(userId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    /**
     * 获取Online计算数量
    */
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
    /**
     * kick用户
    */
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
    /**
     * kick用户
    */
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
     * 将协议中的用户信息转换为VO对象
     * @param info 信息
     * @return 转换转为online用户vo的结果
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

        // 解析最后访问时间
        if (info.getLastAccessTime() != null) {
            vo.setLastAccessTime(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(info.getLastAccessTime()),
                    ZoneId.systemDefault()));
        }

        return vo;
    }
}
