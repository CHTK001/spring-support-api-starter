package com.chua.payment.support.service;

import com.chua.payment.support.entity.RechargeRewardRule;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值奖励规则服务
 */
public interface RechargeRewardRuleService {

    List<RechargeRewardRule> listEnabledByMerchantId(Long merchantId);

    /** 获取充值触发的奖励规则（按最低金额匹配） */
    List<RechargeRewardRule> matchRules(Long merchantId, BigDecimal rechargeAmount);

    RechargeRewardRule create(RechargeRewardRule rule);

    RechargeRewardRule update(RechargeRewardRule rule);

    boolean delete(Long id);
}
