package com.chua.starter.oauth.sso.support.service;

import com.chua.starter.oauth.sso.support.entity.LoginRecord;
import com.chua.starter.oauth.sso.support.entity.LoginStatistics;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth认证登录追踪服务接口
 * 
 * 提供OAuth认证登录信息的追踪和统计功能
 * 
 * @author CH
 * @version 4.0.0.32
 */
public interface LoginTrackingService {

    /**
     * 记录OAuth认证信息
     * 
     * @param ip 认证IP地址
     * @param username 用户名
     * @param userId 用户ID
     * @param success 是否认证成功
     * @param protocol 认证协议（custom, jwt, jwt-rsa等）
     * @param authMethod 认证方式（password, sms, email, oauth等）
     * @param userAgent 用户代理信息
     * @param clientId 客户端ID
     */
    void recordLogin(String ip, String username, String userId, boolean success, 
                    String protocol, String authMethod, String userAgent, String clientId);

    /**
     * 记录OAuth会话开始
     * 
     * @param sessionId 会话ID
     * @param ip 认证IP地址
     * @param username 用户名
     * @param userId 用户ID
     * @param token 访问令牌
     * @param refreshToken 刷新令牌
     */
    void recordSessionStart(String sessionId, String ip, String username, String userId, 
                           String token, String refreshToken);

    /**
     * 记录OAuth会话结束
     * 
     * @param sessionId 会话ID
     */
    void recordSessionEnd(String sessionId);

    /**
     * 记录令牌刷新
     * 
     * @param sessionId 会话ID
     * @param oldToken 旧令牌
     * @param newToken 新令牌
     */
    void recordTokenRefresh(String sessionId, String oldToken, String newToken);

    /**
     * 获取OAuth认证统计信息
     * 
     * @param days 统计天数
     * @return 认证统计信息
     */
    LoginStatistics getLoginStatistics(int days);

    /**
     * 获取特定IP的OAuth认证统计
     * 
     * @param ip IP地址
     * @param days 统计天数
     * @return 认证统计信息
     */
    LoginStatistics getLoginStatisticsByIp(String ip, int days);

    /**
     * 获取特定用户的OAuth认证统计
     * 
     * @param userId 用户ID
     * @param days 统计天数
     * @return 认证统计信息
     */
    LoginStatistics getLoginStatisticsByUser(String userId, int days);

    /**
     * 获取OAuth认证历史记录
     * 
     * @param days 查询天数
     * @param limit 返回记录数限制
     * @return 认证记录列表
     */
    List<LoginRecord> getLoginHistory(int days, int limit);

    /**
     * 获取特定IP的OAuth认证历史
     * 
     * @param ip IP地址
     * @param days 查询天数
     * @param limit 返回记录数限制
     * @return 认证记录列表
     */
    List<LoginRecord> getLoginHistoryByIp(String ip, int days, int limit);

    /**
     * 获取特定用户的OAuth认证历史
     * 
     * @param userId 用户ID
     * @param days 查询天数
     * @param limit 返回记录数限制
     * @return 认证记录列表
     */
    List<LoginRecord> getLoginHistoryByUser(String userId, int days, int limit);

    /**
     * 获取当前活跃OAuth会话
     * 
     * @return 活跃会话列表
     */
    List<LoginRecord> getActiveSessions();

    /**
     * 获取认证次数最多的IP地址
     * 
     * @param days 统计天数
     * @param limit 返回数量限制
     * @return IP地址和认证次数的映射
     */
    List<LoginStatistics.IpStatistic> getTopLoginIps(int days, int limit);

    /**
     * 获取最活跃的用户
     * 
     * @param days 统计天数
     * @param limit 返回数量限制
     * @return 用户统计列表
     */
    List<LoginStatistics.UserStatistic> getTopActiveUsers(int days, int limit);

    /**
     * 检查IP是否被锁定
     * 
     * @param ip IP地址
     * @return 是否被锁定
     */
    boolean isIpLocked(String ip);

    /**
     * 锁定IP地址
     * 
     * @param ip IP地址
     * @param duration 锁定时长（分钟）
     * @param reason 锁定原因
     */
    void lockIp(String ip, int duration, String reason);

    /**
     * 解锁IP地址
     * 
     * @param ip IP地址
     */
    void unlockIp(String ip);

    /**
     * 获取失败认证次数
     * 
     * @param ip IP地址
     * @param timeWindow 时间窗口（分钟）
     * @return 失败次数
     */
    int getFailedLoginCount(String ip, int timeWindow);

    /**
     * 获取用户失败认证次数
     * 
     * @param userId 用户ID
     * @param timeWindow 时间窗口（分钟）
     * @return 失败次数
     */
    int getFailedLoginCountByUser(String userId, int timeWindow);

    /**
     * 清理过期数据
     */
    void cleanupExpiredData();

    /**
     * 获取协议使用统计
     *
     * @param days 统计天数
     * @return 协议使用统计
     */
    java.util.Map<String, Long> getProtocolUsageStats(int days);

    /**
     * 获取认证方式使用统计
     *
     * @param days 统计天数
     * @return 认证方式使用统计
     */
    java.util.Map<String, Long> getAuthMethodUsageStats(int days);
}
