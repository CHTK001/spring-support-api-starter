package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.channel.WithdrawRequest;
import com.chua.payment.support.channel.support.AbstractMerchantPaymentChannel;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.MerchantChannelMapper;
import com.chua.payment.support.provider.PaymentProviderGatewayRegistry;
import com.chua.payment.support.service.MerchantChannelService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.payment.support.service.WalletPayoutService;
import com.chua.starter.aliyun.support.payment.AliyunAlipayGateway;
import com.chua.starter.aliyun.support.payment.dto.AliyunAlipayTransferRequest;
import com.chua.starter.aliyun.support.payment.dto.AliyunAlipayTransferResponse;
import com.chua.starter.aliyun.support.properties.AliyunAlipayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 钱包打款服务：审批通过后实际转账到用户支付宝账号
 */
@Slf4j
@Service
public class WalletPayoutServiceImpl extends AbstractMerchantPaymentChannel implements WalletPayoutService {

    private final PaymentProviderGatewayRegistry providerGatewayRegistry;
    private final WalletOrderService walletOrderService;
    private final WalletAccountService walletAccountService;
    private final MerchantChannelMapper merchantChannelMapper;

    public WalletPayoutServiceImpl(MerchantChannelService merchantChannelService,
                                ObjectMapper objectMapper,
                                PaymentProviderGatewayRegistry providerGatewayRegistry,
                                WalletOrderService walletOrderService,
                                WalletAccountService walletAccountService,
                                MerchantChannelMapper merchantChannelMapper) {
        super(merchantChannelService, objectMapper);
        this.providerGatewayRegistry = providerGatewayRegistry;
        this.walletOrderService = walletOrderService;
        this.walletAccountService = walletAccountService;
        this.merchantChannelMapper = merchantChannelMapper;
    }

    /**
     * 执行支付宝打款，转账到用户支付宝账号
     *
     * @param request   提现请求（含用户支付宝账号信息）
     * @param orderNo   钱包订单号
     * @return true=打款成功, false=打款失败
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean payoutToAlipay(WithdrawRequest request, String orderNo) {
        if (!StringUtils.hasText(request.getBankAccount())) {
            log.warn("提现缺少收款账号，跳过支付宝打款: withdrawNo={}", request.getWithdrawNo());
            return false;
        }

        // 查找商户已启用的支付宝渠道
        List<MerchantChannel> alipayChannels = merchantChannelMapper.selectList(
                new LambdaQueryWrapper<MerchantChannel>()
                        .eq(MerchantChannel::getMerchantId, request.getMerchantId())
                        .eq(MerchantChannel::getChannelType, "ALIPAY")
                        .eq(MerchantChannel::getStatus, 1));
        if (alipayChannels.isEmpty()) {
            log.warn("商户未配置支付宝渠道，跳过支付宝打款: merchantId={}", request.getMerchantId());
            return false;
        }
        MerchantChannel channel = alipayChannels.get(0);

        try {
            AliyunAlipayGateway gateway = providerGatewayRegistry.aliyunAlipayGateway(providerSpi(channel, null));
            AliyunAlipayProperties properties = buildClientProperties(channel);

            AliyunAlipayTransferRequest transferRequest = AliyunAlipayTransferRequest.builder()
                    .outBizNo("WITHDRAW_" + request.getWithdrawNo())
                    .payeeAccount(request.getBankAccount())
                    .payeeName(request.getAccountName())
                    .amount(request.getAmount())
                    .remark("钱包提现-" + (StringUtils.hasText(request.getRemark()) ? request.getRemark() : request.getWithdrawNo()))
                    .build();

            AliyunAlipayTransferResponse response = gateway.transfer(properties, transferRequest);

            if (response.isSuccess()) {
                walletOrderService.markSuccess(orderNo, response.getTransferNo(),
                        "支付宝打款成功: " + response.getMessage());
                log.info("提现支付宝打款成功: withdrawNo={}, transferNo={}, amount={}",
                        request.getWithdrawNo(), response.getTransferNo(), request.getAmount());
                return true;
            } else {
                walletOrderService.markFailed(orderNo, response.getRawResponse(),
                        "支付宝打款失败: " + response.getMessage());
                // 解冻余额（打款失败退回）
                walletAccountService.cancelWithdraw(request.getMerchantId(), request.getUserId(),
                        request.getWithdrawNo(), request.getAmount(),
                        "支付宝打款失败: " + response.getMessage());
                log.warn("提现支付宝打款失败: withdrawNo={}, reason={}",
                        request.getWithdrawNo(), response.getMessage());
                return false;
            }
        } catch (Exception e) {
            walletOrderService.markFailed(orderNo, null, "支付宝打款异常: " + e.getMessage());
            walletAccountService.cancelWithdraw(request.getMerchantId(), request.getUserId(),
                    request.getWithdrawNo(), request.getAmount(),
                    "支付宝打款异常: " + e.getMessage());
            log.error("提现支付宝打款异常: withdrawNo={}", request.getWithdrawNo(), e);
            return false;
        }
    }

    private AliyunAlipayProperties buildClientProperties(MerchantChannel channel) {
        AliyunAlipayProperties properties = new AliyunAlipayProperties();
        properties.setAppId(requiredText(channel.getAppId(), "支付宝应用ID不能为空"));
        properties.setPrivateKey(requiredText(decryptValue(channel.getPrivateKey()), "支付宝私钥不能为空"));
        properties.setAlipayPublicKey(requiredText(channel.getPublicKey(), "支付宝公钥不能为空"));
        properties.setSandbox(channel.getSandboxMode() != null && channel.getSandboxMode() == 1);
        properties.setServerUrl(extText(channel, "serverUrl"));
        properties.setCharset(firstNonBlank(extText(channel, "charset"), "UTF-8"));
        properties.setFormat(firstNonBlank(extText(channel, "format"), "json"));
        properties.setSignType(firstNonBlank(extText(channel, "signType"), "RSA2"));
        return properties;
    }
}
