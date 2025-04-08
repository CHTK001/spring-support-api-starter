package com.chua.starter.pay.support.handler;

import com.chua.starter.pay.support.pojo.PayQrCodeRequest;

/**
 * 支付二维码创建者
 *
 * @author CH
 */
public interface PayQrCodeCreator {
    /**
     * 创建二维码
     *
     * @param request 请求
     * @return 二维码
     */
    byte[] qrcode(PayQrCodeRequest request);
}
