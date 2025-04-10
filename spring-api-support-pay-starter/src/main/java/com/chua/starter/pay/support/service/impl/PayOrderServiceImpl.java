package com.chua.starter.pay.support.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.rpc.RpcService;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.common.support.utils.JakartaValidationUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.pay.support.configuration.PayListenerService;
import com.chua.starter.pay.support.constant.PayConstant;
import com.chua.starter.pay.support.emuns.TradeType;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.handler.CallbackNotificationParser;
import com.chua.starter.pay.support.mapper.PayMerchantOrderMapper;
import com.chua.starter.pay.support.order.*;
import com.chua.starter.pay.support.pojo.*;
import com.chua.starter.pay.support.properties.PayProperties;
import com.chua.starter.pay.support.result.PayRefundResponse;
import com.chua.starter.pay.support.service.*;
import com.chua.starter.pay.support.transfer.UserIdTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.Errors;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 支付订单
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RpcService(interfaceClass = RpcPayService.class)
public class PayOrderServiceImpl implements PayOrderService {

    final PayMerchantService payMerchantService;
    final PayMerchantOrderMapper payMerchantOrderMapper;
    final PayMerchantOrderService payMerchantOrderService;
    final RedissonClient redissonClient;
    final PayListenerService payListenerService;
    final TransactionTemplate transactionTemplate;
    final PayMerchantOrderWaterService payMerchantOrderWaterService;
    final ApplicationContext applicationContext;
    final PayProperties payProperties;

    @Override
    public boolean hasNoPayOrder(PayOrderRequest request) {
        TradeType tradeType = request.getTradeType();
        if (null == tradeType) {
            throw new RuntimeException("交易类型不能为空");
        }
        UserIdTransfer transfer = ServiceProvider.of(UserIdTransfer.class).getNewExtension(tradeType);
        String newUserId = transfer.transfer(request.getUserId());
        return payMerchantOrderMapper.existsNoPayOrder(request, newUserId, payProperties.getCheckScope().name());
    }

    @Override
    public ReturnPageResult<PayMerchantOrder> order(PayReOrderQueryV1Request request) {
        Errors validate = JakartaValidationUtils.validate(request, SelectGroup.class);
        if (validate.hasErrors()) {
            return ReturnPageResult.illegal(validate.getAllErrors().get(0).getDefaultMessage());
        }
        return ReturnPageResultUtils.ok(payMerchantOrderMapper.reOrder(request.createPage(), request));
    }

    @Override
    public ReturnPageResult<PayMerchantOrder> order(Query<PayMerchantOrder> query, PayOrderV1Request request, Set<String> sysUserIds) {
        Errors validate = JakartaValidationUtils.validate(request, SelectGroup.class);
        if (validate.hasErrors()) {
            return ReturnPageResult.illegal(validate.getAllErrors().get(0).getDefaultMessage());
        }
        return ReturnPageResultUtils.ok(payMerchantOrderMapper.order(query.createPage(), request, sysUserIds));
    }

    @Override
    public ReturnResult<PayMerchantOrder> detail(String payMerchantOrderCode) {
        if (StringUtils.isEmpty(payMerchantOrderCode)) {
            return ReturnResult.illegal("订单号不能为空");
        }
        return ReturnResult.optional(payMerchantOrderMapper.selectOne(Wrappers.<PayMerchantOrder>lambdaQuery()
                                .eq(PayMerchantOrder::getPayMerchantOrderCode, payMerchantOrderCode)
                        )
                ).withErrorMessage("订单不存在")
                .asResult();
    }

    @Override
    public WechatOrderCallbackResponse notifyOrder(CallbackNotificationParser parser) {
        //订单号
        String id = parser.id();
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_CALLBACK_PREFIX + id);
        if (!rLock.tryLock()) {
            return new WechatOrderCallbackResponse("FAIL", "正在处理", null);
        }

        rLock.lock(1, TimeUnit.SECONDS);
        try {
            if (!parser.parser(payMerchantService, payMerchantOrderMapper)) {
                return new WechatOrderCallbackResponse("FAIL", "订单不存在", null);
            }

            OrderCallbackRequest request = parser.getRequest();
            UpdateOrder updateOrder = new UpdateOrder(payMerchantOrderMapper);
            if (request.getBusinessStatus() == OrderCallbackRequest.Status.FAILURE) {
                return updateOrder.failure(request, parser.getOrder());
            }

            PayMerchantOrder payMerchantOrder = parser.getOrder();
            WechatOrderCallbackResponse success = updateOrder.success(request, payMerchantOrder);
            payMerchantOrderWaterService.createOrderWater(payMerchantOrder);
            payListenerService.publish(payMerchantOrder);
            return success;
        } catch (Throwable e) {
            throw new RuntimeException("通知失败，订单处理异常");
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public ReturnResult<PayRefundResponse> cancel(PayRefundRequest refundRequest) {
        String payMerchantOrderCode = refundRequest.getPayMerchantOrderCode();
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_REFUND_PREFIX + payMerchantOrderCode);
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单正在关闭, 请勿重复操作");
        }

