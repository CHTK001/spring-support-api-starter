package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.entity.WalletWithdrawApproval;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.WalletOrderMapper;
import com.chua.payment.support.mapper.WalletWithdrawApprovalMapper;
import com.chua.payment.support.service.WalletWithdrawApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 钱包提现审批服务实现
 */
@Service
@RequiredArgsConstructor
public class WalletWithdrawApprovalServiceImpl implements WalletWithdrawApprovalService {

    private final WalletWithdrawApprovalMapper approvalMapper;
    private final WalletOrderMapper walletOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawApproval createApproval(Long merchantId, Long userId, BigDecimal amount,
                                                  String bankAccount, String bankName, String accountName,
                                                  String withdrawNo, String remark) {
        // 如果未传 withdrawNo，优先匹配该用户已冻结的提现订单，避免重复创建订单
        if (!StringUtils.hasText(withdrawNo)) {
            LambdaQueryWrapper<WalletOrder> wrapper = new LambdaQueryWrapper<WalletOrder>()
                    .eq(WalletOrder::getMerchantId, merchantId)
                    .eq(WalletOrder::getUserId, userId)
                    .eq(WalletOrder::getOrderType, "WITHDRAW")
                    .eq(WalletOrder::getAmount, amount)
                    .in(WalletOrder::getStatus, "PENDING", "PROCESSING")
                    .orderByDesc(WalletOrder::getCreateTime)
                    .last("limit 1");
            WalletOrder existingOrder = walletOrderMapper.selectOne(wrapper);
            if (existingOrder != null) {
                withdrawNo = existingOrder.getOrderNo();
            } else {
                withdrawNo = generateBizNo("APR");
            }
        }

        WalletWithdrawApproval approval = new WalletWithdrawApproval();
        approval.setMerchantId(merchantId);
        approval.setUserId(userId);
        approval.setAmount(amount);
        approval.setBankAccount(bankAccount);
        approval.setBankName(bankName);
        approval.setAccountName(accountName);
        approval.setWithdrawNo(withdrawNo);
        approval.setStatus("PENDING");
        approval.setRemark(remark);
        approvalMapper.insert(approval);
        return approvalMapper.selectById(approval.getId());
    }

    @Override
    public Page<WalletWithdrawApproval> pageApprovals(int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<WalletWithdrawApproval> wrapper = new LambdaQueryWrapper<WalletWithdrawApproval>()
                .orderByDesc(WalletWithdrawApproval::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(WalletWithdrawApproval::getStatus, status);
        }
        return approvalMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public WalletWithdrawApproval getById(Long id) {
        WalletWithdrawApproval approval = approvalMapper.selectById(id);
        if (approval == null) {
            throw new PaymentException("提现审批记录不存在");
        }
        return approval;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String operator) {
        WalletWithdrawApproval approval = getById(id);
        if (!"PENDING".equals(approval.getStatus())) {
            throw new PaymentException("仅待审批的记录可以审批通过");
        }
        approval.setStatus("APPROVED");
        approval.setOperator(operator);
        approval.setApproveTime(LocalDateTime.now());
        approvalMapper.updateById(approval);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String operator, String reason) {
        WalletWithdrawApproval approval = getById(id);
        if (!"PENDING".equals(approval.getStatus())) {
            throw new PaymentException("仅待审批的记录可以拒绝");
        }
        approval.setStatus("REJECTED");
        approval.setOperator(operator);
        approval.setRemark(reason);
        approval.setApproveTime(LocalDateTime.now());
        approvalMapper.updateById(approval);
    }

    private String generateBizNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
