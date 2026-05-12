package com.chua.payment.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.mapper.PaymentOpsMapper;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付运营数据控制器（台账/仪表盘）
 * <p>使用 MyBatis XML 查询，SQL 兼容多数据库（MySQL / H2 / PostgreSQL）。
 */
@RestController
@RequestMapping("/ops")
@RequiredArgsConstructor
public class PaymentOpsController {

    private final PaymentOpsMapper paymentOpsMapper;

    @GetMapping("/dashboard/summary")
    @Permission(value = "payment:ops:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<Map<String, Object>> dashboardSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startDate != null && endDate != null) {
            start = LocalDate.parse(startDate).atStartOfDay();
            end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // 商户统计
        result.put("merchantTotal", defaultZero(paymentOpsMapper.countMerchant()));
        result.put("activeMerchantCount", defaultZero(paymentOpsMapper.countActiveMerchant()));
        result.put("configuredMerchantCount", defaultZero(paymentOpsMapper.countConfiguredMerchant()));

        // 订单统计
        result.put("paymentOrderCount", defaultZero(paymentOpsMapper.countPaidOrder(start, end)));
        result.put("totalConsumeAmount", zeroIfNull(paymentOpsMapper.sumPaidAmount(start, end)));
        result.put("totalRefundAmount", zeroIfNull(paymentOpsMapper.sumRefundAmount(start, end)));
        result.put("refundCount", defaultZero(paymentOpsMapper.countRefundOrder(start, end)));

        // 交易流水
        result.put("transactionCount", defaultZero(paymentOpsMapper.countTransaction(start, end)));

        // 回调统计
        result.put("callbackRequestCount", defaultZero(paymentOpsMapper.countNotifyLog(start, end)));
        result.put("successNotifyCount", defaultZero(paymentOpsMapper.countSuccessNotify(start, end)));
        result.put("failedNotifyCount", defaultZero(paymentOpsMapper.countFailedNotify(start, end)));

        // 平均回调处理耗时
        result.put("averageProcessDurationMs", zeroIfNull(paymentOpsMapper.avgNotifyProcessDuration(start, end)));

        return ReturnResult.success(result);
    }

    private static int defaultZero(Integer val) {
        return val != null ? val : 0;
    }

    private static BigDecimal zeroIfNull(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
