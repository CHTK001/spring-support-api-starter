package com.chua.starter.pay.support.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.rpc.RpcService;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.common.support.utils.JakartaValidationUtils;
import com.chua.starter.pay.support.checker.CreateOrderChecker;
import com.chua.starter.pay.support.configuration.PayListenerService;
import com.chua.starter.pay.support.constant.PayConstant;
import com.chua.starter.pay.support.emuns.TradeType;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.mapper.PayMerchantOrderMapper;
import com.chua.starter.pay.support.order.CreateOrder;
import com.chua.starter.pay.support.order.ReCreateOrder;
import com.chua.starter.pay.support.pojo.PayOrderRequest;
import com.chua.starter.pay.support.pojo.PayReOrderRequest;
import com.chua.starter.pay.support.pojo.PaySignCreateRequest;
import com.chua.starter.pay.support.pojo.PayUpdateOrderRequest;
import com.chua.starter.pay.support.properties.PayProperties;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.result.PaySignResponse;
import com.chua.starter.pay.support.service.*;
import com.chua.starter.pay.support.sign.CreateSign;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.Errors;

import java.util.concurrent.TimeUnit;

/**
 * 支付服务
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
@RpcService(interfaceClass = RpcPayService.class)
public class PayServiceImpl implements PayService {
    final PayMerchantService payMerchantService;
    final PayMerchantOrderMapper payMerchantOrderMapper;
    final PayMerchantOrderService payMerchantOrderService;
    final RedissonClient redissonClient;
    final PayListenerService payListenerService;
    final PayOrderService payOrderService;
    final TransactionTemplate transactionTemplate;
    final PayMerchantOrderWaterService payMerchantOrderWaterService;
    final ApplicationContext applicationContext;
    final PayProperties payProperties;

    @Override
    public ReturnResult<PayOrderResponse> createOrder(PayOrderRequest request) {
        Errors errors = JakartaValidationUtils.validate(request, AddGroup.class);
        if (errors.hasErrors()) {
            return ReturnResult.illegal(errors.getAllErrors().getFirst().getDefaultMessage());
        }

        TradeType tradeType = request.getTradeType();
        if (null == tradeType) {
            return ReturnResult.illegal("交易类型不能为空");
        }

        if (payProperties.isWhenPayCheckNoPayOrder() && payOrderService.hasNoPayOrder(request)) {
            return ReturnResult.illegal("有未支付订单, 请勿重复下单");
        }

        CreateOrderChecker createOrderChecker = ServiceProvider.of(CreateOrderChecker.class).getNewExtension(tradeType);
        ReturnResult<String> returnResult = createOrderChecker.check(request);
        if (!returnResult.isOk()) {
            return ReturnResult.illegal(returnResult.getMsg());
        }

        String orderId = request.getOrderId();
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_CREATE_PREFIX + tradeType.getName() + request.getProductCode() + orderId);
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单已存在, 请勿重复下单");
        }

        rLock.lock(1, TimeUnit.SECONDS);
        try {
            return new CreateOrder(transactionTemplate, payMerchantService, payMerchantOrderWaterService, payMerchantOrderMapper).create(request);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rLock.unlock();
            } catch (Throwable ignored) {
            }
        }
    }


    @Override
    public ReturnResult<PayOrderResponse> recreateOrder(PayReOrderRequest request) {
        Errors errors = JakartaValidationUtils.validate(request, AddGroup.class);
        if (errors.hasErrors()) {
            return ReturnResult.illegal(errors.getAllErrors().getFirst().getDefaultMessage());
        }

        RLock rLock = redissonClient.getLock(PayConstant.ORDER_RECREATE_PREFIX + request.getPayMerchantOrderCode());
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单已存在, 请勿重复下单");
        }

        rLock.lock(5, TimeUnit.SECONDS);
        try {
            return new ReCreateOrder(transactionTemplate, payMerchantService, payMerchantOrderWaterService, payMerchantOrderService).create(request);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public ReturnResult<PaySignResponse> createSign(PaySignCreateRequest request) {
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_SIGN_PREFIX + request.getPayMerchantCode());
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("签名正在生成, 请勿重复点击");
        }
        PayMerchantOrder payMerchantOrder = payMerchantOrderMapper.selectOne(Wrappers.lambdaQuery(PayMerchantOrder.class).eq(PayMerchantOrder::getPayMerchantOrderCode, request.getPayMerchantCode()));
        if (null == payMerchantOrder) {
            return ReturnResult.illegal("订单不存在");
        }
        rLock.lock(5, TimeUnit.SECONDS);
        try {
            return new CreateSign(transactionTemplate, payMerchantService, payMerchantOrderMapper).create(request, payMerchantOrder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public ReturnResult<PayOrderResponse> updateOrder(PayUpdateOrderRequest request, String userId) {
        Errors errors = JakartaValidationUtils.validate(request, AddGroup.class);
        if (errors.hasErrors()) {
            return ReturnResult.illegal(errors.getAllErrors().getFirst().getDefaultMessage());
        }

        RLock rLock = redissonClient.getLock(PayConstant.ORDER_RECREATE_PREFIX + request.getPayMerchantOrderCode());
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单已存在, 请勿重复下单");
        }

        rLock.lock(5, TimeUnit.SECONDS);

        try {
            PayMerchantOrder merchantOrder = payOrderService.getOne(request.getPayMerchantOrderCode(), userId);
            if (null == merchantOrder) {
                return ReturnResult.illegal("订单不存在");
            }

            merchantOrder.setPayMerchantOrderTradeType(request.getTradeType().getName());
            payMerchantOrderMapper.updateById(merchantOrder);
            PayOrderResponse payOrderResponse = new PayOrderResponse();
            payOrderResponse.setPayMerchantOrderCode(request.getPayMerchantOrderCode());
            return ReturnResult.ok(payOrderResponse);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
    }

}
