package com.chua.starter.pay.support.handler.wallet;

import com.chua.common.support.annotations.Spi;
import com.chua.starter.pay.support.handler.PayQrCodeCreator;
import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
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
    @Override
    public byte[] qrcode(PayQrCodeRequest request) {
//        // 创建JSAPI支付订单
//        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
//        request.setBody("商品描述");
//        request.setOutTradeNo("订单号");
//        request.setTotalFee(1); // 金额(分)
//        request.setSpbillCreateIp("127.0.0.1");
//        request.setNotifyUrl("回调地址");
//        request.setTradeType("JSAPI");
//        request.setOpenid("用户openid"); // JSAPI必须设置openid
//
//        // 调用统一下单接口
//        WxPayUnifiedOrderResult result = wxPayService.unifiedOrder(request);
//        String prepayId = result.getPrepayId();
//
//        // 生成支付参数
//        Map<String, String> payInfo = wxPayService.createPayInfo(prepayId);
//        String payUrl = "weixin://wxpay/bizpayurl?" + buildUrlParams(payInfo);

        String payUrl = request.getWalletCallbackUrl();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 生成二维码
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payUrl, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            // 输出图片
            ImageIO.write(qrImage, "JPEG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
