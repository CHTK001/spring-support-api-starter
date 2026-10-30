package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.online.OnlineLoginDayStat;
import com.chua.starter.system.pojo.online.OnlineLoginIpStat;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @since 2024/8/8
 * @author CH
 */
@Mapper
public interface SysUserLogMapper extends BaseMapper<SysUserLog> {
    Long countForUserLog(@Param("query") SysUserLogQuery query,
                         @Param("sysUserId") Integer sysUserId,
                         @Param("isSuperAdmin") boolean isSuperAdmin,
                         @Param("isAdmin") boolean isAdmin,
                         @Param("isDeptLeader") boolean isDeptLeader,
                         @Param("managedDeptTreeIds") List<String> managedDeptTreeIds);

    List<SysUserLog> listForUserLog(@Param("query") SysUserLogQuery query,
    /**
     * 列出LatestLoginLogsByUsernames。
     *
     * @param usernames 方法入参 usernames
     * @return 结果列表，无数据时为空列表
     */
                                    @Param("sysUserId") Integer sysUserId,
                                    @Param("isSuperAdmin") boolean isSuperAdmin,
                                    @Param("isAdmin") boolean isAdmin,
                                    @Param("isDeptLeader") boolean isDeptLeader,
                                    @Param("managedDeptTreeIds") List<String> managedDeptTreeIds,
                                    @Param("offset") long offset,
                                    @Param("limit") long limit);

    List<SysUserLog> listLatestLoginLogsByUsernames(@Param("usernames") List<String> usernames);

    /**
     * 按用户聚合成功登录的 IP（次数 + 最近登录时间），用于在线用户「全部登录 IP」
     *
     * @param usernames 用户名列表，不允许为空
     * @return 用户 + IP 维度统计，无数据时为空列表
     */
    List<OnlineLoginIpStat> listLoginIpStats(@Param("usernames") List<String> usernames);

    /**
     * 按用户 + 日期聚合登录/失败日志，用于在线用户「最近 N 天登录时间轴」
     *
     * @param usernames 用户名列表，不允许为空
     * @param startTime 统计起点（含），不允许为 null
     * @return 用户 + 日期维度统计，无数据时为空列表
     */
    List<OnlineLoginDayStat> listLoginDayStats(@Param("usernames") List<String> usernames,
                                               @Param("startTime") LocalDateTime startTime);

    int deleteForUserLog(@Param("query") SysUserLogQuery query,
                         @Param("sysUserId") Integer sysUserId,
                         @Param("isSuperAdmin") boolean isSuperAdmin,
                         @Param("isAdmin") boolean isAdmin,
                         @Param("isDeptLeader") boolean isDeptLeader,
                         @Param("managedDeptTreeIds") List<String> managedDeptTreeIds);
}
