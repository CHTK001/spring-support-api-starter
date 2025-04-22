package com.chua.starter.pay.support.handler.entity;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 转账响应
 *
 * @author CH
 * @since 2025/4/16 9:34
 */
@Data
public class TransferResponse {

    /**
     * 转账批次号
     */
    private String outBatchNo;
    /**
     * 转账批次
     */
    private List<Batch> data;

    public TransferResponse() {
        this.data = new LinkedList<>();
    }

    /**
     * 添加转账批次
     *
     * @param batch 转账批次
     * @return this
     */
    public TransferResponse addBatch(Batch batch) {
        data.add(batch);
        return this;
    }

    /**
     * 转账批次
     */
    @Data
    @Builder
    public static class Batch {

        /**
         * 转账批次号
         */
        private String batchId;

        /**
         * 转账批次错误原因
         */
        private String error;
        /**
         * 转账批次状态
         */
        private WechatTransferBatchStatus batchStatus;
    }
}