        rLock.lock(10, TimeUnit.SECONDS);
        try {
            ReturnResult<PayRefundResponse> update = new CancelOrder(transactionTemplate, payMerchantService, payMerchantOrderMapper).update(refundRequest);
            if (update.isOk()) {
                PayMerchantOrder payMerchantOrder = update.getData().getOrder();
                payMerchantOrderWaterService.createOrderWater(payMerchantOrder);
                payListenerService.publish(payMerchantOrder);
            }
            return update;
        } catch (Exception e) {
            throw new RuntimeException("退款操作失败，请稍后重试");
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public PayMerchantOrder getOne(String payMerchantOrderCode, String userId) {
        return payMerchantOrderMapper.selectOne(
                Wrappers.<PayMerchantOrder>lambdaQuery()
                        .eq(PayMerchantOrder::getPayMerchantOrderCode, payMerchantOrderCode)
                        .eq(PayMerchantOrder::getPayMerchantOrderUserId, userId)
                , false);
    }

    @Override
    public ReturnResult<String> newestNoPayOrder(String userId) {
        PayMerchantOrder merchantOrder = CollectionUtils.findFirst(payMerchantOrderMapper.selectList(
                Wrappers.<PayMerchantOrder>lambdaQuery()
                        .eq(PayMerchantOrder::getPayMerchantOrderUserId, userId)
                        .eq(PayMerchantOrder::getPayMerchantOrderTradeType, TradeType.UNCERTAIN.name())
                        .eq(PayMerchantOrder::getPayMerchantOrderStatus, "1000")
                        .orderByDesc(PayMerchantOrder::getCreateTime)));
        return ReturnResult.ok(null == merchantOrder ? null : merchantOrder.getPayMerchantOrderCode());
    }

    @Override
    public ReturnResult<PayRefundResponse> refund(PayRefundRequest refundRequest) {
        String payMerchantOrderCode = refundRequest.getPayMerchantOrderCode();
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_REFUND_PREFIX + payMerchantOrderCode);
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单正在退款, 请勿重复操作");
        }

        rLock.lock(10, TimeUnit.SECONDS);
        try {
            ReturnResult<PayRefundResponse> update = new RefundOrder(transactionTemplate, payMerchantService, payMerchantOrderMapper).update(refundRequest);
            if (update.isOk()) {
                PayMerchantOrder payMerchantOrder = update.getData().getOrder();
                payMerchantOrderWaterService.createOrderWater(payMerchantOrder);
                payListenerService.publish(payMerchantOrder);
            }
            return update;
        } catch (Exception e) {
            throw new RuntimeException("退款操作失败，" + e.getLocalizedMessage());
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public ReturnResult<PayRefundResponse> refundToWallet(PayRefundRequest request) {
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_REFUND_PREFIX);
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单正在退款, 请勿重复操作");
        }

        rLock.lock(10, TimeUnit.SECONDS);
        try {
            ReturnResult<PayRefundResponse> update = new RefundWalletOrder(transactionTemplate, payMerchantService, payMerchantOrderMapper).update(request);
            if (update.isOk()) {
                PayMerchantOrder payMerchantOrder = update.getData().getOrder();
                payMerchantOrderWaterService.createOrderWater(payMerchantOrder);
                payListenerService.publish(payMerchantOrder);
            }
            return update;
        } catch (Exception e) {
            throw new RuntimeException("退款操作失败，请稍后重试");
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public ReturnResult<PayRefundResponse> cancelToWallet(PayRefundRequest request) {
        RLock rLock = redissonClient.getLock(PayConstant.ORDER_REFUND_PREFIX);
        if (!rLock.tryLock()) {
            return ReturnResult.illegal("订单正在退款, 请勿重复操作");
        }

        rLock.lock(10, TimeUnit.SECONDS);
        try {
            ReturnResult<PayRefundResponse> update = new CancelWalletOrder(transactionTemplate, payMerchantService, payMerchantOrderMapper)
                    .update(request);
            if (update.isOk()) {
                PayMerchantOrder payMerchantOrder = update.getData().getOrder();
                payMerchantOrderWaterService.createOrderWater(payMerchantOrder);
                payListenerService.publish(payMerchantOrder);
            }
            return update;
        } catch (Exception e) {
            throw new RuntimeException("退款操作失败，请稍后重试");
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public WechatOrderCallbackResponse refundOrder(CallbackNotificationParser wechatOrderCallbackRequest) {
        return null;
    }

}
