package com.chua.starter.pay.support.handler.wechat;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.entity.PayMerchantConfigWechat;
import com.chua.starter.pay.support.handler.TransferBatchService;
import com.chua.starter.pay.support.handler.entity.TransferRequest;
import com.chua.starter.pay.support.handler.entity.TransferResponse;
import com.chua.starter.pay.support.handler.entity.WechatTransferBatchStatus;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.transferbatch.model.InitiateBatchTransferRequest;
import com.wechat.pay.java.service.transferbatch.model.InitiateBatchTransferResponse;
import com.wechat.pay.java.service.transferbatch.model.TransferDetailInput;

import java.math.BigDecimal;
import java.util.List;

/**
 * 微信转账
 *
 * @author CH
 * @since 2025/4/16 9:25
 */
public class WechatTransferBatchService implements TransferBatchService {
    final PayMerchantConfigWechat payMerchantConfigWechat;

    public WechatTransferBatchService(PayMerchantConfigWechat payMerchantConfigWechat) {
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
        int number = transferRequest.getNumber();
        String description = transferRequest.getDescription();
        // 金额转换为分
        long amountInCent = amount.multiply(new BigDecimal("100")).longValue();

        // 构建转账明细
        TransferDetailInput transferDetail = new TransferDetailInput();
        transferDetail.setOutDetailNo(outTradeNo);
        transferDetail.setTransferAmount(amountInCent);
        transferDetail.setOpenid(transferRequest.getToUser());
        transferDetail.setTransferRemark(description);
        if (realName != null) {
            transferDetail.setUserName(realName);
        }
        // 构建批次请求
        InitiateBatchTransferRequest request = new InitiateBatchTransferRequest();
        request.setAppid((payMerchantConfigWechat.getPayMerchantConfigWechatAppId()));
        // 使用商户订单号作为批次号(单笔转账)
        request.setOutBatchNo(outTradeNo);
        request.setBatchName(outTradeNo + "单笔转账");
        request.setBatchRemark(description);
        request.setTotalAmount(amountInCent);
        // 单笔转账
        request.setTotalNum(number);
        request.setNotifyUrl(payMerchantConfigWechat.getPayMerchantConfigWechatTransferUrl()
                + "/" + outTradeNo
                + "/" + payMerchantConfigWechat.getPayMerchantConfigWechatId()
                + "/" + payMerchantConfigWechat.getPayMerchantId()
        );
        request.setTransferDetailList(List.of(transferDetail));
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setOutBatchNo(outTradeNo);
        try {
            com.wechat.pay.java.service.transferbatch.TransferBatchService transferBatchService = new com.wechat.pay.java.service.transferbatch.TransferBatchService.Builder().config(config).build();
            InitiateBatchTransferResponse initiateBatchTransferResponse = transferBatchService.initiateBatchTransfer(request);
            transferResponse.addBatch(TransferResponse.Batch.builder()
                    .batchId(initiateBatchTransferResponse.getBatchId())
                    .batchStatus(WechatTransferBatchStatus.valueOf(initiateBatchTransferResponse.getBatchStatus()))
                    .build()
            );
        } catch (ServiceException e) {
            // 获取错误码和错误信息
            String errorMessage = e.getErrorMessage();
            transferResponse.addBatch(TransferResponse.Batch.builder()
                    .batchStatus(WechatTransferBatchStatus.FAIL)
                    .error(errorMessage)
                    .build()
            );
        }

        return ReturnResult.ok(transferResponse);

    }
}
