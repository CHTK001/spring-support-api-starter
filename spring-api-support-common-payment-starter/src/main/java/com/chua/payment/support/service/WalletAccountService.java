package com.chua.payment.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.payment.support.dto.WalletRechargeDTO;
import com.chua.payment.support.dto.WalletTransferDTO;
import com.chua.payment.support.dto.WalletWithdrawDTO;
import com.chua.payment.support.entity.WalletAccountLog;
import com.chua.payment.support.entity.WalletAccount;
import com.chua.payment.support.vo.WalletAccountVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包账户服务
 */
public interface WalletAccountService {

    WalletAccountVO getAccount(Long merchantId, Long userId);

    void ensureAccount(Long merchantId, Long userId);

    WalletAccountVO recharge(WalletRechargeDTO dto);

    WalletAccountVO transfer(WalletTransferDTO dto);

    WalletAccountVO withdraw(WalletWithdrawDTO dto);

    WalletAccountVO pay(Long merchantId, Long userId, BigDecimal amount, String orderNo, String operator, String remark);

    WalletAccountVO refund(Long merchantId, Long userId, BigDecimal amount, String refundNo, String operator, String remark);

    Page<WalletAccount> pageAccounts(int pageNum, int pageSize, Long merchantId);

    /**
     * 冻结钱包账户
     */
    void freeze(Long id);

    /**
     * 解冻钱包账户
     */
    void unfreeze(Long id);

    WalletAccountLog getLog(String bizType, Long merchantId, Long userId, String bizNo);

    Page<WalletAccountLog> pageLogs(int pageNum, int pageSize, Long merchantId, Long userId, String bizType, String bizNo, LocalDateTime startTime, LocalDateTime endTime);
}
