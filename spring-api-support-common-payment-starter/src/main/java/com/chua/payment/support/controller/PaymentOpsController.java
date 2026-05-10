package com.chua.payment.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付运营数据控制器（台账/仪表盘）
 */
@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
public class PaymentOpsController {

    private final JdbcTemplate jdbc;

    @GetMapping("/dashboard/summary")
    @Permission(value = "payment:ops:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<Map<String, Object>> dashboardSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        boolean hasRange = startDate != null && endDate != null;
        Object[] dateArgs = hasRange ? new Object[]{startDate, endDate} : new Object[0];

        String dcCreated = hasRange
                ? " AND created_at >= ? AND created_at < DATE_ADD(?, INTERVAL 1 DAY)"
                : "";
        String dcReceived = hasRange
                ? " AND received_time >= ? AND received_time < DATE_ADD(?, INTERVAL 1 DAY)"
                : "";

        Map<String, Object> result = new LinkedHashMap<>();

        // 商户统计
        result.put("merchantTotal", queryInt("SELECT COUNT(*) FROM pay_merchant"));
        result.put("activeMerchantCount", queryInt("SELECT COUNT(*) FROM pay_merchant WHERE status = 1"));
        result.put("configuredMerchantCount", queryInt(
                "SELECT COUNT(DISTINCT m.id) FROM pay_merchant m INNER JOIN pay_merchant_channel c ON c.merchant_id = m.id WHERE c.status = 1"));

        // 订单统计
        String orderBase = "FROM pay_order WHERE status IN ('PAID','COMPLETED')";
        String refundBase = "FROM pay_order WHERE refund_amount > 0";

        result.put("paymentOrderCount", queryInt("SELECT COUNT(*) " + orderBase + dcCreated, dateArgs));
        result.put("totalConsumeAmount", queryDecimal("SELECT COALESCE(SUM(paid_amount),0) " + orderBase + dcCreated, dateArgs));
        result.put("totalRefundAmount", queryDecimal("SELECT COALESCE(SUM(refund_amount),0) " + refundBase + dcCreated, dateArgs));
        result.put("refundCount", queryInt("SELECT COUNT(*) " + refundBase + dcCreated, dateArgs));

        // 交易流水
        result.put("transactionCount", queryInt("SELECT COUNT(*) FROM pay_transaction_record" + dcCreated, dateArgs));

        // 回调统计
        String notifyBase = "FROM pay_notify_log WHERE 1=1";
        result.put("callbackRequestCount", queryInt("SELECT COUNT(*) " + notifyBase + dcReceived, dateArgs));
        result.put("successNotifyCount", queryInt("SELECT COUNT(*) " + notifyBase + " AND process_status = 'SUCCESS'" + dcReceived, dateArgs));
        result.put("failedNotifyCount", queryInt("SELECT COUNT(*) " + notifyBase + " AND process_status = 'FAILED'" + dcReceived, dateArgs));

        // 平均回调处理耗时
        result.put("averageProcessDurationMs",
                queryDecimal("SELECT COALESCE(AVG(TIMESTAMPDIFF(MICROSECOND, received_time, processed_time)) / 1000, 0) " + notifyBase + " AND process_status IS NOT NULL" + dcReceived, dateArgs));

        return ReturnResult.success(result);
    }

    private int queryInt(String sql, Object... args) {
        Integer val = jdbc.queryForObject(sql, Integer.class, args);
        return val != null ? val : 0;
    }

    private BigDecimal queryDecimal(String sql, Object... args) {
        BigDecimal val = jdbc.queryForObject(sql, BigDecimal.class, args);
        return val != null ? val : BigDecimal.ZERO;
    }
}
