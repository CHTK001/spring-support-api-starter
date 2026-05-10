package com.chua.starter.payment.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘数据查询 Mapper（通用 SQL，无数据库方言依赖）
 */
@Mapper
public interface DashboardMapper {

    /** 查询指定时间范围内已支付订单的金额合计 */
    BigDecimal selectPaidAmountSum(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("statuses") List<String> statuses);

    /** 查询指定时间范围内退款金额合计 */
    BigDecimal selectRefundAmountSum(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    /** 查询钱包冻结金额合计 */
    BigDecimal selectFrozenBalanceSum();

    /** 查询指定时间范围内的订单数 */
    Integer selectOrderCount(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    /** 查询指定时间范围内有退款的订单数 */
    Integer selectRefundOrderCount(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    /** 查询指定时间范围内超时待支付订单数 */
    Integer selectTimeoutOrderCount(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("deadline") LocalDateTime deadline);

    /** 查询指定日期已支付金额 */
    BigDecimal selectDailyPaidAmount(@Param("dayStart") LocalDateTime dayStart,
                                     @Param("dayEnd") LocalDateTime dayEnd,
                                     @Param("statuses") List<String> statuses);

    /** 查询各渠道支付金额分布 */
    List<Map<String, Object>> selectChannelDistribution(@Param("statuses") List<String> statuses);

    /** 查询各渠道支付金额对比 */
    List<Map<String, Object>> selectChannelComparison(@Param("statuses") List<String> statuses);
}
