package com.chua.starter.pay.support.order;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.pay.support.emuns.TradeType;
import com.chua.starter.pay.support.entity.PayMerchant;
import com.chua.starter.pay.support.handler.PayConfigDetector;
import com.chua.starter.pay.support.handler.PayQrCodeCreator;
import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import com.chua.starter.pay.support.service.PayMerchantService;
import com.chua.starter.pay.support.service.PayService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 二维码
 *
 * @author CH
 */
public class QrCode {
    private final PayMerchantService payMerchantService;
    private final PayService payService;
    private final PayQrCodeRequest request;
    private final AtomicBoolean success = new AtomicBoolean(false);

    public QrCode(PayMerchantService payMerchantService, PayService payService, PayQrCodeRequest request) {
        this.payMerchantService = payMerchantService;
        this.payService = payService;
        this.request = request;
    }

    /**
     * 是否成功
     *
     * @return 是否成功
     */
    public boolean isOk() {
        return true;
    }

    public byte[] toResult() {
        TradeType tradeType = request.getTradeType();
        ReturnResult<PayMerchant> payMerchantValue = payMerchantService.getOneByCode(request.getMerchantCode());
        if (null == payMerchantValue || !payMerchantValue.isOk()) {
            throw new RuntimeException("商户不存在");
        }
        PayMerchant payMerchant = payMerchantValue.getData();

        PayConfigDetector<?> payConfigDetector = ServiceProvider.of(PayConfigDetector.class).getNewExtension(tradeType);
        if (null == payConfigDetector) {
            throw new RuntimeException("当前系统不支持该下单方式");
        }
        ReturnResult<?> checked = payConfigDetector.check(payMerchant, tradeType);
        if (!checked.isOk()) {
            throw new RuntimeException(checked.getMsg());
        }

        PayQrCodeCreator creator = ServiceProvider.of(PayQrCodeCreator.class).getNewExtension(tradeType, checked.getData(), payService);
        return creator.qrcode(request);
    }
}
