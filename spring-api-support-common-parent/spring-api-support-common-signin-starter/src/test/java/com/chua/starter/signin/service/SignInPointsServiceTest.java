package com.chua.starter.signin.service;

import com.chua.starter.signin.dto.SignInPointDTO;
import com.chua.starter.signin.mapper.SignInPointsRecordMapper;
import com.chua.starter.signin.mapper.SignInPointsRuleMapper;
import com.chua.starter.signin.model.SignInPointsRecord;
import com.chua.starter.signin.model.SignInPointsRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SignInPointsService 单元测试
 *
 * <p>覆盖积分计算逻辑：每日基础积分、连续天数里程碑奖励、积分总和计算。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@ExtendWith(MockitoExtension.class)
class SignInPointsServiceTest {

    /**
     * 积分规则 Mapper 模拟
     */
    @Mock
    private SignInPointsRuleMapper ruleMapper;

    /**
     * 积分流水 Mapper 模拟
     */
    @Mock
    private SignInPointsRecordMapper pointsRecordMapper;

    /**
     * 测试目标服务
     */
    private SignInPointsService pointsService;

    /**
     * 测试前初始化
     */
    @BeforeEach
    void setUp() {
        pointsService = new SignInPointsService(ruleMapper, pointsRecordMapper);
    }

    // ==================== 每日基础积分 ====================

    @Test
    void testCalculatePoints_dailyOnly() {
        when(ruleMapper.selectByStatus(1)).thenReturn(List.of(createRule("DAILY_SIGN", 10)));

        List<SignInPointDTO> result = pointsService.calculatePoints(1L, 1, 100L);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getPoints());
        assertEquals("DAILY_SIGN", result.get(0).getRuleType());
        verify(pointsRecordMapper).insert(any(SignInPointsRecord.class));
    }

    // ==================== 连续天数里程碑奖励 ====================

    @Test
    void testCalculatePoints_streak3_bonus() {
        when(ruleMapper.selectByStatus(1)).thenReturn(List.of(
                createRule("DAILY_SIGN", 10),
                createRule("STREAK_BONUS_3", 5)
        ));

        List<SignInPointDTO> result = pointsService.calculatePoints(1L, 3, 100L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> "STREAK_BONUS_3".equals(p.getRuleType()) && p.getPoints() == 5));
        verify(pointsRecordMapper, times(2)).insert(any(SignInPointsRecord.class));
    }

    @Test
    void testCalculatePoints_streak3_notMatch() {
        when(ruleMapper.selectByStatus(1)).thenReturn(List.of(
                createRule("DAILY_SIGN", 10),
                createRule("STREAK_BONUS_3", 5)
        ));

        // streak=2 未达到阈值，不应发放奖励
        List<SignInPointDTO> result = pointsService.calculatePoints(1L, 2, 100L);

        assertEquals(1, result.size());
        assertTrue(result.stream().noneMatch(p -> "STREAK_BONUS_3".equals(p.getRuleType())));
        verify(pointsRecordMapper, times(1)).insert(any(SignInPointsRecord.class));
    }

    @Test
    void testCalculatePoints_streak7_bonus() {
        when(ruleMapper.selectByStatus(1)).thenReturn(List.of(
                createRule("DAILY_SIGN", 10),
                createRule("STREAK_BONUS_7", 10)
        ));

        List<SignInPointDTO> result = pointsService.calculatePoints(1L, 7, 100L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> "STREAK_BONUS_7".equals(p.getRuleType())));
        verify(pointsRecordMapper, times(2)).insert(any(SignInPointsRecord.class));
    }

    // ==================== sumPoints ====================

    @Test
    void testSumPoints() {
        List<SignInPointDTO> points = List.of(
                new SignInPointDTO("DAILY_SIGN", 10, "每日"),
                new SignInPointDTO("STREAK_BONUS_3", 5, "连续3天")
        );

        int sum = pointsService.sumPoints(points);

        assertEquals(15, sum);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建积分规则用于测试。
     *
     * @param type   规则类型
     * @param points 积分数
     * @return 积分规则
     */
    private SignInPointsRule createRule(String type, int points) {
        SignInPointsRule rule = new SignInPointsRule();
        rule.setId((long) type.length());
        rule.setType(type);
        rule.setPoints(points);
        rule.setStatus(1);
        return rule;
    }
}