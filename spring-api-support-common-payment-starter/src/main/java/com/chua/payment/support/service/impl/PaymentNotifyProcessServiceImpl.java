package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.entity.PaymentNotifyError;
import com.chua.payment.support.entity.PaymentNotifyLog;
import com.chua.payment.support.event.PaymentNotifyFailedEvent;
import com.chua.payment.support.event.PaymentNotifyReceivedEvent;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.PaymentNotifyErrorMapper;
import com.chua.payment.support.mapper.PaymentNotifyLogMapper;
import com.chua.payment.support.service.PaymentNotifyProcessService;
import com.chua.payment.support.service.PaymentNotifyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Enumeration;

/**
 * 支付回调处理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotifyProcessServiceImpl implements PaymentNotifyProcessService {

    private final PaymentNotifyLogMapper notifyLogMapper;
    private final PaymentNotifyErrorMapper notifyErrorMapper;
    private final PaymentNotifyService paymentNotifyService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentNotifyLog logNotify(String notifyType, Long merchantId, String channelType, String channelSubType,
                                       HttpServletRequest request, String body) {
        PaymentNotifyLog log = new PaymentNotifyLog();
        log.setNotifyType(notifyType);
        log.setMerchantId(merchantId);
        log.setChannelType(channelType);
        log.setChannelSubType(channelSubType);
        log.setRequestHeaders(extractHeaders(request));
        log.setRequestBody(body);
        log.setRequestParams(extractParams(request));
        log.setProcessStatus("PENDING");
        log.setRetryCount(0);
        log.setReceivedAt(LocalDateTime.now());
        notifyLogMapper.insert(log);

        eventPublisher.publishEvent(new PaymentNotifyReceivedEvent(
            this, log.getTenantId(), log.getMerchantId(), log.getId(),
            notifyType, channelType, channelSubType,
            log.getOrderNo(), log.getRefundNo(), log.getSignVerified()
        ));

        return log;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPaymentNotify(PaymentNotifyLog log) {
        try {
            log.setProcessStatus("PROCESSING");
            log.setProcessedAt(LocalDateTime.now());
            notifyLogMapper.updateById(log);

            paymentNotifyService.handlePaymentNotify(log.getChannelType(), log.getChannelSubType(), log.getRequestBody());

            markSuccess(log.getId(), "支付回调处理成功");
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            markFailedAndRecordError(log.getId(), e.getClass().getSimpleName(), e.getMessage(), getStackTrace(e));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefundNotify(PaymentNotifyLog log) {
        try {
            log.setProcessStatus("PROCESSING");
            log.setProcessedAt(LocalDateTime.now());
            notifyLogMapper.updateById(log);

            paymentNotifyService.handleRefundNotify(log.getChannelType(), log.getChannelSubType(), log.getRequestBody());

            markSuccess(log.getId(), "退款回调处理成功");
        } catch (Exception e) {
            log.error("处理退款回调失败", e);
            markFailedAndRecordError(log.getId(), e.getClass().getSimpleName(), e.getMessage(), getStackTrace(e));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(Long logId, String result) {
        PaymentNotifyLog log = notifyLogMapper.selectById(logId);
        if (log != null) {
            log.setProcessStatus("SUCCESS");
            log.setProcessResult(result);
            log.setProcessedAt(LocalDateTime.now());
            notifyLogMapper.updateById(log);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailedAndRecordError(Long logId, String errorType, String errorMessage, String errorStack) {
        PaymentNotifyLog log = notifyLogMapper.selectById(logId);
        if (log == null) {
            return;
        }

        log.setProcessStatus("FAILED");
        log.setErrorMessage(errorMessage);
        log.setRetryCount(log.getRetryCount() + 1);
        notifyLogMapper.updateById(log);

        PaymentNotifyError error = new PaymentNotifyError();
        error.setNotifyLogId(logId);
        error.setNotifyType(log.getNotifyType());
        error.setTenantId(log.getTenantId());
        error.setMerchantId(log.getMerchantId());
        error.setOrderNo(log.getOrderNo());
        error.setRefundNo(log.getRefundNo());
        error.setErrorType(errorType);
        error.setErrorMessage(errorMessage);
        error.setErrorStack(errorStack);
        error.setRequestData(log.getRequestBody());
        error.setRetryCount(0);
        error.setMaxRetryCount(5);
        error.setNextRetryTime(calculateNextRetryTime(0));
        error.setStatus("PENDING");
        notifyErrorMapper.insert(error);

        eventPublisher.publishEvent(new PaymentNotifyFailedEvent(
            this, log.getTenantId(), log.getMerchantId(), logId,
            error.getId(), log.getNotifyType(), log.getOrderNo(), log.getRefundNo(),
            errorType, errorMessage
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryFailedNotify(Long errorId) {
        PaymentNotifyError error = notifyErrorMapper.selectById(errorId);
        if (error == null || !"PENDING".equals(error.getStatus())) {
            return;
        }

        if (error.getRetryCount() >= error.getMaxRetryCount()) {
            error.setStatus("ABANDONED");
            error.setRemark("超过最大重试次数");
            notifyErrorMapper.updateById(error);
            return;
        }

        error.setStatus("PROCESSING");
        notifyErrorMapper.updateById(error);

        try {
            PaymentNotifyLog log = notifyLogMapper.selectById(error.getNotifyLogId());
            if (log == null) {
                throw new PaymentException("回调日志不存在");
            }

            if ("PAYMENT".equals(error.getNotifyType())) {
                processPaymentNotify(log);
            } else if ("REFUND".equals(error.getNotifyType())) {
                processRefundNotify(log);
            }

            error.setStatus("RESOLVED");
            error.setResolvedAt(LocalDateTime.now());
            notifyErrorMapper.updateById(error);
        } catch (Exception e) {
            log.error("重试回调处理失败", e);
            error.setStatus("PENDING");
            error.setRetryCount(error.getRetryCount() + 1);
            error.setNextRetryTime(calculateNextRetryTime(error.getRetryCount()));
            error.setErrorMessage(e.getMessage());
            notifyErrorMapper.updateById(error);
        }
    }

    private String extractHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            headers.append(name).append(": ").append(value).append("\n");
        }
        return headers.toString();
    }

    private String extractParams(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> {
            params.append(key).append("=").append(String.join(",", values)).append("&");
        });
        return params.toString();
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        int[] delays = {1, 5, 15, 30, 60};
        int delayMinutes = retryCount < delays.length ? delays[retryCount] : 60;
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }
}
