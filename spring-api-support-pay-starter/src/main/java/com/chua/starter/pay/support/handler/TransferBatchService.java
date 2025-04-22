package com.chua.starter.pay.support.handler;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.handler.entity.TransferRequest;
import com.chua.starter.pay.support.handler.entity.TransferResponse;

/**
 * 转账
 *
 * @author CH
 * @since 2025/4/16 9:25
 */
public interface TransferBatchService {


    /**
     * 转账
     *
     * @param transferRequest 转账请求
     * @return 转账结果
     */
    ReturnResult<TransferResponse> handle(TransferRequest transferRequest);
}
