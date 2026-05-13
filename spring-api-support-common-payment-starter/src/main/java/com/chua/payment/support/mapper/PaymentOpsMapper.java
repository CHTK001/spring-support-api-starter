package com.chua.payment.support.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付运营数据 Mapper（台账/仪表盘）
 */
@Mapper
public interface PaymentOpsMapper {

    /** 商户总数 */
    Integer countMerchant();

    /** 活跃商户数 */
    Integer countActiveMerchant();

    /** 已配置支付渠道的商户数 */
    Integer countConfiguredMerchant();

    /** 指定时间范围内已支付订单数 */
    Integer countPaidOrder(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内已支付订单金额合计 */
    BigDecimal sumPaidAmount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内有退款的订单数 */
    Integer countRefundOrder(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内退款金额合计 */
    BigDecimal sumRefundAmount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内交易流水数 */
    Integer countTransaction(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内回调请求数 */
    Integer countNotifyLog(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内成功回调数 */
    Integer countSuccessNotify(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间范围内失败回调数 */
    Integer countFailedNotify(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 平均回调处理耗时（毫秒） */
    BigDecimal avgNotifyProcessDuration(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ========== 图表数据 ==========

    /** 每日交易趋势（近 30 天） */
    List<Map<String, Object>> selectDailyTrend(@Param("start") LocalDateTime start);

    /** 支付方式分布（渠道类型聚合） */
    List<Map<String, Object>> selectChannelDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 最近 N 天的每日交易汇总（用作通道对比柱状图） */
    List<Map<String, Object>> selectRecentDailySummary(@Param("start") LocalDateTime start);
}
