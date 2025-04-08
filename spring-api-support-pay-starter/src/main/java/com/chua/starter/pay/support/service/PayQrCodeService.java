package com.chua.starter.pay.support.service;

import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import org.springframework.http.ResponseEntity;

/**
 * 微信支付
 *
 * @author CH
 */
public interface PayQrCodeService {
    /**
     * 二维码
     *
     * @param request
     * @return
     */
    ResponseEntity<byte[]> qrcode(PayQrCodeRequest request);
}
