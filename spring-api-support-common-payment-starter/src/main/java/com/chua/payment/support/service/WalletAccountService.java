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

    /**
     * 转账（参数版）
     */
    void transfer(Long merchantId,
                  Long fromUserId,
                  Long toUserId,
                  BigDecimal amount,
                  String transferNo,
                  String operator,
                  String remark);

    WalletAccountVO withdraw(WalletWithdrawDTO dto);

    /**
     * 冻结余额（提现专用）
     * available_balance → frozen_balance，不立即扣减
     */
    WalletAccountVO freezeForWithdraw(WalletWithdrawDTO dto);

    /**
     * 确认提现到账（扣除冻结余额）
     */
    void confirmWithdraw(Long merchantId, Long userId, String bizNo, BigDecimal amount);

    /**
     * 取消提现（释放冻结余额回可用余额）
     */
    void cancelWithdraw(Long merchantId, Long userId, String bizNo, BigDecimal amount, String remark);

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
