package com.chua.starter.payment.test.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/test/ops")
public class DashboardController {

    private final JdbcTemplate jdbc;

    public DashboardController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/dashboard/summary")
    public Map<String, Object> summary(String startDate, String endDate) {
        boolean isTotal = startDate != null;
        String dateCondition = isTotal ? "" : " AND DATE(created_at) = CURDATE()";
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("totalConsumeAmount", querySingle(
            "SELECT COALESCE(SUM(paid_amount),0) FROM pay_order WHERE status IN ('PAID','COMPLETED')" + dateCondition));

        result.put("settledAmount", querySingle(
            "SELECT COALESCE(SUM(paid_amount),0) FROM pay_order WHERE status IN ('PAID','COMPLETED')" + dateCondition));

        result.put("refundAmount", querySingle(
            "SELECT COALESCE(SUM(refund_amount),0) FROM pay_order WHERE 1=1" + dateCondition));

        result.put("frozenAmount", querySingle(
            "SELECT COALESCE(SUM(frozen_balance),0) FROM pay_wallet_account"));

        result.put("orderCount", defaultZero(jdbc.queryForObject(
            "SELECT COUNT(*) FROM pay_order" + (isTotal ? "" : " WHERE DATE(created_at) = CURDATE()"), Integer.class)));

        result.put("refundOrders", defaultZero(jdbc.queryForObject(
            "SELECT COUNT(*) FROM pay_order WHERE refund_amount > 0" + dateCondition, Integer.class)));

        result.put("timeoutOrders", defaultZero(jdbc.queryForObject(
            "SELECT COUNT(*) FROM pay_order WHERE status = 'PENDING' AND created_at < DATE_SUB(NOW(), INTERVAL 1 HOUR)" +
            (isTotal ? "" : " AND DATE(created_at) = CURDATE()"), Integer.class)));

        return result;
    }

    @GetMapping("/dashboard/trend")
    public List<Map<String, Object>> trend() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            BigDecimal amount = querySingle(
                "SELECT COALESCE(SUM(paid_amount),0) FROM pay_order WHERE status IN ('PAID','COMPLETED') AND DATE(created_at) = DATE_SUB(CURDATE(), INTERVAL " + i + " DAY)");
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", (6 - i + 1) + "日");
            day.put("amount", amount);
            result.add(day);
        }
        return result;
    }

    @GetMapping("/dashboard/channel-distribution")
    public List<Map<String, Object>> channelDistribution() {
        String sql = "SELECT " +
            "COALESCE(SUM(CASE WHEN channel_type = 'WECHAT' THEN paid_amount ELSE 0 END),0) AS wechat, " +
            "COALESCE(SUM(CASE WHEN channel_type = 'ALIPAY' THEN paid_amount ELSE 0 END),0) AS alipay, " +
            "COALESCE(SUM(CASE WHEN channel_type = 'WALLET' THEN paid_amount ELSE 0 END),0) AS wallet " +
            "FROM pay_order WHERE status IN ('PAID','COMPLETED')";
        Map<String, Object> row = jdbc.queryForMap(sql);
        return List.of(
            Map.of("name", "微信支付", "value", row.get("wechat")),
            Map.of("name", "支付宝", "value", row.get("alipay")),
            Map.of("name", "钱包余额", "value", row.get("wallet")));
    }

    @GetMapping("/dashboard/channel-comparison")
    public Map<String, Object> channelComparison() {
        String sql = "SELECT " +
            "COALESCE(SUM(CASE WHEN channel_type = 'WECHAT' THEN paid_amount ELSE 0 END),0) AS wechat, " +
            "COALESCE(SUM(CASE WHEN channel_type = 'ALIPAY' THEN paid_amount ELSE 0 END),0) AS alipay, " +
            "COALESCE(SUM(CASE WHEN channel_type = 'WALLET' THEN paid_amount ELSE 0 END),0) AS wallet, " +
            "COALESCE(SUM(CASE WHEN channel_type = 'UNIONPAY' THEN paid_amount ELSE 0 END),0) AS unionpay " +
            "FROM pay_order WHERE status IN ('PAID','COMPLETED')";
        Map<String, Object> row = jdbc.queryForMap(sql);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", List.of("微信支付", "支付宝", "钱包余额", "银联", "其他"));
        result.put("values", List.of(
            row.get("wechat"), row.get("alipay"),
            row.get("wallet"), row.get("unionpay"), BigDecimal.ZERO));
        return result;
    }

    private BigDecimal querySingle(String sql) {
        BigDecimal val = jdbc.queryForObject(sql, BigDecimal.class);
        return val != null ? val : BigDecimal.ZERO;
    }

    private int defaultZero(Integer val) {
        return val != null ? val : 0;
    }
}