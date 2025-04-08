package com.chua.starter.pay.support.service.impl;

import com.chua.starter.common.support.exception.BusinessException;
import com.chua.starter.pay.support.constant.PayConstant;
import com.chua.starter.pay.support.order.QrCode;
import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import com.chua.starter.pay.support.service.PayMerchantService;
import com.chua.starter.pay.support.service.PayQrCodeService;
import com.chua.starter.pay.support.service.PayService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 微信支付
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class PayQrCodeServiceImpl implements PayQrCodeService {

    final RedissonClient redissonClient;
    final PayMerchantService payMerchantService;
    final PayService payService;

    @Override
    public ResponseEntity<byte[]> qrcode(PayQrCodeRequest request) {
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_PAY_QR_CODE);
        if (!rLock.tryLock()) {
            throw new BusinessException("请稍后重试");
        }

        rLock.lock(10, TimeUnit.SECONDS);
        try {
            QrCode qrCode = new QrCode(payMerchantService, payService, request);
            if (!qrCode.isOk()) {
                throw new BusinessException("请稍后重试");
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Content-Disposition", "attachment; filename=qrcode.jpg")
                    .body(qrCode.toResult())
                    ;
        } catch (Exception e) {
            throw new RuntimeException("退款操作失败，" + e.getLocalizedMessage());
        } finally {
            rLock.unlock();
        }
    }
}
