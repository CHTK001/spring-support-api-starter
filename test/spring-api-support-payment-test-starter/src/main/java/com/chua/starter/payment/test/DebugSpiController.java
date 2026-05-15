package com.chua.starter.payment.test;

import com.chua.payment.support.dto.WalletRechargeDTO;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.service.PaymentOrderService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.payment.support.vo.OrderVO;
import com.chua.payment.support.channel.impl.AlipayWebPaymentChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugSpiController {

    private final AlipayWebPaymentChannel alipayWebPaymentChannel;
    private final PaymentOrderService paymentOrderService;
    private final WalletAccountService walletAccountService;
    private final WalletOrderService walletOrderService;

    @GetMapping("/gateway-check")
    public Map<String, Object> checkGateway() {
        Map<String, Object> result = new HashMap<>();

        // Check AlipayWebPaymentChannel class source
        Class<?> channelClass = alipayWebPaymentChannel.getClass();
        result.put("channelClass", channelClass.getName());
        result.put("channelClassLoader", channelClass.getClassLoader().toString());
        result.put("channelCodeSource", channelClass.getProtectionDomain().getCodeSource().getLocation().toString());

        return result;
    }

    /**
     * 调试端点：手动完成钱包充值流程（绕过支付宝回调）
     */
    @GetMapping("/complete-recharge")
    public Map<String, Object> completeRecharge(@RequestParam Long orderId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询支付订单
        OrderVO order = paymentOrderService.getOrder(orderId);
        if (order == null) {
            result.put("error", "支付订单不存在: " + orderId);
            return result;
        }
        result.put("orderNo", order.getOrderNo());
        result.put("businessOrderNo", order.getBusinessOrderNo());
        result.put("currentStatus", order.getStatus());

        // 2. 检查是否为钱包充值
        if (order.getBusinessOrderNo() == null || !order.getBusinessOrderNo().startsWith("WALLET_RECHARGE_")) {
            result.put("error", "非钱包充值订单");
            return result;
        }

        String walletOrderNo = order.getBusinessOrderNo().substring("WALLET_RECHARGE_".length());
        result.put("walletOrderNo", walletOrderNo);

        // 3. 查询钱包订单
        WalletOrder walletOrder = walletOrderService.getByOrderNo(walletOrderNo);
        if (walletOrder == null) {
            result.put("error", "钱包订单不存在: " + walletOrderNo);
            return result;
        }
        result.put("walletOrderStatus", walletOrder.getStatus());

        // 4. 标记支付订单为 PAID
        boolean paid = paymentOrderService.paySuccess(orderId, order.getOrderAmount(), "DEBUG_" + System.currentTimeMillis(), "debug");
        result.put("paySuccess", paid);

        // 5. 钱包充值
        WalletRechargeDTO rechargeDTO = WalletRechargeDTO.builder()
                .merchantId(order.getMerchantId())
                .userId(order.getUserId())
                .rechargeNo(walletOrderNo)
                .amount(order.getOrderAmount())
                .operator("debug")
                .remark("调试充值到账")
                .build();
        var accountVO = walletAccountService.recharge(rechargeDTO);
        result.put("rechargeAmount", order.getOrderAmount());
        result.put("availableBalance", accountVO.getAvailableBalance());

        // 6. 标记钱包订单成功
        walletOrderService.markSuccess(walletOrderNo, "DEBUG_TRADE_NO", "调试充值到账");

        // 7. 返回最终结果
        result.put("success", true);
        result.put("message", "钱包充值流程已完成");

        // 8. 查询最新的钱包余额
        var finalAccount = walletAccountService.getAccount(order.getMerchantId(), order.getUserId());
        result.put("finalAvailableBalance", finalAccount.getAvailableBalance());
        result.put("finalFrozenBalance", finalAccount.getFrozenBalance());

        return result;
    }
}
