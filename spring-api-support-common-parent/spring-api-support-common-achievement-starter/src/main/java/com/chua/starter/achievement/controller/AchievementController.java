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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Achievement controller
 *
 * <p>Provides achievement query endpoints for frontend: user achievement status,
 * tier definitions, stats overview, and admin CRUD.</p>
 *
 * @author CH
 * @since 2026-08-07
 */
@Slf4j
@RestController
@RequestMapping("/achievement")
@Tag(name = "Achievement System", description = "Achievement query and management API")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final AchievementTierMapper tierMapper;
    private final UserAchievementMapper userAchievementMapper;

    // ================== Admin CRUD ==================

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

    @Operation(summary = "Create achievement tier")
    @PostMapping("/tiers/save")
    public ReturnResult<Boolean> saveTier(@RequestBody AchievementTier tier) {
        return ReturnResult.ok(tierMapper.insert(tier) > 0);
    }

    @Operation(summary = "Update achievement tier")
    @PutMapping("/tiers/update")
    public ReturnResult<Boolean> updateTier(@RequestBody AchievementTier tier) {
        return ReturnResult.ok(tierMapper.updateById(tier) > 0);
    }

    @Operation(summary = "Delete achievement tier")
    @DeleteMapping("/tiers/delete")
    public ReturnResult<Boolean> deleteTier(@RequestParam Long id) {
        return ReturnResult.ok(tierMapper.deleteById(id) > 0);
    }

    @Operation(summary = "List all achievement types")
    @GetMapping("/tiers/types")
    public ReturnResult<List<String>> listTypes() {
        List<AchievementTier> all = tierMapper.selectList(null);
        return ReturnResult.ok(all.stream()
                .map(AchievementTier::getType)
                .distinct()
                .toList());
    }

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

    @Operation(summary = "Report user stats and refresh achievements")
    @PostMapping("/report")
    public ReturnResult<List<UnlockedAchievementDTO>> reportStats(@RequestBody ReportStatsDTO dto) {
        return ReturnResult.ok(achievementService.reportStats(dto.getUserId(), dto.getStats()));
    }

    @Operation(summary = "Get user unlocked achievements")
    @GetMapping("/my/{userId}")
    public ReturnResult<List<UnlockedAchievementDTO>> getMyAchievements(@PathVariable Long userId) {
        return ReturnResult.ok(achievementService.getUnlocked(userId));
    }

    @Operation(summary = "Get user achievement stats")
    @GetMapping("/stats/{userId}")
    public ReturnResult<UserAchievementStatsDTO> getStats(@PathVariable Long userId) {
        List<UnlockedAchievementDTO> unlocked = achievementService.getUnlocked(userId);
        UserAchievementStatsDTO stats = new UserAchievementStatsDTO();
        stats.setUserId(userId);
        stats.setTotalUnlocked(unlocked.size());
        Map<String, Optional<UnlockedAchievementDTO>> byType = unlocked.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UnlockedAchievementDTO::getType,
                        java.util.stream.Collectors.maxBy(java.util.Comparator.comparingInt(UnlockedAchievementDTO::getLevel))
                ));
        stats.setUnlockedByType(byType.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().map(UnlockedAchievementDTO::getLevel).orElse(0)
                )));
        return ReturnResult.ok(stats);
    }

    @Operation(summary = "Get tier list by type")
    @GetMapping("/tiers/{type}")
    public ReturnResult<List<AchievementTier>> getTierList(@PathVariable String type) {
        return ReturnResult.ok(achievementService.getTierListByType(type));
    }

    @Operation(summary = "Get user uncompleted tiers")
    @GetMapping("/uncompleted/{userId}/{type}")
    public ReturnResult<List<AchievementTier>> getUncompleted(@PathVariable Long userId, @PathVariable String type) {
        return ReturnResult.ok(achievementService.getUncompleted(userId, type));
    }
}
