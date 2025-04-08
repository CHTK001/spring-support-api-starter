package com.chua.starter.pay.support.handler.wallet;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.pay.support.emuns.TradeType;
import com.chua.starter.pay.support.entity.PayMerchant;
import com.chua.starter.pay.support.handler.PayQrCodeCreator;
import com.chua.starter.pay.support.pojo.PayOrderRequest;
import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.service.PayService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * 钱包二维码
 *
 * @author CH
 */
@Spi("wallet")
public class WalletPayQrCodeCreator implements PayQrCodeCreator {
    final PayMerchant payMerchant;
    final PayService payService;

    public WalletPayQrCodeCreator(PayMerchant payMerchant, PayService payService) {
        this.payMerchant = payMerchant;
        this.payService = payService;
    }
    @Override
    public byte[] qrcode(PayQrCodeRequest request) {
        PayOrderRequest payOrderRequest = new PayOrderRequest();
        payOrderRequest.setMerchantCode(request.getMerchantCode());
        payOrderRequest.setPrice(request.getPrice());
        payOrderRequest.setTotalPrice(request.getPrice());
        payOrderRequest.setTradeType(TradeType.WALLET);
        payOrderRequest.setProductCode("P00000000");
        payOrderRequest.setOrderId(IdUtils.createUlid());
        payOrderRequest.setProductName("测试");
        payOrderRequest.setOrigin("扫码");
        ReturnResult<PayOrderResponse> order = payService.createOrder(payOrderRequest);
        // 生成支付链接
        String payUrl = order.getMsg();
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
