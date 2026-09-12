package com.chua.starter.achievement.service;

import com.chua.common.support.concurrent.circuit.CircuitBreaker;
import com.chua.common.support.lang.datasource.kv.KvEngine;
import com.chua.starter.achievement.constant.AchievementContextConstants;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.mapper.AchievementTierMapper;
import com.chua.starter.achievement.mapper.UserAchievementMapper;
import com.chua.starter.achievement.model.AchievementTier;
import com.chua.starter.achievement.model.UserAchievement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成就服务核心业务逻辑
 *
 * <p>使用上游统一 {@link KvEngine}（由 spring-support-redis-starter 注册）存储用户统计数据。
   * 使用 {@link CircuitBreaker} + {@link com.chua.common.support.concurrent.circuit.BreakerJudge}（由 utils-support-common-starter 提供）
 * 对段位规则进行判定，通过 SPI 自动加载默认判断器，无需自建规则引擎。</p>
 *
 * <h3>数据流</h3>
 * <pre>
 * 1. KvEngine.findAllByPrefix("achievement:{userId}:") → Map&lt;statKey, value&gt;
 * 2. CircuitBreaker.evaluate(expression, context) → boolean
 * 3. 条件通过则解锁对应段位，写入 user_achievement 表
 * </pre>
 *
 * @since 2026-08-07
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class AchievementService {

    /**
      * 统一 KV 引擎，由 spring-支持-Redis-启动 注册
     */
    private final KvEngine kvEngine;

    /**
      * 成就段位定义 映射器
     */
    private final AchievementTierMapper tierMapper;

    /**
      * 用户成就记录 映射器
     */
    private final UserAchievementMapper userAchievementMapper;

    /**
     * 成就推送服务
     */
    private final AchievementPushService pushService;

    /**
     * 处理用户行为事件：更新统计数据 + 判定成就。
     *
     * @param userId  用户标识
     * @param statKey 统计字段名
     * @param delta   增量值
     * @return 新解锁的成就列表
     */
    public List<UnlockedAchievementDTO> onUserAction(Long userId, String statKey, long delta) {
        String key = AchievementContextConstants.KEY_PREFIX + userId + ":" + statKey;

        // 根据增量正负更新统计值
        if (delta >= 1) {
            kvEngine.incr(key);
        } else {
            String val = kvEngine.get(key);
            if (val != null) {
                kvEngine.put(key, String.valueOf(Math.max(0, Long.parseLong(val) - Math.abs(delta))));
            }
        }

        // 拉取该用户全部统计数据上下文
        Map<String, String> context = kvEngine.findAllByPrefix(AchievementContextConstants.KEY_PREFIX + userId + ":");
        return judgeAll(userId, context);
    }

    /**
     * 设置统计值（非增量）并判定成就。
     *
     * @param userId  用户标识
     * @param statKey 统计字段名
     * @param value   统计值
     * @return 新解锁的成就列表
     */
    public List<UnlockedAchievementDTO> setStatAndJudge(Long userId, String statKey, String value) {
        String key = AchievementContextConstants.KEY_PREFIX + userId + ":" + statKey;
        kvEngine.put(key, value);
        Map<String, String> context = kvEngine.findAllByPrefix(AchievementContextConstants.KEY_PREFIX + userId + ":");
        return judgeAll(userId, context);
    }

    /**
     * 批量上报统计值并判定成就（刷新接口）。
     *
     * <p>前端将自身掌握但后端无法感知的行为数据（如菜单点击次数、消息发送次数等）
      * 通过此接口批量上报，写入 kvengine 后统一判定成就解锁。</p>
     *
     * @param userId 用户标识
     * @param stats  统计值映射，键 为统计字段名（如 menuvisited数量），值 为数值
     * @return 新解锁的成就列表
     */
    public List<UnlockedAchievementDTO> reportStats(Long userId, Map<String, Object> stats) {
        if (stats == null || stats.isEmpty()) {
            return new ArrayList<>();
        }
        String prefix = AchievementContextConstants.KEY_PREFIX + userId + ":";
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            kvEngine.put(prefix + entry.getKey(), String.valueOf(entry.getValue()));
        }
        Map<String, String> context = kvEngine.findAllByPrefix(prefix);
        return judgeAll(userId, context);
    }

    /**
     * 拉取用户上下文并判定所有未解锁段位。
     *
     * <p>使用 {@link CircuitBreaker} 对每个段位的规则表达式进行判定，
     * 通过 {@link BreakerJudge} SPI 自动选择默认判断器。</p>
     *
     * @param userId  用户标识
     * @param context 用户统计数据上下文，键 为统计字段名（如 login数量），值 为当前值
     * @return 新解锁的成就列表
     */
    public List<UnlockedAchievementDTO> judgeAll(Long userId, Map<String, String> context) {
        List<UnlockedAchievementDTO> unlockedList = new ArrayList<>();

 // 将 kvengine 的 字符串 值转换为 熔断中断 需要的 对象 上下文
        Map<String, Object> breakerContext = convertToBreakerContext(userId, context);

        // 获取用户已解锁的各分组及最高等级
        Map<String, Integer> unlockedMap = userAchievementMapper.selectMaxLevelByUserId(userId);

        // 获取所有段位定义
        List<AchievementTier> allTiers = tierMapper.selectList(null);
        List<String> types = allTiers.stream()
                .map(AchievementTier::getType)
                .distinct()
                .collect(Collectors.toList());

        for (String type : types) {
 // 按 排序、级别 排序获取该分组段位列表
            List<AchievementTier> tierList = allTiers.stream()
                    .filter(t -> type.equals(t.getType()))
                    .sorted((a, b) -> {
                        int cmp = Integer.compare(a.getSort() != null ? a.getSort() : 0,
                                b.getSort() != null ? b.getSort() : 0);
                        return cmp != 0 ? cmp : Integer.compare(a.getLevel(), b.getLevel());
                    })
                    .collect(Collectors.toList());

            // 当前已解锁的最高等级
            Integer currentMaxLevel = unlockedMap.get(type) != null ? unlockedMap.get(type) : 0;

            for (AchievementTier tier : tierList) {
                // 已解锁的段位跳过
                if (tier.getLevel() <= currentMaxLevel) {
                    continue;
                }

 // 使用 熔断中断 判定规则表达式
                String ruleExpression = tier.getRule();
                if (ruleExpression == null || ruleExpression.trim().isEmpty()) {
                    continue;
                }

                boolean evaluated = CircuitBreaker.evaluate(ruleExpression, breakerContext);

                if (evaluated) {
                    // 解锁段位并写入数据库
                    saveUnlocked(userId, tier);
                    UnlockedAchievementDTO dto = toDTO(tier, userId);
                    unlockedList.add(dto);
                    currentMaxLevel = tier.getLevel();
                    log.info("用户 {} 解锁成就: {} [{} L{}]", userId, tier.getName(), tier.getType(), tier.getLevel());
                }
            }
        }

        // 有新增解锁则推送到前端
        if (!unlockedList.isEmpty()) {
            pushService.pushAchievements(userId, unlockedList);
        }
        return unlockedList;
    }

    /**
     * 查询用户已解锁的成就概览。
     *
     * @param userId 用户标识
     * @return 已解锁成就列表
     */
    public List<UnlockedAchievementDTO> getUnlocked(Long userId) {
        Map<String, Integer> unlockedMap = userAchievementMapper.selectMaxLevelByUserId(userId);
        List<UnlockedAchievementDTO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : unlockedMap.entrySet()) {
            List<AchievementTier> tiers = tierMapper.selectList(null).stream()
                    .filter(t -> entry.getKey().equals(t.getType()))
                    .collect(Collectors.toList());
            for (AchievementTier tier : tiers) {
                if (tier.getLevel() <= entry.getValue()) {
                    result.add(toDTO(tier, userId));
                }
            }
        }
        return result;
    }

    /**
     * 查询用户在指定分组下尚未解锁的段位。
     *
     * @param userId 用户标识
     * @param type   分组标识
     * @return 未解锁段位列表
     */
    public List<AchievementTier> getUncompleted(Long userId, String type) {
        UserAchievement record = userAchievementMapper.selectByUserIdAndType(userId, type);
        int currentMaxLevel = record != null ? record.getMaxLevel() : 0;
        return tierMapper.selectByTypeOrderBySortAndLevel(type).stream()
                .filter(t -> t.getLevel() > currentMaxLevel)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定分组下的所有段位定义。
     *
     * @param type 分组标识
     * @return 段位定义列表
     */
    public List<AchievementTier> getTierListByType(String type) {
        return tierMapper.selectByTypeOrderBySortAndLevel(type);
    }

    /**
      * 将 kvengine 的 字符串 上下文转换为 熔断中断 需要的 对象 上下文。
     *
     * <p>KvEngine 以 String 存储所有值，CircuitBreaker 需要 Number 类型才能正确执行数值比较。
     * 同时去除键前缀 {@code achievement:{userId}:}，保留纯统计字段名（如 {@code loginCount}）。</p>
     *
     * @param userId  用户标识，用于构建待去除的前缀
     * @param context kvengine 上下文
     * @return 数值类型已转换的断路器上下文
     */
    private Map<String, Object> convertToBreakerContext(Long userId, Map<String, String> context) {
        String prefixToRemove = AchievementContextConstants.KEY_PREFIX + userId + ":";
        Map<String, Object> result = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : context.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
            // 去除 "achievement:{userId}:" 前缀，保留纯统计字段名
            String statKey = key.startsWith(prefixToRemove)
                    ? key.substring(prefixToRemove.length())
                    : key;
            try {
                // 尝试解析为整数
                result.put(statKey, Integer.parseInt(value));
            } catch (NumberFormatException e) {
                try {
                    // 尝试解析为长整数
                    result.put(statKey, Long.parseLong(value));
                } catch (NumberFormatException ex) {
                    try {
                        // 尝试解析为浮点数
                        result.put(statKey, Double.parseDouble(value));
                    } catch (NumberFormatException exc) {
                        // 保留为字符串
                        result.put(statKey, value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 保存解锁记录到数据库。
     *
     * @param userId 用户标识
     * @param tier   段位定义
     */
    private void saveUnlocked(Long userId, AchievementTier tier) {
        UserAchievement existing = userAchievementMapper.selectByUserIdAndType(userId, tier.getType());
        UserAchievement record = new UserAchievement();
        record.setUserId(userId);
        record.setType(tier.getType());
        record.setMaxLevel(tier.getLevel());
        record.setUnlockedAt(LocalDateTime.now());
        if (existing == null) {
            userAchievementMapper.insert(record);
        } else {
            existing.setMaxLevel(tier.getLevel());
            existing.setUnlockedAt(LocalDateTime.now());
            userAchievementMapper.updateById(existing);
        }
    }

    /**
     * 统计所有有效段位的总数（全部分组）。
     *
     * @return 段位总数
     */
    public long countAllTiers() {
        return tierMapper.selectCount(null);
    }

    /**
     * 段位定义转解锁成就 DTO。
     *
     * @param tier   段位定义
     * @param userId 用户标识
     * @return 解锁成就 DTO
     */
    private UnlockedAchievementDTO toDTO(AchievementTier tier, Long userId) {
        UserAchievement record = userAchievementMapper.selectByUserIdAndType(userId, tier.getType());
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setId(tier.getId());
        dto.setName(tier.getName());
        dto.setBadge(tier.getBadge());
        dto.setDescription(tier.getDescription());
        dto.setType(tier.getType());
        dto.setLevel(tier.getLevel());
        dto.setUnlockedAt(record != null ? record.getUnlockedAt() : LocalDateTime.now());
        return dto;
    }
}
