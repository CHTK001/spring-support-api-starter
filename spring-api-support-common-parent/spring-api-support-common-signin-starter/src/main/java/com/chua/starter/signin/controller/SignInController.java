package com.chua.starter.signin.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.signin.dto.SignInDayDTO;
import com.chua.starter.signin.dto.SignInResultDTO;
import com.chua.starter.signin.dto.SignInStatsDTO;
import com.chua.starter.signin.dto.UserSignInSummaryDTO;
import com.chua.starter.signin.service.SignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
* 签到控制器
*
* <p>提供签到系统的用户端接口（签到、日历、统计）和管理员端接口（用户签到汇总）。</p>
*
* @since 4.0.0.42
* @author CH
 */
@Slf4j
@RestController
@RequestMapping("/signin")
@Tag(name = "Sign-In System", description = "签到系统：每日签到、月度日历、积分统计")
@RequiredArgsConstructor
public class SignInController {

    /**
    * 签到核心服务
     */
    private final SignInService signInService;

    /**
    * 用户签到。
    *
    * <p>包含防抖机制：同一用户同一日期重复请求将被拦截。</p>
    *
    * @param userId 用户标识
    * @return 签到结果
     */
    @Operation(summary = "用户签到")
    @PostMapping("/do")
    public ReturnResult<SignInResultDTO> doSignIn(@RequestParam Long userId) {
        return ReturnResult.ok(signInService.signIn(userId));
    }

    /**
    * 查询今日是否已签到。
    *
    * @param userId 用户标识
    * @return 签到状态
     */
    @Operation(summary = "查询今日签到状态")
    @GetMapping("/today/{userId}")
    public ReturnResult<Map<String, Object>> checkToday(@PathVariable Long userId) {
        boolean signed = signInService.isSignedToday(userId);
        return ReturnResult.ok(Map.of("signed", signed, "userId", userId));
    }

    /**
    * 查询月度签到日历。
    *
    * @param userId    用户标识
    * @param yearMonth 年月，格式 yyyy-MM，默认当月
    * @return 已签到日期列表
     */
    @Operation(summary = "查询月度签到日历")
    @GetMapping("/calendar/{userId}")
    public ReturnResult<List<SignInDayDTO>> getCalendar(
            @PathVariable Long userId,
            @RequestParam(required = false) String yearMonth) {
        if (yearMonth == null) {
            yearMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return ReturnResult.ok(signInService.getCalendar(userId, yearMonth));
    }

    /**
    * 查询用户签到统计。
    *
    * @param userId 用户标识
    * @return 签到统计
     */
    @Operation(summary = "查询用户签到统计")
    @GetMapping("/stats/{userId}")
    public ReturnResult<SignInStatsDTO> getStats(@PathVariable Long userId) {
        return ReturnResult.ok(signInService.getStats(userId));
    }

    /**
    * 分页查询用户签到汇总（管理员端）。
    *
    * <p>按用户聚合签到天数和总积分，按积分降序排列。</p>
    *
    * @param page     页码，默认 1
    * @param pageSize 每页条数，默认 10
    * @return 用户签到汇总列表
     */
    @Operation(summary = "管理员：用户签到汇总分页")
    @GetMapping("/users/page")
    public ReturnResult<Map<String, Object>> pageUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<UserSignInSummaryDTO> records = signInService.getUserSummaryPage(page, pageSize);
        long total = signInService.countUserSummary();
        return ReturnResult.ok(Map.of(
                "records", records,
                "total", total,
                "page", page,
                "pageSize", pageSize
        ));
    }
}