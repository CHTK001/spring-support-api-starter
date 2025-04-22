package com.chua.starter.pay.support.controller;

import com.chua.starter.pay.support.entity.PayMerchantConfigWechat;
import com.chua.starter.pay.support.handler.entity.TransferBatchEntity;
import com.chua.starter.pay.support.handler.utils.CertificateLoader;
import com.chua.starter.pay.support.handler.wechat.handler.TransferNotifyHandler;
import com.chua.starter.pay.support.service.PayMerchantConfigWechatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

/**
 * 转账回调
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "转账回调")
@Tag(name = "转账回调")
@RestController
@RequestMapping("/v3/pay/callback/wechat/transfer")
@Slf4j
@RequiredArgsConstructor
public class WechatPayTransferCallbackController {

    final PayMerchantConfigWechatService payMerchantConfigWechatService;
    final ApplicationContext applicationContext;

    @PostMapping(value = "/notify/{outTradeNo}/{payMerchantConfigWechatId}/{payMerchantId}")
    @ApiOperation("订单结果通知")
    public String refundOrder(HttpServletRequest request,
                              @PathVariable("outTradeNo") String outTradeNo,
                              @PathVariable("payMerchantConfigWechatId") String payMerchantConfigWechatId,
                              @PathVariable("payMerchantId") String payMerchantId,
                              @RequestHeader("Wechatpay-Signature") String signature,
                              @RequestHeader("Wechatpay-Timestamp") String timestamp,
                              @RequestHeader("Wechatpay-Nonce") String nonce,
                              @RequestHeader("Wechatpay-Serial") String serial,
                              @RequestBody String requestBody) {

        PayMerchantConfigWechat configWechat = payMerchantConfigWechatService.getById(payMerchantConfigWechatId);
        if (null == configWechat) {
            return "SUCCESS";
        }

        try {
            TransferBatchEntity batch = new TransferNotifyHandler(
                    configWechat.getPayMerchantConfigWechatApiKeyV3(),
                    CertificateLoader.loadFromFileSystem(configWechat.getPayMerchantConfigWechatPrivateKeyPath())
            ).handleNotify(
                    signature,
                    timestamp,
                    nonce,
                    serial,
                    requestBody
            );

            applicationContext.publishEvent(batch);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            log.error("转账回调失败:{}加载证书失败{}", configWechat.getPayMerchantConfigWechatPrivateKeyPath(), e);
        }
        return "SUCCESS";
    }
}
