package com.chua.starter.achievement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.dto.UserAchievementStatsDTO;
import com.chua.starter.achievement.mapper.AchievementTierMapper;
import com.chua.starter.achievement.mapper.UserAchievementMapper;
import com.chua.starter.achievement.model.AchievementTier;
import com.chua.starter.achievement.model.UserAchievement;
import com.chua.starter.achievement.service.AchievementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AchievementController 接口测试（Standalone MockMvc）
 *
 * <p>不加载 Spring 容器，手动 mock 依赖并验证各接口的 HTTP 状态码与响应 JSON 结构。</p>
 *
 * @author CH
 * @since 2026-08-07
 */
class AchievementControllerStandaloneTest {

    private AchievementService achievementService;
    private AchievementTierMapper tierMapper;
    private UserAchievementMapper userAchievementMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        achievementService = mock(AchievementService.class);
        tierMapper = mock(AchievementTierMapper.class);
        userAchievementMapper = mock(UserAchievementMapper.class);
        AchievementController controller = new AchievementController(achievementService, tierMapper, userAchievementMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    // ==================== Admin CRUD ====================

    @Test
    void shouldPageTiers() throws Exception {
        Page<AchievementTier> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(createTier("login", 1, "首次登录")));
        when(tierMapper.selectPage(any(Page.class), any())).thenReturn(page);

        mockMvc.perform(get("/achievement/tiers/page").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("首次登录"));
    }

    @Test
    void shouldPageTiers_withTypeFilter() throws Exception {
        Page<AchievementTier> page = new Page<>(1, 10, 0);
        when(tierMapper.selectPage(any(Page.class), any())).thenReturn(page);

        mockMvc.perform(get("/achievement/tiers/page").param("type", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void shouldSaveTier() throws Exception {
        when(tierMapper.insert(any(AchievementTier.class))).thenReturn(1);

        mockMvc.perform(post("/achievement/tiers/save")
                        .contentType("application/json")
                        .content(getTierJson("login", 1, "首次登录")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldUpdateTier() throws Exception {
        when(tierMapper.updateById(any(AchievementTier.class))).thenReturn(1);

        mockMvc.perform(put("/achievement/tiers/update")
                        .contentType("application/json")
                        .content("{\"id\":1,\"name\":\"新名称\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldDeleteTier() throws Exception {
        when(tierMapper.deleteById(anyLong())).thenReturn(1);

        mockMvc.perform(delete("/achievement/tiers/delete").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldListTypes() throws Exception {
        when(tierMapper.selectList(null)).thenReturn(List.of(
                createTier("login", 1, "a"),
                createTier("login", 2, "b"),
                createTier("sign", 1, "c")
        ));

        mockMvc.perform(get("/achievement/tiers/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0]").value("login"))
                .andExpect(jsonPath("$.data[1]").value("sign"));
    }

    @Test
    void shouldPageRecords() throws Exception {
        Page<UserAchievement> page = new Page<>(1, 10, 1);
        UserAchievement record = new UserAchievement();
        record.setId(1L);
        record.setUserId(100L);
        record.setType("login");
        record.setMaxLevel(2);
        page.setRecords(List.of(record));
        when(userAchievementMapper.selectPage(any(Page.class), any())).thenReturn(page);

        mockMvc.perform(get("/achievement/records/page").param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].type").value("login"))
                .andExpect(jsonPath("$.data.records[0].maxLevel").value(2));
    }

    // ==================== User side ====================

    @Test
    void shouldGetMyAchievements() throws Exception {
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setName("首次登录");
        dto.setType("login");
        dto.setLevel(1);
        when(achievementService.getUnlocked(anyLong())).thenReturn(List.of(dto));

        mockMvc.perform(get("/achievement/my/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("首次登录"))
                .andExpect(jsonPath("$.data[0].type").value("login"));
    }

    @Test
    void shouldGetStats() throws Exception {
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setName("首次登录");
        dto.setType("login");
        dto.setLevel(1);
        when(achievementService.getUnlocked(anyLong())).thenReturn(List.of(dto));

        mockMvc.perform(get("/achievement/stats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalUnlocked").value(1))
                .andExpect(jsonPath("$.data.unlockedByType.login").value(1));
    }

    @Test
    void shouldGetStats_empty() throws Exception {
        when(achievementService.getUnlocked(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/achievement/stats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUnlocked").value(0));
    }

    @Test
    void shouldGetTierList() throws Exception {
        when(achievementService.getTierListByType(anyString())).thenReturn(List.of(createTier("login", 1, "首次登录")));

        mockMvc.perform(get("/achievement/tiers/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("login"));
    }

    @Test
    void shouldGetUncompleted() throws Exception {
        when(achievementService.getUncompleted(anyLong(), anyString())).thenReturn(List.of(createTier("login", 2, "登录达人")));

        mockMvc.perform(get("/achievement/uncompleted/1/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("登录达人"));
    }

    // ==================== helpers ====================

    private AchievementTier createTier(String type, int level, String name) {
        AchievementTier tier = new AchievementTier();
        tier.setId(Long.valueOf(level));
        tier.setType(type);
        tier.setLevel(level);
        tier.setName(name);
        tier.setBadge("\uD83C\uDFC5");
        return tier;
    }

    private String getTierJson(String type, int level, String name) {
        return "{\"type\":\"" + type + "\",\"level\":" + level + ",\"name\":\"" + name + "\",\"status\":1}";
    }
}