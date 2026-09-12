package com.chua.starter.signin.service;

import com.chua.starter.signin.dto.SignInPointDTO;
import com.chua.starter.signin.mapper.SignInPointsRecordMapper;
import com.chua.starter.signin.mapper.SignInPointsRuleMapper;
import com.chua.starter.signin.model.SignInPointsRecord;
import com.chua.starter.signin.model.SignInPointsRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 签到积分服务
 *
 * <p>根据启用的积分规则计算签到积分，包含每日基础积分和连续天数里程碑奖励。
 * 积分规则由 {@code sign_in_points_rule} 表驱动，支持动态调整。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignInPointsService {

    /**
     * 规则类型：每日签到基础积分
     */
    private static final String RULE_DAILY_SIGN = "DAILY_SIGN";

    /**
     * 规则类型：连续签到 3 天奖励
     */
    private static final String RULE_STREAK_BONUS_3 = "STREAK_BONUS_3";

    /**
     * 规则类型：连续签到 7 天奖励
     */
    private static final String RULE_STREAK_BONUS_7 = "STREAK_BONUS_7";

    /**
     * 规则类型：连续签到 30 天奖励
     */
    private static final String RULE_STREAK_BONUS_30 = "STREAK_BONUS_30";

    /**
     * 连续签到 3 天的里程碑阈值
     */
    private static final int STREAK_THRESHOLD_3 = 3;

    /**
     * 连续签到 7 天的里程碑阈值
     */
    private static final int STREAK_THRESHOLD_7 = 7;

    /**
     * 连续签到 30 天的里程碑阈值
     */
    private static final int STREAK_THRESHOLD_30 = 30;

    /**
      * 积分规则 映射器
     */
    private final SignInPointsRuleMapper ruleMapper;

    /**
      * 积分流水 映射器
     */
    private final SignInPointsRecordMapper pointsRecordMapper;

    /**
     * 计算本次签到应获得的积分，并写入积分流水。
     *
     * <p>计算逻辑：</p>
     * <ul>
     *   <li>每日签到基础积分（DAILY_SIGN）</li>
     *   <li>连续签到里程碑奖励，仅当连续天数恰好等于阈值时发放</li>
     * </ul>
     *
     * @param userId         用户标识
     * @param streak         当前连续签到天数
     * @param signInRecordId 关联签到记录标识
     * @return 积分明细列表
     */
    public List<SignInPointDTO> calculatePoints(Long userId, int streak, Long signInRecordId) {
        List<SignInPointsRule> rules = ruleMapper.selectByStatus(1);
        Map<String, SignInPointsRule> ruleMap = rules.stream()
                .collect(Collectors.toMap(SignInPointsRule::getType, r -> r));

        List<SignInPointDTO> result = new ArrayList<>();

        // 每日签到基础积分
        SignInPointsRule dailyRule = ruleMap.get(RULE_DAILY_SIGN);
        if (dailyRule != null) {
            result.add(new SignInPointDTO(RULE_DAILY_SIGN, dailyRule.getPoints(), dailyRule.getDescription()));
            savePointsRecord(userId, signInRecordId, dailyRule.getId(), dailyRule.getPoints());
        }

        // 连续签到里程碑奖励（仅当连续天数恰好等于阈值时）
        addStreakBonusIfMatch(result, ruleMap, RULE_STREAK_BONUS_3, STREAK_THRESHOLD_3, streak, userId, signInRecordId);
        addStreakBonusIfMatch(result, ruleMap, RULE_STREAK_BONUS_7, STREAK_THRESHOLD_7, streak, userId, signInRecordId);
        addStreakBonusIfMatch(result, ruleMap, RULE_STREAK_BONUS_30, STREAK_THRESHOLD_30, streak, userId, signInRecordId);

        return result;
    }

    /**
     * 如果连续天数达到里程碑阈值，发放连续签到奖励积分。
     *
     * @param result          积分明细结果列表
     * @param ruleMap         规则映射
     * @param ruleType        规则类型
     * @param threshold       里程碑天数阈值
     * @param streak          当前连续签到天数
     * @param userId          用户标识
     * @param signInRecordId  关联签到记录标识
     */
    private void addStreakBonusIfMatch(List<SignInPointDTO> result, Map<String, SignInPointsRule> ruleMap,
                                        String ruleType, int threshold, int streak, Long userId, Long signInRecordId) {
        if (streak == threshold && ruleMap.containsKey(ruleType)) {
            SignInPointsRule rule = ruleMap.get(ruleType);
            result.add(new SignInPointDTO(ruleType, rule.getPoints(), rule.getDescription()));
            savePointsRecord(userId, signInRecordId, rule.getId(), rule.getPoints());
        }
    }

    /**
     * 保存单条积分流水记录。
     *
     * @param userId         用户标识
     * @param signInRecordId 签到记录标识
     * @param ruleId         规则标识
     * @param points         积分值
     */
    private void savePointsRecord(Long userId, Long signInRecordId, Long ruleId, Integer points) {
        SignInPointsRecord record = new SignInPointsRecord();
        record.setUserId(userId);
        record.setSignInRecordId(signInRecordId);
        record.setRuleId(ruleId);
        record.setPoints(points);
        pointsRecordMapper.insert(record);
    }

    /**
     * 计算积分明细列表的总和。
     *
     * @param points 积分明细列表
     * @return 积分总和
     */
    public int sumPoints(List<SignInPointDTO> points) {
        return points.stream().mapToInt(SignInPointDTO::getPoints).sum();
    }
}