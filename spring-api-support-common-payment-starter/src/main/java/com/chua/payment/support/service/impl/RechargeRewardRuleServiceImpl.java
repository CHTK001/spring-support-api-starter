package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.entity.RechargeRewardRule;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.RechargeRewardRuleMapper;
import com.chua.payment.support.service.RechargeRewardRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值奖励规则服务实现
 */
@Service
@RequiredArgsConstructor
public class RechargeRewardRuleServiceImpl implements RechargeRewardRuleService {

    private final RechargeRewardRuleMapper rechargeRewardRuleMapper;

    @Override
    public List<RechargeRewardRule> listEnabledByMerchantId(Long merchantId) {
        return rechargeRewardRuleMapper.selectList(new LambdaQueryWrapper<RechargeRewardRule>()
                .eq(RechargeRewardRule::getMerchantId, merchantId)
                .eq(RechargeRewardRule::getStatus, "ENABLED")
                .orderByAsc(RechargeRewardRule::getSortOrder));
    }

    @Override
    public List<RechargeRewardRule> matchRules(Long merchantId, BigDecimal rechargeAmount) {
        return rechargeRewardRuleMapper.selectList(new LambdaQueryWrapper<RechargeRewardRule>()
                .eq(RechargeRewardRule::getMerchantId, merchantId)
                .eq(RechargeRewardRule::getStatus, "ENABLED")
                .le(RechargeRewardRule::getMinAmount, rechargeAmount != null ? rechargeAmount : BigDecimal.ZERO)
                .orderByAsc(RechargeRewardRule::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RechargeRewardRule create(RechargeRewardRule rule) {
        if (rule.getStatus() == null) {
            rule.setStatus("ENABLED");
        }
        rechargeRewardRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RechargeRewardRule update(RechargeRewardRule rule) {
        rechargeRewardRuleMapper.updateById(rule);
        return rechargeRewardRuleMapper.selectById(rule.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        return rechargeRewardRuleMapper.deleteById(id) > 0;
    }
}
