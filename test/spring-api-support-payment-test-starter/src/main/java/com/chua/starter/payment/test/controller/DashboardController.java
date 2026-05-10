package com.chua.starter.payment.test.controller;

import com.chua.starter.payment.test.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 支付仪表盘 - 数据统计接口
 * <p>使用 MyBatis XML 查询，SQL 兼容多数据库（MySQL / H2 / PostgreSQL / SQL Server）。
 */
@RestController
@RequestMapping("/api/test/ops")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    private static final List<String> PAID_STATUSES = List.of("PAID", "COMPLETED");
    private static final List<String> PAID_REFUNDED_STATUSES = List.of("PAID", "COMPLETED", "REFUNDED", "USER_REFUNDED", "SYS_REFUNDED");

    @GetMapping("/dashboard/summary")
    public Map<String, Object> summary(String startDate, String endDate) {
        boolean isTotal = startDate != null;
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        // 总计/今日范围
        LocalDateTime rangeStart = isTotal ? null : todayStart;
        LocalDateTime rangeEnd = isTotal ? null : todayEnd;

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("totalConsumeAmount", zeroIfNull(
            dashboardMapper.selectPaidAmountSum(rangeStart, rangeEnd, PAID_STATUSES)));

        result.put("settledAmount", zeroIfNull(
            dashboardMapper.selectPaidAmountSum(rangeStart, rangeEnd, PAID_STATUSES)));

        result.put("refundAmount", zeroIfNull(
            dashboardMapper.selectRefundAmountSum(rangeStart, rangeEnd)));

        result.put("frozenAmount", zeroIfNull(
            dashboardMapper.selectFrozenBalanceSum()));

        result.put("orderCount", defaultZero(
            dashboardMapper.selectOrderCount(rangeStart, rangeEnd)));

        result.put("refundOrders", defaultZero(
            dashboardMapper.selectRefundOrderCount(rangeStart, rangeEnd)));

        result.put("timeoutOrders", defaultZero(
            dashboardMapper.selectTimeoutOrderCount(rangeStart, rangeEnd, LocalDateTime.now().minusHours(1))));

        return result;
    }

    @GetMapping("/dashboard/trend")
    public List<Map<String, Object>> trend() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            BigDecimal amount = zeroIfNull(
                dashboardMapper.selectDailyPaidAmount(dayStart, dayEnd, PAID_STATUSES));

            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("date", (6 - i + 1) + "日");
            dayData.put("amount", amount);
            result.add(dayData);
        }
        return result;
    }

    @GetMapping("/dashboard/channel-distribution")
    public List<Map<String, Object>> channelDistribution() {
        List<Map<String, Object>> rows = dashboardMapper.selectChannelDistribution(PAID_STATUSES);

        // 填充缺失渠道，确保返回结构完整
        Map<String, String> channelNames = Map.of(
            "WECHAT", "微信支付",
            "ALIPAY", "支付宝",
            "WALLET", "钱包余额",
            "UNIONPAY", "银联"
        );

        Set<String> existing = rows.stream()
            .map(r -> (String) r.get("name"))
            .collect(Collectors.toSet());

        for (var entry : channelNames.entrySet()) {
            if (!existing.contains(entry.getKey())) {
                rows.add(Map.of("name", entry.getValue(), "value", BigDecimal.ZERO));
            }
        }

        // 将内部 code 转为中文名
        return rows.stream().map(r -> {
            String code = (String) r.get("name");
            String displayName = channelNames.getOrDefault(code, code);
            return Map.of("name", displayName, "value", r.get("value"));
        }).collect(Collectors.toList());
    }

    @GetMapping("/dashboard/channel-comparison")
    public Map<String, Object> channelComparison() {
        List<Map<String, Object>> rows = dashboardMapper.selectChannelComparison(PAID_STATUSES);

        Map<String, String> channelNames = Map.of(
            "WECHAT", "微信支付",
            "ALIPAY", "支付宝",
            "WALLET", "钱包余额",
            "UNIONPAY", "银联"
        );

        List<String> categories = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (var entry : channelNames.entrySet()) {
            categories.add(entry.getValue());
            rows.stream()
                .filter(r -> entry.getKey().equals(r.get("name")))
                .findFirst()
                .ifPresentOrElse(
                    r -> values.add((BigDecimal) r.get("value")),
                    () -> values.add(BigDecimal.ZERO));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", categories);
        result.put("values", values);
        return result;
    }

    private static BigDecimal zeroIfNull(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private static int defaultZero(Integer val) {
        return val != null ? val : 0;
    }
}
