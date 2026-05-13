package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.channel.WithdrawRequest;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.entity.WalletWithdrawApproval;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.payment.support.service.WalletPayoutService;
import com.chua.payment.support.service.WalletWithdrawApprovalService;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钱包提现审批控制器
 */
@Slf4j
@RestController
@RequestMapping("/wallet/withdraw-approval")
@RequiredArgsConstructor
public class WalletWithdrawApprovalController {

    private final WalletWithdrawApprovalService walletWithdrawApprovalService;
    private final WalletAccountService walletAccountService;
    private final WalletOrderService walletOrderService;
    private final WalletPayoutService walletPayoutService;

    @GetMapping("/page")
    @Permission(value = "payment:wallet:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<WalletWithdrawApproval>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false) String status) {
        Page<WalletWithdrawApproval> page = walletWithdrawApprovalService.pageApprovals(pageNum, pageSize, status);
        return ReturnResult.success(PageResult.of(page));
    }

    @PostMapping("/{id}/approve")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> approve(@PathVariable Long id, @RequestParam(required = false) String operator) {
        WalletWithdrawApproval approval = walletWithdrawApprovalService.getById(id);
        if (!"PENDING".equals(approval.getStatus())) {
            throw new PaymentException("仅待审批的记录可以审批通过");
        }

        // 扣除冻结余额
        walletAccountService.confirmWithdraw(approval.getMerchantId(), approval.getUserId(),
                approval.getWithdrawNo(), approval.getAmount());

        // 创建提现订单并标记处理中
        WithdrawRequest request = buildWithdrawRequest(approval, operator);
        WalletOrder order = walletOrderService.createWithdrawOrder(request);
        walletOrderService.markProcessing(order.getOrderNo(), null, null);

        // 更新审批状态
        walletWithdrawApprovalService.approve(id, operator);

        // 尝试支付宝自动打款（不阻塞主流程）
        try {
            boolean ok = walletPayoutService.payoutToAlipay(request, order.getOrderNo());
            if (!ok) {
                log.warn("提现支付宝自动打款未执行（可能未配支付宝渠道），需人工处理: id={}", id);
            }
        } catch (Exception e) {
            log.error("提现支付宝自动打款异常（已回滚余额），需人工核查: id={}", id, e);
        }

        return ReturnResult.success();
    }

    @PostMapping("/{id}/reject")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> reject(@PathVariable Long id,
                                     @RequestParam(required = false) String operator,
                                     @RequestParam(required = false) String reason) {
        WalletWithdrawApproval approval = walletWithdrawApprovalService.getById(id);
        if (!"PENDING".equals(approval.getStatus())) {
            throw new PaymentException("仅待审批的记录可以拒绝");
        }

        // 解冻余额
        walletAccountService.cancelWithdraw(approval.getMerchantId(), approval.getUserId(),
                approval.getWithdrawNo(), approval.getAmount(),
                StringUtils.hasText(reason) ? reason : "审批拒绝");

        // 更新审批状态
        walletWithdrawApprovalService.reject(id, operator, reason);
        return ReturnResult.success();
    }

    @PostMapping("/cancel")
    @Permission(value = "payment:wallet:cancel", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> cancel(@RequestParam Long id,
                                     @RequestParam(required = false) String reason) {
        WalletWithdrawApproval approval = walletWithdrawApprovalService.getById(id);
        if (!"PENDING".equals(approval.getStatus())) {
            throw new PaymentException("仅待审批的提现可以取消");
        }

        // 解冻余额
        walletAccountService.cancelWithdraw(approval.getMerchantId(), approval.getUserId(),
                approval.getWithdrawNo(), approval.getAmount(),
                "用户主动取消" + (StringUtils.hasText(reason) ? ": " + reason : ""));

        // 标记拒绝
        walletWithdrawApprovalService.reject(id, String.valueOf(approval.getUserId()),
                "用户主动取消" + (StringUtils.hasText(reason) ? ": " + reason : ""));
        return ReturnResult.success();
    }

    @PostMapping("/{orderNo}/withdrawal/success")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> withdrawalSuccess(@PathVariable String orderNo,
                                                 @RequestParam String thirdPartyOrderNo,
                                                 @RequestParam(required = false) String responsePayload) {
        WalletOrder order = walletOrderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new PaymentException("钱包订单不存在");
        }
        walletOrderService.markSuccess(orderNo, thirdPartyOrderNo, responsePayload);
        return ReturnResult.success();
    }

    @PostMapping("/{orderNo}/withdrawal/fail")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> withdrawalFail(@PathVariable String orderNo,
                                              @RequestParam(required = false) String responsePayload,
                                              @RequestParam(required = false) String reason) {
        WalletOrder order = walletOrderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new PaymentException("钱包订单不存在");
        }
        walletOrderService.markFailed(orderNo, responsePayload, reason);
        return ReturnResult.success();
    }

    private WithdrawRequest buildWithdrawRequest(WalletWithdrawApproval approval, String operator) {
        WithdrawRequest request = new WithdrawRequest();
        request.setWithdrawNo(approval.getWithdrawNo());
        request.setMerchantId(approval.getMerchantId());
        request.setUserId(approval.getUserId());
        request.setAmount(approval.getAmount());
        request.setBankAccount(approval.getBankAccount());
        request.setBankName(approval.getBankName());
        request.setAccountName(approval.getAccountName());
        request.setOperator(operator);
        return request;
    }
}
