package com.chua.starter.signin.controller;

import com.chua.starter.signin.dto.SignInDayDTO;
import com.chua.starter.signin.dto.SignInResultDTO;
import com.chua.starter.signin.dto.SignInStatsDTO;
import com.chua.starter.signin.dto.UserSignInSummaryDTO;
import com.chua.starter.signin.service.SignInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SignInController 接口测试（Standalone MockMvc）
 *
 * <p>不加载 Spring 容器，手动 mock 依赖并验证各接口的 HTTP 状态码与响应 JSON 结构。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class SignInControllerStandaloneTest {

    /**
     * 签到服务模拟
     */
    private SignInService signInService;

    /**
     * MockMvc 实例
     */
    private MockMvc mockMvc;

    /**
     * 测试前初始化，强制使用 JSON 消息转换器
     */
    @BeforeEach
    void setUp() {
        signInService = mock(SignInService.class);
        SignInController controller = new SignInController(signInService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    // ==================== POST /signin/do ====================

    @Test
    void shouldDoSignIn_success() throws Exception {
        SignInResultDTO result = new SignInResultDTO();
        result.setSuccess(true);
        result.setStreak(3);
        result.setTodayPoints(15);
        result.setTotalPoints(45);
        result.setTotalDays(10);
        result.setMsg("签到成功");

        when(signInService.signIn(anyLong())).thenReturn(result);

        mockMvc.perform(post("/signin/do").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.streak").value(3))
                .andExpect(jsonPath("$.data.todayPoints").value(15))
                .andExpect(jsonPath("$.data.totalPoints").value(45))
                .andExpect(jsonPath("$.data.totalDays").value(10))
                .andExpect(jsonPath("$.data.msg").value("签到成功"));
    }

    @Test
    void shouldDoSignIn_debounce() throws Exception {
        SignInResultDTO result = new SignInResultDTO();
        result.setSuccess(false);
        result.setAlreadySigned(true);
        result.setMsg("请勿重复签到");

        when(signInService.signIn(anyLong())).thenReturn(result);

        mockMvc.perform(post("/signin/do").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.alreadySigned").value(true))
                .andExpect(jsonPath("$.data.msg").value("请勿重复签到"));
    }

    // ==================== GET /signin/today/{userId} ====================

    @Test
    void shouldCheckToday_signed() throws Exception {
        when(signInService.isSignedToday(anyLong())).thenReturn(true);

        mockMvc.perform(get("/signin/today/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signed").value(true));
    }

    @Test
    void shouldCheckToday_notSigned() throws Exception {
        when(signInService.isSignedToday(anyLong())).thenReturn(false);

        mockMvc.perform(get("/signin/today/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signed").value(false));
    }

    // ==================== GET /signin/calendar/{userId} ====================

    @Test
    void shouldGetCalendar() throws Exception {
        SignInDayDTO dto = new SignInDayDTO();
        dto.setDate(LocalDate.of(2026, 8, 1));
        dto.setSigned(true);
        dto.setPoints(10);

        when(signInService.getCalendar(anyLong(), anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/signin/calendar/1").param("yearMonth", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].signed").value(true))
                .andExpect(jsonPath("$.data[0].points").value(10));
    }

    @Test
    void shouldGetCalendar_defaultMonth() throws Exception {
        when(signInService.getCalendar(anyLong(), anyString())).thenReturn(List.of());

        mockMvc.perform(get("/signin/calendar/1"))
                .andExpect(status().isOk());
    }

    // ==================== GET /signin/stats/{userId} ====================

    @Test
    void shouldGetStats() throws Exception {
        SignInStatsDTO stats = new SignInStatsDTO();
        stats.setUserId(1L);
        stats.setTotalDays(10);
        stats.setCurrentStreak(3);
        stats.setMaxStreak(7);
        stats.setTotalPoints(100);
        stats.setSignedToday(true);

        when(signInService.getStats(anyLong())).thenReturn(stats);

        mockMvc.perform(get("/signin/stats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalDays").value(10))
                .andExpect(jsonPath("$.data.currentStreak").value(3))
                .andExpect(jsonPath("$.data.maxStreak").value(7))
                .andExpect(jsonPath("$.data.totalPoints").value(100))
                .andExpect(jsonPath("$.data.signedToday").value(true));
    }

    @Test
    void shouldGetStats_empty() throws Exception {
        SignInStatsDTO stats = new SignInStatsDTO();
        stats.setUserId(2L);
        stats.setTotalDays(0);
        stats.setCurrentStreak(0);
        stats.setMaxStreak(0);
        stats.setTotalPoints(0);
        stats.setSignedToday(false);

        when(signInService.getStats(anyLong())).thenReturn(stats);

        mockMvc.perform(get("/signin/stats/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDays").value(0))
                .andExpect(jsonPath("$.data.signedToday").value(false));
    }

    // ==================== GET /signin/users/page ====================

    @Test
    void shouldPageUsers() throws Exception {
        UserSignInSummaryDTO summary = new UserSignInSummaryDTO();
        summary.setUserId(1L);
        summary.setSignInDays(10);
        summary.setTotalPoints(100);

        when(signInService.getUserSummaryPage(anyInt(), anyInt())).thenReturn(List.of(summary));
        when(signInService.countUserSummary()).thenReturn(5L);

        mockMvc.perform(get("/signin/users/page").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].userId").value(1))
                .andExpect(jsonPath("$.data.records[0].signInDays").value(10))
                .andExpect(jsonPath("$.data.records[0].totalPoints").value(100))
                .andExpect(jsonPath("$.data.total").value(5));
    }
}