package com.chua.starter.pay.support.handler.wechat;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.entity.PayMerchantConfigWechat;
import com.chua.starter.pay.support.handler.TransferBillsService;
import com.chua.starter.pay.support.handler.entity.*;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.cipher.PrivacyEncryptor;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.http.*;

import java.math.BigDecimal;
import java.util.List;

import static com.wechat.pay.java.core.util.GsonUtil.toJson;

/**
 * 微信转账
 *
 * @author CH
 * @since 2025/4/22 10:41
 */
public class WechatTransferBillsService implements TransferBillsService {
    final PayMerchantConfigWechat payMerchantConfigWechat;

    public WechatTransferBillsService(PayMerchantConfigWechat payMerchantConfigWechat) {
        this.payMerchantConfigWechat = payMerchantConfigWechat;
    }

    @Override
    public ReturnResult<TransferResponse> handle(TransferRequest transferRequest) {
        // 参数配置
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(payMerchantConfigWechat.getPayMerchantConfigWechatMchId())
                        .privateKeyFromPath(payMerchantConfigWechat.getPayMerchantConfigWechatPrivateKeyPath())
                        .merchantSerialNumber(payMerchantConfigWechat.getPayMerchantConfigWechatMchSerialNo())
                        .apiV3Key(payMerchantConfigWechat.getPayMerchantConfigWechatApiKeyV3())
                        .build();
        BigDecimal amount = transferRequest.getAmount();
        String realName = transferRequest.getRealName();
        String outTradeNo = transferRequest.getOutTradeNo();
        String description = transferRequest.getDescription();
        // 金额转换为分
        Integer amountInCent = amount.multiply(new BigDecimal("100")).intValue();
        TransferBillsRequest request = new TransferBillsRequest();
        request.setTransferAmount(amountInCent);
        request.setOutBillNo(outTradeNo);
        request.setTransferRemark(description);
        request.setTransferSceneId(transferRequest.getTransferSceneId());
        request.setUserName(realName);
        request.setOpenid(transferRequest.getToUser());
        request.setAppid(payMerchantConfigWechat.getPayMerchantConfigWechatAppId());
        request.setNotifyUrl(payMerchantConfigWechat.getPayMerchantConfigWechatTransferUrl()
                + "/" + outTradeNo
                + "/" + payMerchantConfigWechat.getPayMerchantConfigWechatId()
                + "/" + payMerchantConfigWechat.getPayMerchantId()
        );

        TransferBillsRequest.TransferSceneReportInfo item1 = new TransferBillsRequest.TransferSceneReportInfo();
        item1.setInfoType("活动名称");
        item1.setInfoContent("账号提现");
        TransferBillsRequest.TransferSceneReportInfo item2 = new TransferBillsRequest.TransferSceneReportInfo();
        item2.setInfoType("奖励说明");
        item2.setInfoContent("账号余额提现");

        request.setTransferSceneReportInfo(List.of(item1, item2));

        PrivacyEncryptor encryptor = config.createEncryptor();
        String requestPath = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills";
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader(Constant.ACCEPT, MediaType.APPLICATION_JSON.getValue());
        headers.addHeader(Constant.CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue());
        headers.addHeader(Constant.WECHAT_PAY_SERIAL, encryptor.getWechatpaySerial());
        HttpRequest httpRequest =
                new HttpRequest.Builder()
                        .httpMethod(HttpMethod.POST)
                        .url(requestPath)
                        .headers(headers)
                        .body(createRequestBody(request))
                        .build();
        HttpClient httpClient = new DefaultHttpClientBuilder().config(config).build();
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setOutBatchNo(outTradeNo);
        try {
            HttpResponse<TransferBillsResponse> httpResponse =
                    httpClient.execute(httpRequest, TransferBillsResponse.class);
            TransferBillsResponse serviceResponse = null;
            serviceResponse = httpResponse.getServiceResponse();
            transferResponse.addBatch(TransferResponse.Batch.builder()
                    .batchId(serviceResponse.getTransferBillNo())
                    .batchStatus(WechatTransferBatchStatus.valueOf(serviceResponse.getState()))
                    .build());
        } catch (ServiceException e) {
            // 获取错误码和错误信息
            String errorMessage = e.getErrorMessage().trim();
            transferResponse.addBatch(TransferResponse.Batch.builder()
                    .batchStatus(WechatTransferBatchStatus.FAIL)
                    .error(errorMessage)
                    .build()
            );
            return ReturnResult.error(errorMessage);
        }

        return ReturnResult.ok(transferResponse);

    }

    private RequestBody createRequestBody(Object request) {
        return new JsonRequestBody.Builder().body(toJson(request)).build();
    }
}
