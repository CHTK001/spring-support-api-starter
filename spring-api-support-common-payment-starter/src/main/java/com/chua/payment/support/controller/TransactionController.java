package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.entity.TransactionRecord;
import com.chua.payment.support.service.TransactionRecordService;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 交易流水控制器
 */
@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRecordService transactionRecordService;

    @GetMapping("/{id}")
    @Permission(value = "payment:transaction:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<TransactionRecord> getById(@PathVariable Long id) {
        TransactionRecord record = transactionRecordService.getById(id);
        return record != null ? ReturnResult.success(record) : ReturnResult.failure("流水记录不存在");
    }

    @GetMapping("/order/{orderNo}")
    @Permission(value = "payment:transaction:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<TransactionRecord>> listByOrderNo(@PathVariable String orderNo,
                                                               @RequestParam(defaultValue = "1") int pageNum,
                                                               @RequestParam(defaultValue = "10") int pageSize) {
        return ReturnResult.success(PageResult.of(transactionRecordService.listByOrderNo(orderNo, pageNum, pageSize)));
    }

    @GetMapping("/merchant/{merchantId}")
    @Permission(value = "payment:transaction:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<TransactionRecord>> listByMerchantId(@PathVariable Long merchantId,
                                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return ReturnResult.success(PageResult.of(transactionRecordService.listByMerchantId(merchantId, pageNum, pageSize)));
    }

    @GetMapping("/page")
    @Permission(value = "payment:transaction:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<TransactionRecord>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) Long merchantId,
                                                      @RequestParam(required = false) String orderNo,
                                                      @RequestParam(required = false) String transactionType,
                                                      @RequestParam(required = false) Integer status) {
        Page<TransactionRecord> page = transactionRecordService.page(pageNum, pageSize, merchantId, orderNo, transactionType, status);
        return ReturnResult.success(PageResult.of(page));
    }
}
