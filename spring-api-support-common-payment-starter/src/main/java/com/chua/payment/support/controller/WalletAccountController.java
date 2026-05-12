package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.channel.*;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.dto.WalletRechargeDTO;
import com.chua.payment.support.dto.WalletTransferDTO;
import com.chua.payment.support.dto.WalletWithdrawDTO;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.entity.WalletAccount;
import com.chua.payment.support.entity.WalletAccountLog;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.MerchantChannelService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.vo.WalletAccountVO;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 钱包账户控制器
 */
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletAccountController {

    private final WalletAccountService walletAccountService;
    private final MerchantChannelService merchantChannelService;
    private final PaymentChannelRegistry paymentChannelRegistry;

    @GetMapping("/account/page")
    @Permission(value = "payment:wallet:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<WalletAccount>> pageAccounts(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false) Long merchantId) {
        Page<WalletAccount> page = walletAccountService.pageAccounts(pageNum, pageSize, merchantId);
        return ReturnResult.success(PageResult.of(page));
    }

    @GetMapping("/account")
    @Permission(value = "payment:wallet:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<WalletAccountVO> getAccount(@RequestParam Long merchantId,
                                              @RequestParam Long userId) {
        return ReturnResult.success(walletAccountService.getAccount(merchantId, userId));
    }

    @PostMapping("/account/recharge")
    @Permission(value = "payment:wallet:recharge", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<RechargeResult> recharge(@RequestBody WalletRechargeDTO dto) {
        MerchantChannel channel = merchantChannelService.getWalletChannel(dto.getMerchantId());
        PaymentChannel paymentChannel = paymentChannelRegistry.getChannel("WALLET", "BALANCE");

        RechargeRequest request = new RechargeRequest();
        request.setRechargeNo(dto.getRechargeNo());
        request.setMerchantId(dto.getMerchantId());
        request.setUserId(dto.getUserId());
        request.setAmount(dto.getAmount());
        request.setOperator(dto.getOperator());
        request.setRemark(dto.getRemark());
        request.setCreateOrder(dto.getCreateOrder());

        RechargeResult result = paymentChannel.recharge(channel, request);
        return ReturnResult.success(result);
    }

    @PostMapping("/account/transfer")
    @Permission(value = "payment:wallet:transfer", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<TransferResult> transfer(@RequestBody WalletTransferDTO dto) {
        // 校验转账方式
        String method = dto.getTransferMethod();
        if (method == null || method.isEmpty()) {
            method = "WALLET";
        }
        if (!"WALLET".equals(method)) {
            throw new PaymentException("暂不支持" + method + "转账方式，当前仅支持 WALLET（钱包转账）");
        }

        MerchantChannel channel = merchantChannelService.getWalletChannel(dto.getMerchantId());
        PaymentChannel paymentChannel = paymentChannelRegistry.getChannel("WALLET", "BALANCE");

        TransferRequest request = new TransferRequest();
        request.setTransferNo(dto.getTransferNo());
        request.setMerchantId(dto.getMerchantId());
        request.setFromUserId(dto.getFromUserId());
        request.setToUserId(dto.getToUserId());
        request.setAmount(dto.getAmount());
        request.setOperator(dto.getOperator());
        request.setRemark(dto.getRemark());
        request.setTransferMethod(dto.getTransferMethod());

        TransferResult result = paymentChannel.transfer(channel, request);
        return ReturnResult.success(result);
    }

    @PostMapping("/account/withdraw")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<WithdrawResult> withdraw(@RequestBody WalletWithdrawDTO dto) {
        MerchantChannel channel = merchantChannelService.getWalletChannel(dto.getMerchantId());
        PaymentChannel paymentChannel = paymentChannelRegistry.getChannel("WALLET", "BALANCE");

        WithdrawRequest request = new WithdrawRequest();
        request.setWithdrawNo(dto.getWithdrawNo());
        request.setMerchantId(dto.getMerchantId());
        request.setUserId(dto.getUserId());
        request.setAmount(dto.getAmount());
        request.setBankAccount(dto.getBankAccount());
        request.setBankName(dto.getBankName());
        request.setAccountName(dto.getAccountName());
        request.setOperator(dto.getOperator());
        request.setRemark(dto.getRemark());

        WithdrawResult result = paymentChannel.withdraw(channel, request);
        return ReturnResult.success(result);
    }

    @GetMapping("/account/log/page")
    @Permission(value = "payment:wallet:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<WalletAccountLog>> pageLogs(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         @RequestParam(required = false) Long merchantId,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) String bizType,
                                                         @RequestParam(required = false) String bizNo,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Page<WalletAccountLog> page = walletAccountService.pageLogs(pageNum, pageSize, merchantId, userId, bizType, bizNo, startTime, endTime);
        return ReturnResult.success(PageResult.of(page));
    }

    @PutMapping("/account/{id}/freeze")
    @Permission(value = "payment:wallet:freeze", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> freeze(@PathVariable Long id) {
        walletAccountService.freeze(id);
        return ReturnResult.success();
    }

    @PutMapping("/account/{id}/unfreeze")
    @Permission(value = "payment:wallet:freeze", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> unfreeze(@PathVariable Long id) {
        walletAccountService.unfreeze(id);
        return ReturnResult.success();
    }
}
