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
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.system.pojo.online.OnlineLoginDayStat;
import com.chua.starter.system.pojo.online.OnlineLoginDayVO;
import com.chua.starter.system.pojo.online.OnlineLoginHistoryVO;
import com.chua.starter.system.pojo.online.OnlineLoginIpStat;
import com.chua.starter.system.pojo.online.OnlineLoginIpVO;
import com.chua.starter.system.pojo.online.OnlineUserVO;
import com.chua.starter.system.service.SysOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * 登录时间轴默认窗口天数
    */
    private static final int DEFAULT_LOGIN_WINDOW_DAYS = 30;

    /**
     * 登录时间轴窗口天数上限
    */
    private static final int MAX_LOGIN_WINDOW_DAYS = 90;

    /**
     * SYS用户 Mapper(用于按部门/角色过滤在线用户)
    */
    private final SysUserMapper sysUserMapper;

    /**
     * SYS用户角色 Mapper
    */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * SYS用户日志 Mapper(用于聚合登录 IP 与最近 30 天登录时间轴)
    */
    private final SysUserLogMapper sysUserLogMapper;

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
                    .filter(vo -> matchRole(vo.userId(), roleCode.trim()))
                    .collect(Collectors.toList());
        }
        // 按部门过滤
        if (null != deptId) {
            onlineUsers = onlineUsers.stream()
                    .filter(vo -> matchDept(vo.userId(), deptId))
                    .collect(Collectors.toList());
        }

        // 按登录时间倒序排列
        onlineUsers.sort((a, b) -> {
            if (a.loginTime() == null) {
                return 1;
            }
            if (b.loginTime() == null) {
                return -1;
            }
            return b.loginTime().compareTo(a.loginTime());
        });

        // 登录日志是附加信息：查询失败只降级为空列表，不影响在线名单本身
        return enrichLoginActivity(onlineUsers);
    }

    @Override
    /**
     * 获取用户最近 N 天登录时间轴
    */
    public OnlineLoginHistoryVO getLoginHistory(String username, Integer days) {
        String name = trim(username);
        if (null == name) {
            return null;
        }
        int windowDays = null == days
                ? DEFAULT_LOGIN_WINDOW_DAYS
                : Math.max(1, Math.min(MAX_LOGIN_WINDOW_DAYS, days));
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(windowDays - 1L);
        List<OnlineLoginIpStat> ipStats;
        List<OnlineLoginDayStat> dayStats;
        try {
            ipStats = sysUserLogMapper.listLoginIpStats(List.of(name));
            dayStats = sysUserLogMapper.listLoginDayStats(List.of(name), startDate.atStartOfDay());
        } catch (Exception e) {
            log.error("查询用户登录时间轴失败: username={}", name, e);
            return new OnlineLoginHistoryVO(name, windowDays, 0, 0, 0, null, List.of(), List.of());
        }

        Map<String, OnlineLoginDayStat> statsByDate = new HashMap<>();
        for (OnlineLoginDayStat stat : dayStats) {
            statsByDate.put(String.valueOf(stat.date()), stat);
        }
        List<OnlineLoginDayVO> items = new ArrayList<>(windowDays);
        int total = 0;
        int success = 0;
        int fail = 0;
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            OnlineLoginDayStat stat = statsByDate.get(date.toString());
            if (null == stat) {
                items.add(new OnlineLoginDayVO(date.toString(), 0, 0, 0, List.of(), null));
                continue;
            }
            OnlineLoginDayVO day = toLoginDay(stat);
            items.add(day);
            total += defaultValue(day.total());
            success += defaultValue(day.success());
            fail += defaultValue(day.fail());
        }

        List<OnlineLoginIpVO> ips = new ArrayList<>(ipStats.size());
        LocalDateTime lastTime = null;
        for (OnlineLoginIpStat stat : ipStats) {
            ips.add(new OnlineLoginIpVO(stat.ip(), stat.address(), stat.lastTime(), stat.total()));
            if (null != stat.lastTime() && (null == lastTime || stat.lastTime().isAfter(lastTime))) {
                lastTime = stat.lastTime();
            }
        }
        return new OnlineLoginHistoryVO(name, windowDays, total, success, fail, lastTime, ips, items);
    }

    /**
     * 用登录日志补齐在线用户的「全部登录 IP」与「最近 30 天登录情况」
     *
     * @param onlineUsers 在线用户列表，不允许为 null
     * @return 补齐后的列表，查询失败时原样返回
     */
    private List<OnlineUserVO> enrichLoginActivity(List<OnlineUserVO> onlineUsers) {
        if (onlineUsers.isEmpty()) {
            return onlineUsers;
        }
        List<String> usernames = onlineUsers.stream()
                .map(OnlineUserVO::username)
                .filter(this::hasText)
                .distinct()
                .toList();
        if (usernames.isEmpty()) {
            return onlineUsers;
        }
        LocalDate startDate = LocalDate.now().minusDays(DEFAULT_LOGIN_WINDOW_DAYS - 1L);
        Map<String, List<OnlineLoginIpStat>> ipStats = new HashMap<>();
        Map<String, List<OnlineLoginDayStat>> dayStats = new HashMap<>();
        try {
            for (OnlineLoginIpStat stat : sysUserLogMapper.listLoginIpStats(usernames)) {
                ipStats.computeIfAbsent(stat.username(), key -> new ArrayList<>()).add(stat);
            }
            for (OnlineLoginDayStat stat : sysUserLogMapper.listLoginDayStats(usernames, startDate.atStartOfDay())) {
                dayStats.computeIfAbsent(stat.username(), key -> new ArrayList<>()).add(stat);
            }
        } catch (Exception e) {
            log.warn("加载在线用户登录历史失败: {}", e.getMessage());
            return onlineUsers;
        }

        List<OnlineUserVO> enriched = new ArrayList<>(onlineUsers.size());
        for (OnlineUserVO user : onlineUsers) {
            enriched.add(new OnlineUserVO(
                    user.userId(), user.username(), user.nickname(), user.deptName(),
                    user.loginIp(), user.loginAddress(), user.browser(), user.os(),
                    user.loginTime(), user.lastAccessTime(), user.token(), user.loginType(),
                    buildLoginIps(user, ipStats.get(user.username())),
                    buildLoginDays(dayStats.get(user.username()))));
        }
        return enriched;
    }

    /**
     * 合并当前会话 IP 与登录日志 IP，供前端「xx,yy +N」折叠展示
     *
     * @param user  在线用户
     * @param stats 该用户的登录 IP 统计，可为 null
     * @return IP 列表，当前会话 IP 置顶
     */
    private List<OnlineLoginIpVO> buildLoginIps(OnlineUserVO user, List<OnlineLoginIpStat> stats) {
        List<OnlineLoginIpVO> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        String currentIp = trim(user.loginIp());
        if (null != currentIp) {
            OnlineLoginIpVO current = null;
            if (null != stats) {
                for (OnlineLoginIpStat stat : stats) {
                    if (currentIp.equals(stat.ip())) {
                        current = new OnlineLoginIpVO(stat.ip(), stat.address(), stat.lastTime(), stat.total());
                        break;
                    }
                }
            }
            if (null == current) {
                current = new OnlineLoginIpVO(currentIp, user.loginAddress(), null, 0L);
            }
            result.add(current);
            seen.add(currentIp);
        }
        if (null != stats) {
            for (OnlineLoginIpStat stat : stats) {
                if (!hasText(stat.ip()) || seen.contains(stat.ip())) {
                    continue;
                }
                result.add(new OnlineLoginIpVO(stat.ip(), stat.address(), stat.lastTime(), stat.total()));
                seen.add(stat.ip());
            }
        }
        return result;
    }

    /**
     * 将日维度聚合行转换为前端时间轴节点
     *
     * @param stats 日维度聚合行，可为 null
     * @return 只含有登录记录的日期节点
     */
    private List<OnlineLoginDayVO> buildLoginDays(List<OnlineLoginDayStat> stats) {
        if (null == stats || stats.isEmpty()) {
            return List.of();
        }
        List<OnlineLoginDayVO> days = new ArrayList<>(stats.size());
        for (OnlineLoginDayStat stat : stats) {
            days.add(toLoginDay(stat));
        }
        return days;
    }

    /**
     * 转换单个日维度聚合行
     *
     * @param stat 日维度聚合行，不允许为 null
     * @return 时间轴节点
     */
    private OnlineLoginDayVO toLoginDay(OnlineLoginDayStat stat) {
        List<String> ips = new ArrayList<>();
        if (hasText(stat.ips())) {
            for (String ip : stat.ips().split(",")) {
                String trimmed = trim(ip);
                if (null != trimmed && !ips.contains(trimmed)) {
                    ips.add(trimmed);
                }
            }
        }
        return new OnlineLoginDayVO(String.valueOf(stat.date()),
                defaultValue(stat.total()), defaultValue(stat.success()), defaultValue(stat.fail()),
                ips, stat.lastTime());
    }

    /**
     * 空计数归一为 0，避免前端收到 null
     *
     * @param value 计数
     * @return 非空计数
     */
    private int defaultValue(Integer value) {
        return null == value ? 0 : value;
    }

    /**
     * 是否为有效字符串
     *
     * @param value 值
     * @return 是否非空白
     */
    private boolean hasText(String value) {
        return null != trim(value);
    }

    /**
     * 修剪字符串
     *
     * @param value 值
     * @return 修剪后的值，空白返回 null
     */
    private String trim(String value) {
        if (null == value) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
        // 解析登录时间
        LocalDateTime loginTime = info.getLoginTime() == null ? null : LocalDateTime.ofInstant(
                Instant.ofEpochMilli(info.getLoginTime()),
                ZoneId.systemDefault());

        // 解析最后访问时间
        LocalDateTime lastAccessTime = info.getLastAccessTime() == null ? null : LocalDateTime.ofInstant(
                Instant.ofEpochMilli(info.getLastAccessTime()),
                ZoneId.systemDefault());

        return new OnlineUserVO(
                info.getUserId(),
                info.getUsername(),
                info.getNickname(),
                null,
                info.getLoginIp(),
                info.getLoginAddress(),
                info.getBrowser(),
                info.getOs(),
                loginTime,
                lastAccessTime,
                info.getToken(),
                info.getLoginType(),
                List.of(),
                List.of());
    }
}
