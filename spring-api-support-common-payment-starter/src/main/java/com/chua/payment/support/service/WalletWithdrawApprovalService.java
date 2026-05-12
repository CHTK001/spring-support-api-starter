package com.chua.payment.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.payment.support.entity.WalletWithdrawApproval;

import java.math.BigDecimal;

/**
 * 钱包提现审批服务
 */
public interface WalletWithdrawApprovalService {

    /**
     * 创建提现审批记录
     */
    WalletWithdrawApproval createApproval(Long merchantId, Long userId, BigDecimal amount,
                                          String bankAccount, String bankName, String accountName,
                                          String withdrawNo, String remark);

    /**
     * 分页查询审批列表
     */
    Page<WalletWithdrawApproval> pageApprovals(int pageNum, int pageSize, String status);

    /**
     * 根据ID查询
     */
    WalletWithdrawApproval getById(Long id);

    /**
     * 通过审批
     */
    void approve(Long id, String operator);

    /**
     * 拒绝审批
     */
    void reject(Long id, String operator, String reason);
}
