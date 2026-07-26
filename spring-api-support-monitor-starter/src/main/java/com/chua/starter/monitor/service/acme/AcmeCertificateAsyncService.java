package com.chua.starter.monitor.service.acme;

import com.chua.starter.monitor.pojo.acme.AcmeApplyCertRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * ACME 证书异步申请服务（Stub 简化版）。
 * <p>
 * 本实现用于通过编译，不依赖 socketio、ssh、protocol 等可选 starter。
 * 实际功能会根据运行时类路径进行条件激活。
 *
 * @author CH
 * @since 2025-12-23
 */
@Slf4j
@Service
@NoArgsConstructor
@ConditionalOnClass(name = "com.chua.socket.support.session.SocketSessionTemplate")
public class AcmeCertificateAsyncService {

    /**
     * 异步申请证书
     *
     * @param certId  预创建的证书ID
     * @param request 申请请求
     * @param userId  用户ID（用于消息推送）
     */
    public void applyCertificateAsync(Integer certId, AcmeApplyCertRequest request, Integer userId) {
        log.warn("[ACME-ASYNC] stub 异步申请接口被调用, certId={}, domain={}, userId={}",
                certId, request != null ? request.getPrimaryDomain() : null, userId);
        CompletableFuture.runAsync(() -> {
        });
    }

    /**
     * 重新验证证书
     */
    public void retryValidationAsync(Integer certId, Integer userId) {
        log.warn("[ACME-ASYNC] stub 重新验证接口被调用, certId={}, userId={}", certId, userId);
        CompletableFuture.runAsync(() -> {
        });
    }
}