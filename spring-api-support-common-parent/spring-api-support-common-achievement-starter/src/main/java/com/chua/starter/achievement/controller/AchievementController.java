package com.chua.starter.achievement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.achievement.dto.ReportStatsDTO;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.dto.UserAchievementStatsDTO;
import com.chua.starter.achievement.mapper.AchievementTierMapper;
import com.chua.starter.achievement.mapper.UserAchievementMapper;
import com.chua.starter.achievement.model.AchievementTier;
import com.chua.starter.achievement.model.UserAchievement;
import com.chua.starter.achievement.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 成就控制器
 *
 * <p>面向前端提供成就查询接口：用户成就状态、段位定义、统计概览以及管理员 CRUD 操作。</p>
 *
 * @since 2026-08-07
 * @author CH
 */
@Slf4j
@RestController
@RequestMapping("/achievement")
@Tag(name = "Achievement System", description = "Achievement query and management API")
@RequiredArgsConstructor
public class AchievementController {

    /**
     * 成就业务服务
     */
    private final AchievementService achievementService;

    /**
      * 成就段位定义 映射器
     */
    private final AchievementTierMapper tierMapper;

    /**
      * 用户成就记录 映射器
     */
    private final UserAchievementMapper userAchievementMapper;

    // ================== Admin CRUD ==================

    /**
     * 分页查询成就段位定义列表（管理员端）。
     *
     * @param page     页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @param type     分组标识（可选）
     * @return 段位定义分页结果
     */
    @Operation(summary = "Achievement tier page list")
    @GetMapping("/tiers/page")
    public ReturnResult<Page<AchievementTier>> pageTiers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type) {
        Page<AchievementTier> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AchievementTier> wrapper = new LambdaQueryWrapper<AchievementTier>()
                .orderByAsc(AchievementTier::getType)
                .orderByAsc(AchievementTier::getSort)
                .orderByAsc(AchievementTier::getLevel);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(AchievementTier::getType, type);
        }
        return ReturnResult.ok(tierMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 新增成就段位定义（管理员端）。
     *
     * @param tier 段位定义
     * @return 是否插入成功
     */
    @Operation(summary = "Create achievement tier")
    @PostMapping("/tiers/save")
    public ReturnResult<Boolean> saveTier(@RequestBody AchievementTier tier) {
        return ReturnResult.ok(tierMapper.insert(tier) > 0);
    }

    /**
     * 更新成就段位定义（管理员端）。
     *
     * @param tier 段位定义
     * @return 是否更新成功
     */
    @Operation(summary = "Update achievement tier")
    @PutMapping("/tiers/update")
    public ReturnResult<Boolean> updateTier(@RequestBody AchievementTier tier) {
        return ReturnResult.ok(tierMapper.updateById(tier) > 0);
    }

    /**
     * 删除成就段位定义（管理员端）。
     *
     * @param id 段位定义标识
     * @return 是否删除成功
     */
    @Operation(summary = "Delete achievement tier")
    @DeleteMapping("/tiers/delete")
    public ReturnResult<Boolean> deleteTier(@RequestParam Long id) {
        return ReturnResult.ok(tierMapper.deleteById(id) > 0);
    }

    /**
     * 查询全部已定义的成就分组标识列表。
     *
     * @return 分组标识去重后的列表
     */
    @Operation(summary = "List all achievement types")
    @GetMapping("/tiers/types")
    public ReturnResult<List<String>> listTypes() {
        List<AchievementTier> all = tierMapper.selectList(null);
        return ReturnResult.ok(all.stream()
                .map(AchievementTier::getType)
                .distinct()
                .toList());
    }

    /**
     * 分页查询指定用户的成就记录（管理员端）。
     *
     * @param userId   用户标识
     * @param page     页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @param type     分组标识（可选）
     * @return 用户成就记录分页结果
     */
    @Operation(summary = "User achievement records page")
    @GetMapping("/records/page")
    public ReturnResult<Page<UserAchievement>> pageRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type) {
        Page<UserAchievement> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserAchievement> wrapper = new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .orderByDesc(UserAchievement::getUnlockedAt);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(UserAchievement::getType, type);
        }
        return ReturnResult.ok(userAchievementMapper.selectPage(pageParam, wrapper));
    }

    // ================== User side ==================

    /**
      * 用户标识必填提示
     */
    private static final String ERROR_USER_ID_REQUIRED = "userId is required";

    /**
     * 上报用户统计值并刷新成就判定。
     *
     * <p>前端将自身掌握的行为数据批量上报，写入 KvEngine 后统一触发成就解锁。</p>
     *
     * @param dto 上报统计值请求
     * @return 新解锁的成就列表
     */
    @Operation(summary = "Report user stats and refresh achievements")
    @PostMapping("/report")
    public ReturnResult<List<UnlockedAchievementDTO>> reportStats(@RequestBody ReportStatsDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            return ReturnResult.error(ERROR_USER_ID_REQUIRED);
        }
        return ReturnResult.ok(achievementService.reportStats(dto.getUserId(), dto.getStats()));
    }

    /**
     * 查询用户已解锁的成就列表。
     *
     * @param userId 用户标识
     * @return 已解锁成就列表
     */
    @Operation(summary = "Get user unlocked achievements")
    @GetMapping("/my/{userId}")
    public ReturnResult<List<UnlockedAchievementDTO>> getMyAchievements(@PathVariable Long userId) {
        return ReturnResult.ok(achievementService.getUnlocked(userId));
    }

    /**
     * 查询用户成就统计概览。
     *
     * @param userId 用户标识
     * @return 成就统计概览
     */
    @Operation(summary = "Get user achievement stats")
    @GetMapping("/stats/{userId}")
    public ReturnResult<UserAchievementStatsDTO> getStats(@PathVariable Long userId) {
        List<UnlockedAchievementDTO> unlocked = achievementService.getUnlocked(userId);
        UserAchievementStatsDTO stats = new UserAchievementStatsDTO();
        stats.setUserId(userId);
        stats.setTotalUnlocked(unlocked.size());
        Map<String, Optional<UnlockedAchievementDTO>> byType = unlocked.stream()
                .collect(Collectors.groupingBy(
                        UnlockedAchievementDTO::getType,
                        Collectors.maxBy(Comparator.comparingInt(UnlockedAchievementDTO::getLevel))
                ));
        stats.setUnlockedByType(byType.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().map(UnlockedAchievementDTO::getLevel).orElse(0)
                )));
        stats.setTotalAchievements((int) achievementService.countAllTiers());
        return ReturnResult.ok(stats);
    }

    /**
     * 查询指定分组下的段位定义列表。
     *
     * @param type 分组标识
     * @return 段位定义列表
     */
    @Operation(summary = "Get tier list by type")
    @GetMapping("/tiers/{type}")
    public ReturnResult<List<AchievementTier>> getTierList(@PathVariable String type) {
        return ReturnResult.ok(achievementService.getTierListByType(type));
    }

    /**
     * 查询用户在指定分组下尚未解锁的段位列表。
     *
     * @param userId 用户标识
     * @param type   分组标识
     * @return 未解锁段位列表
     */
    @Operation(summary = "Get user uncompleted tiers")
    @GetMapping("/uncompleted/{userId}/{type}")
    public ReturnResult<List<AchievementTier>> getUncompleted(@PathVariable Long userId, @PathVariable String type) {
        return ReturnResult.ok(achievementService.getUncompleted(userId, type));
    }
}
