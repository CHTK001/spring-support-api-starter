package com.chua.starter.signin.service;

import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.starter.achievement.constant.AchievementContextConstants;
import com.chua.starter.achievement.event.UserActionEvent;
import com.chua.starter.redis.support.template.LockTemplate;
import com.chua.starter.signin.dto.SignInDayDTO;
import com.chua.starter.signin.dto.SignInPointDTO;
import com.chua.starter.signin.dto.SignInResultDTO;
import com.chua.starter.signin.dto.SignInStatsDTO;
import com.chua.starter.signin.dto.UserSignInSummaryDTO;
import com.chua.starter.signin.mapper.SignInRecordMapper;
import com.chua.starter.signin.model.SignInRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到核心服务
 *
 * <p>提供每日签到、防抖、连续天数计算、积分发放、成就联动等完整签到业务逻辑。
 * 防抖机制使用 Redis 分布式锁 + 数据库唯一索引双层保障。</p>
 *
 * <p>签到成功后通过 {@link KvEngine} 写入成就上下文数据（持续天数、累计积分、最高连续），
 * 供成就模块的 熔断中断 规则判定使用，随后发布签到事件触发成就判定。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignInService {

    /**
     * 分布式锁 键 前缀，格式：锁:signin:{用户id}:{日期}
     */
    private static final String LOCK_KEY_PREFIX = "signin:lock:";

    /**
     * 防抖锁等待时间（毫秒），0 表示非阻塞，获取不到立即返回失败
     */
    private static final long LOCK_WAIT_MILLIS = 0L;

    /**
     * 防抖锁持有时间（秒），防止锁未释放导致死锁
     */
    private static final long LOCK_LEASE_SECONDS = 10L;

    /**
     * 签到记录 映射器
     */
    private final SignInRecordMapper recordMapper;

    /**
     * 积分计算服务
     */
    private final SignInPointsService pointsService;

    /**
     * 签到推送服务
     */
    private final SignInPushService pushService;

    /**
     * 分布式锁模板（Redis Redisson 实现，由 spring-支持-Redis-启动 自动装配）
     */
    private final LockTemplate lockTemplate;

    /**
     * 事件发布器，用于发布签到事件给成就模块
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 统一 KV 引擎，写入成就上下文数据（由 spring-支持-Redis-启动 注册）
     */
    private final KvEngine kvEngine;

    /**
     * 用户签到操作。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>分布式锁防抖（同一用户同一日期的请求串行执行）</li>
     *   <li>校验今日是否已签到（数据库查询）</li>
     *   <li>计算连续天数（检查昨日签到记录）</li>
     *   <li>插入签到记录</li>
     *   <li>计算积分并写入积分流水</li>
     *   <li>更新签到记录的积分字段</li>
     *   <li>发布签到事件给成就模块（触发 signInCount 累计）</li>
     *   <li>推送签到成功消息到前端</li>
     * </ol>
     *
     * @param userId 用户标识
     * @return 签到结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SignInResultDTO signIn(Long userId) {
        LocalDate today = LocalDate.now();
        String lockKey = LOCK_KEY_PREFIX + userId + ":" + today;

        // 防抖：尝试获取锁，获取失败提示用户
        boolean locked = lockTemplate.tryLock(lockKey, Duration.ofMillis(LOCK_WAIT_MILLIS),
                Duration.ofSeconds(LOCK_LEASE_SECONDS), () -> {});
        if (!locked) {
            SignInResultDTO result = new SignInResultDTO();
            result.setSuccess(false);
            result.setAlreadySigned(true);
            result.setMsg("请勿重复签到");
            return result;
        }

        try {
            // 校验今日是否已签到
            SignInRecord existing = recordMapper.selectByUserIdAndDate(userId, today);
            if (existing != null) {
                SignInResultDTO result = new SignInResultDTO();
                result.setSuccess(false);
                result.setAlreadySigned(true);
                result.setMsg("今日已签到");
                result.setTodayPoints(existing.getPoints());
                result.setStreak(existing.getStreak());
                result.setTotalPoints(recordMapper.selectTotalPointsByUserId(userId));
                result.setTotalDays(recordMapper.selectByUserIdOrderByDateDesc(userId).size());
                return result;
            }

            // 查询昨日签到记录，计算连续天数
            LocalDate yesterday = today.minusDays(1);
            SignInRecord yesterdayRecord = recordMapper.selectByUserIdAndDate(userId, yesterday);
            int streak = (yesterdayRecord != null) ? yesterdayRecord.getStreak() + 1 : 1;

            // 插入签到记录（积分暂为 0，稍后更新）
            SignInRecord record = new SignInRecord();
            record.setUserId(userId);
            record.setSignDate(today);
            record.setStreak(streak);
            record.setPoints(0);
            recordMapper.insert(record);

            // 计算积分并写入流水
            List<SignInPointDTO> pointsBreakdown = pointsService.calculatePoints(userId, streak, record.getId());
            int todayPoints = pointsService.sumPoints(pointsBreakdown);

            // 更新签到记录的积分字段
            record.setPoints(todayPoints);
            recordMapper.updateById(record);

            // 查询累计数据
            Integer totalPoints = recordMapper.selectTotalPointsByUserId(userId);
            List<SignInRecord> allRecords = recordMapper.selectByUserIdOrderByDateDesc(userId);

            // 构建结果
            SignInResultDTO result = new SignInResultDTO();
            result.setSuccess(true);
            result.setAlreadySigned(false);
            result.setMsg("签到成功");
            result.setTodayPoints(todayPoints);
            result.setStreak(streak);
            result.setTotalPoints(totalPoints);
            result.setTotalDays(allRecords.size());
            result.setPointsBreakdown(pointsBreakdown);

 // 写入成就上下文数据（前缀与成就模块一致，熔断中断 规则可引用）
            writeAchievementContext(userId, streak, totalPoints != null ? totalPoints : 0);

            // 发布签到事件给成就模块
            try {
                eventPublisher.publishEvent(new UserActionEvent(this, userId, "sign_in"));
            } catch (Exception e) {
                log.warn("发布签到事件失败，不影响签到流程: {}", e.getMessage());
            }

            // 推送签到成功消息到前端
            try {
                pushService.pushSignInDone(userId, result);
            } catch (Exception e) {
                log.warn("推送签到消息失败，不影响签到流程: {}", e.getMessage());
            }

            log.info("用户 {} 签到成功，连续 {} 天，获得 {} 积分", userId, streak, todayPoints);
            return result;

        } catch (Exception e) {
            log.error("签到失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("签到失败: " + e.getMessage(), e);
        } finally {
            lockTemplate.release(lockKey);
        }
    }

    /**
     * 查询今日是否已签到。
     *
     * @param userId 用户标识
     * @return true 已签到，false 未签到
     */
    public boolean isSignedToday(Long userId) {
        return recordMapper.selectByUserIdAndDate(userId, LocalDate.now()) != null;
    }

    /**
     * 查询指定月份的签到日历数据。
     *
     * @param userId    用户标识
     * @param yearMonth 年月，格式 yyyy-MM
     * @return 日历项列表
     */
    public List<SignInDayDTO> getCalendar(Long userId, String yearMonth) {
        List<LocalDate> signedDates = recordMapper.selectDatesByUserIdAndMonth(userId, yearMonth);
        return signedDates.stream().map(date -> {
            SignInDayDTO dto = new SignInDayDTO();
            dto.setDate(date);
            dto.setSigned(true);
            // 获取该日期的签到记录积分
            SignInRecord record = recordMapper.selectByUserIdAndDate(userId, date);
            dto.setPoints(record != null ? record.getPoints() : 0);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 查询用户签到统计。
     *
     * @param userId 用户标识
     * @return 签到统计
     */
    public SignInStatsDTO getStats(Long userId) {
        List<SignInRecord> allRecords = recordMapper.selectByUserIdOrderByDateDesc(userId);
        Integer maxStreak = recordMapper.selectMaxStreakByUserId(userId);
        Integer totalPoints = recordMapper.selectTotalPointsByUserId(userId);
        LocalDate today = LocalDate.now();
        SignInRecord todayRecord = recordMapper.selectByUserIdAndDate(userId, today);

        SignInStatsDTO stats = new SignInStatsDTO();
        stats.setUserId(userId);
        stats.setTotalDays(allRecords.size());
        stats.setCurrentStreak(todayRecord != null ? todayRecord.getStreak() : 0);
        stats.setMaxStreak(maxStreak != null ? maxStreak : 0);
        stats.setTotalPoints(totalPoints != null ? totalPoints : 0);
        stats.setSignedToday(todayRecord != null);
        return stats;
    }

    /**
     * 分页查询用户签到汇总（管理员端）。
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 用户签到汇总列表
     */
    public List<UserSignInSummaryDTO> getUserSummaryPage(int page, int pageSize) {
        long offset = (long) (page - 1) * pageSize;
        return recordMapper.selectUserSummaryPage(offset, pageSize);
    }

    /**
     * 查询参与签到的用户总数（管理员端）。
     *
     * @return 用户总数
     */
    public long countUserSummary() {
        Long count = recordMapper.countUserSummary();
        return count != null ? count : 0L;
    }

    /**
     * 写入签到成就上下文数据到 kvengine。
     *
     * <p>写入的键（前缀 {@code achievement:{userId}:}）与成就模块一致，
     * 成就模块的 熔断中断 规则可直接引用：</p>
     * <ul>
     *   <li>{@code signInStreak}：当前连续签到天数</li>
     *   <li>{@code signInMaxStreak}：历史最高连续签到天数</li>
     *   <li>{@code signInTotalPoints}：累计签到积分</li>
     *   <li>{@code signInCount}：累计签到次数（由成就事件异步累加）</li>
     * </ul>
     *
     * @param userId      用户标识
     * @param streak      当前连续签到天数
     * @param totalPoints 累计签到积分
     */
    private void writeAchievementContext(Long userId, int streak, int totalPoints) {
        String prefix = AchievementContextConstants.KEY_PREFIX + userId + ":";
        try {
            // 当前连续天数
            kvEngine.put(prefix + AchievementContextConstants.STAT_SIGN_IN_STREAK, String.valueOf(streak));
            // 累计积分
            kvEngine.put(prefix + AchievementContextConstants.STAT_SIGN_IN_TOTAL_POINTS, String.valueOf(totalPoints));

            // 历史最高连续天数：仅当当前连续大于历史值时更新
            String maxStreakVal = kvEngine.get(prefix + AchievementContextConstants.STAT_SIGN_IN_MAX_STREAK);
            int maxStreak = 0;
            if (maxStreakVal != null) {
                try {
                    maxStreak = Integer.parseInt(maxStreakVal);
                } catch (NumberFormatException e) {
                    maxStreak = 0;
                }
            }
            if (streak > maxStreak) {
                kvEngine.put(prefix + AchievementContextConstants.STAT_SIGN_IN_MAX_STREAK, String.valueOf(streak));
            }
        } catch (Exception e) {
            log.warn("写入签到成就上下文数据失败，不影响签到流程: {}", e.getMessage());
        }
    }
}