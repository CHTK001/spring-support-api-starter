package com.chua.starter.pay.support.handler.wechat.natives;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.emuns.TradeType;
import com.chua.starter.pay.support.entity.PayMerchantConfigWechat;
import com.chua.starter.pay.support.handler.PayQrCodeCreator;
import com.chua.starter.pay.support.pojo.PayOrderRequest;
import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.service.PayService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 支付配置处理器
 *
 * @author CH
 * @since 2024/12/30
 */
@Spi("wechat_native")
public final class WechatNativePayQrCodeCreator implements PayQrCodeCreator {
    final PayMerchantConfigWechat payMerchantConfigWechat;
    final PayService payService;

    public WechatNativePayQrCodeCreator(PayMerchantConfigWechat payMerchantConfigWechat, PayService payService) {
        this.payMerchantConfigWechat = payMerchantConfigWechat;
        this.payService = payService;
    }

    @Override
    public byte[] qrcode(PayQrCodeRequest request) {
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(payMerchantConfigWechat.getPayMerchantConfigWechatMchId())
                        .privateKeyFromPath(payMerchantConfigWechat.getPayMerchantConfigWechatPrivateKeyPath())
                        .merchantSerialNumber(payMerchantConfigWechat.getPayMerchantConfigWechatMchSerialNo())
                        .apiV3Key(payMerchantConfigWechat.getPayMerchantConfigWechatApiKeyV3())
                        .build();

        PayOrderRequest payOrderRequest = new PayOrderRequest();
        payOrderRequest.setMerchantCode(request.getMerchantCode());
        payOrderRequest.setPrice(request.getPrice());
        payOrderRequest.setTotalPrice(request.getPrice());
        payOrderRequest.setTradeType(TradeType.UNCERTAIN);
        payOrderRequest.setProductCode("P00000000");
        payOrderRequest.setProductName("测试");
        payOrderRequest.setOrigin("扫码");
        payOrderRequest.setUserId(payMerchantConfigWechat.getPayMerchantConfigTestAccount());
        ReturnResult<PayOrderResponse> order = payService.createOrder(payOrderRequest);
        if (!order.isOk()) {
            throw new RuntimeException(order.getMsg());
        }

        NativePayService service = new NativePayService.Builder().config(config).build();
        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(payMerchantConfigWechat.getPayMerchantConfigWechatAppId());
        prepayRequest.setMchid(payMerchantConfigWechat.getPayMerchantConfigWechatMchId());
        prepayRequest.setDescription("测试");
        prepayRequest.setOutTradeNo(order.getData().getPayMerchantOrderCode());
        prepayRequest.setNotifyUrl(payMerchantConfigWechat.getPayMerchantConfigWechatNotifyUrl());
        Amount amount = new Amount();
        amount.setTotal(payOrderRequest.getPrice().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).intValue());
        prepayRequest.setAmount(amount);
        PrepayResponse response = service.prepay(prepayRequest);
        // 生成支付链接
        String payUrl = response.getCodeUrl();


        // 输出为Base64字符串
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 使用ZXing生成二维码
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payUrl, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ImageIO.write(qrImage, "JPEG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
