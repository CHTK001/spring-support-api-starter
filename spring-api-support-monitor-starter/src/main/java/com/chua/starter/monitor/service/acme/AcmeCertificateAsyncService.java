package com.chua.starter.monitor.service.acme;

import com.chua.acme.support.AcmeCertificateResult;
import com.chua.acme.support.AcmeConnectionResult;
import com.chua.acme.support.AcmeProvider;
import com.chua.acme.support.AcmeValidationInfo;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.pojo.acme.AcmeApplyCertRequest;
import com.chua.starter.monitor.properties.AcmeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ACME 证书异步申请服务实现。
 * <p>
 * 在原 Stub 基础上补全真实能力：在后台线程中连接 ACME 服务器、发起证书申请或重新验证，
 * 并把申请/续签/验证过程中的状态变化通过 {@link SocketSessionTemplate} 推送给对应用户，
 * 实现「消息中心查看进度」的体验。当项目未启用 Socket 时推送能力自动降级为日志记录。
 *
 * @author CH
 * @since 2025-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcmeCertificateAsyncService {

    /**
     * Socket 推送事件名称，前端监听该事件获取证书申请/续签/验证进度
     */
    private static final String ACME_EVENT = "acme-cert-progress";

    /**
     * ACME 服务提供者扩展名（SPI 名称）
     */
    private static final String ACME_PROVIDER = "acme4j";

    /**
     * 证书状态：待验证（已提交申请，等待域名验证）
     */
    private static final String STATUS_VALIDATING = "validating";

    /**
     * 证书状态：已签发
     */
    private static final String STATUS_VALID = "valid";

    /**
     * 证书状态：失败
     */
    private static final String STATUS_FAILED = "failed";

    /**
     * 时间格式化，与证书表时间字段格式保持一致
     */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 证书 Mapper，用于回填申请结果与状态
     */
    private final MonitorAcmeCertificateMapper certificateMapper;

    /**
     * 账户 Mapper，用于读取 ACME 账户连接信息
     */
    private final MonitorAcmeAccountMapper accountMapper;

    /**
     * 订单 Mapper，用于持久化待验证信息（供前端展示验证文件）
     */
    private final MonitorAcmeOrderMapper orderMapper;

    /**
     * ACME 配置，读取续签提前天数等参数
     */
    private final AcmeProperties acmeProperties;

    /**
     * Socket 会话模板，用于向前端推送进度；项目未启用 Socket 时为 null（推送降级为日志）
     */
    @Autowired(required = false)
    private SocketSessionTemplate socketTemplate;

    /**
     * 异步申请证书，立即返回，真实工作在后台线程执行。
     *
     * @param certId  预创建的证书ID
     * @param request 申请请求
     * @param userId  用户ID（用于消息推送）
     */
    public void applyCertificateAsync(Integer certId, AcmeApplyCertRequest request, Integer userId) {
        CompletableFuture.runAsync(() -> doApply(certId, request, userId));
    }

    /**
     * 异步重新验证证书，立即返回，真实工作在后台线程执行。
     *
     * @param certId 证书ID
     * @param userId 用户ID（用于消息推送）
     */
    public void retryValidationAsync(Integer certId, Integer userId) {
        CompletableFuture.runAsync(() -> doRetry(certId, userId));
    }

    /**
     * 执行证书申请：连接 ACME 服务器、发起申请并处理结果。
     *
     * @param certId  证书ID
     * @param request 申请请求
     * @param userId  用户ID
     */
    private void doApply(Integer certId, AcmeApplyCertRequest request, Integer userId) {
        pushProgress(userId, "apply", "pending", "开始申请证书", baseMap(certId, null));
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            pushProgress(userId, "apply", STATUS_FAILED, "证书记录不存在: " + certId, baseMap(certId, null));
            return;
        }

        MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
        if (account == null) {
            updateFailed(cert, "ACME账户不存在");
            pushProgress(userId, "apply", STATUS_FAILED, "ACME账户不存在",
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
            return;
        }

        AcmeProvider provider = ServiceProvider.of(AcmeProvider.class).getNewExtension(ACME_PROVIDER);
        if (provider == null) {
            updateFailed(cert, "ACME 服务提供者不可用");
            pushProgress(userId, "apply", STATUS_FAILED, "ACME 服务提供者不可用",
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
            return;
        }

        try {
            AcmeConnectionResult conn = provider.connect(
                    account.getAcmeAccountServer(),
                    account.getAcmeAccountEmail(),
                    account.getAcmeAccountPrivateKeyPem(),
                    account.getAcmeAccountEabKid(),
                    account.getAcmeAccountEabHmacKey());
            if (!conn.isSuccess()) {
                updateFailed(cert, "连接 ACME 服务器失败: " + conn.getError());
                pushProgress(userId, "apply", STATUS_FAILED, "连接 ACME 服务器失败: " + conn.getError(),
                        baseMap(certId, cert.getAcmeCertPrimaryDomain()));
                return;
            }

            // 首次申请时账户私钥为自动生成，回写以便后续续签使用
            if (StringUtils.isNotEmpty(conn.getPrivateKeyPem())) {
                account.setAcmeAccountPrivateKeyPem(conn.getPrivateKeyPem());
                accountMapper.updateById(account);
            }

            List<String> domains = buildDomains(cert);
            String challenge = StringUtils.defaultString(cert.getAcmeCertChallengeType(), "HTTP-01");
            pushProgress(userId, "apply", STATUS_VALIDATING, "已连接 ACME 服务器，正在申请证书",
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));

            AcmeCertificateResult result = provider.requestCertificate(domains, challenge);
            applyResult(cert, result, userId, "apply", certId);
        } catch (Exception e) {
            log.error("[ACME-ASYNC] 申请证书异常, certId={}", certId, e);
            updateFailed(cert, shortMsg(e));
            pushProgress(userId, "apply", STATUS_FAILED, "申请异常: " + e.getMessage(),
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
        } finally {
            provider.close();
        }
    }

    /**
     * 执行重新验证：用户部署验证文件后再次发起请求以完成签发。
     *
     * @param certId 证书ID
     * @param userId 用户ID
     */
    private void doRetry(Integer certId, Integer userId) {
        pushProgress(userId, "retry", STATUS_VALIDATING, "开始重新验证", baseMap(certId, null));
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            pushProgress(userId, "retry", STATUS_FAILED, "证书记录不存在: " + certId, baseMap(certId, null));
            return;
        }
        if (!STATUS_VALIDATING.equals(cert.getAcmeCertStatus())) {
            pushProgress(userId, "retry", STATUS_FAILED, "证书状态不是待验证，无法重新验证",
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
            return;
        }

        MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
        if (account == null) {
            pushProgress(userId, "retry", STATUS_FAILED, "ACME账户不存在",
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
            return;
        }

        AcmeProvider provider = ServiceProvider.of(AcmeProvider.class).getNewExtension(ACME_PROVIDER);
        if (provider == null) {
            pushProgress(userId, "retry", STATUS_FAILED, "ACME 服务提供者不可用",
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
            return;
        }

        try {
            AcmeConnectionResult conn = provider.connect(
                    account.getAcmeAccountServer(),
                    account.getAcmeAccountEmail(),
                    account.getAcmeAccountPrivateKeyPem(),
                    account.getAcmeAccountEabKid(),
                    account.getAcmeAccountEabHmacKey());
            if (!conn.isSuccess()) {
                pushProgress(userId, "retry", STATUS_FAILED, "连接 ACME 服务器失败: " + conn.getError(),
                        baseMap(certId, cert.getAcmeCertPrimaryDomain()));
                return;
            }

            List<String> domains = buildDomains(cert);
            String challenge = StringUtils.defaultString(cert.getAcmeCertChallengeType(), "HTTP-01");
            AcmeCertificateResult result = provider.requestCertificate(domains, challenge);
            applyResult(cert, result, userId, "retry", certId);
        } catch (Exception e) {
            log.error("[ACME-ASYNC] 重新验证异常, certId={}", certId, e);
            updateFailed(cert, shortMsg(e));
            pushProgress(userId, "retry", STATUS_FAILED, "重新验证异常: " + e.getMessage(),
                    baseMap(certId, cert.getAcmeCertPrimaryDomain()));
        } finally {
            provider.close();
        }
    }

    /**
     * 统一处理申请/验证结果，并推送进度给用户。
     *
     * @param cert    证书实体（将被回填）
     * @param result  ACME 申请结果
     * @param userId  用户ID
     * @param stage   阶段标识（apply/retry）
     * @param certId  证书ID
     */
    private void applyResult(MonitorAcmeCertificate cert, AcmeCertificateResult result,
                             Integer userId, String stage, Integer certId) {
        String domain = cert.getAcmeCertPrimaryDomain();
        if (result.isSuccess()) {
            fillSuccess(cert, result);
            certificateMapper.updateById(cert);
            pushProgress(userId, stage, STATUS_VALID, "证书签发成功", baseMap(certId, domain));
            return;
        }
        if (result.isNeedsValidation()) {
            cert.setAcmeCertStatus(STATUS_VALIDATING);
            certificateMapper.updateById(cert);
            persistValidation(cert, result.getValidationInfos());
            pushProgress(userId, stage, STATUS_VALIDATING, "等待域名验证，请部署验证文件后点击重新验证",
                    validationMap(certId, domain, result.getValidationInfos()));
            return;
        }
        updateFailed(cert, result.getError());
        pushProgress(userId, stage, STATUS_FAILED, "申请失败: " + result.getError(), baseMap(certId, domain));
    }

    /**
     * 持久化待验证信息到订单表，供前端展示验证文件路径与内容。
     *
     * @param cert  证书实体
     * @param infos 验证信息列表
     */
    private void persistValidation(MonitorAcmeCertificate cert, List<AcmeValidationInfo> infos) {
        if (infos == null || infos.isEmpty()) {
            return;
        }
        MonitorAcmeOrder order = new MonitorAcmeOrder();
        order.setAcmeAccountId(cert.getAcmeAccountId());
        order.setAcmeCertId(cert.getAcmeCertId());
        order.setAcmeOrderStatus(STATUS_VALIDATING);
        order.setAcmeAuthzUrls(Json.toJson(infos));
        orderMapper.insert(order);
        log.info("[ACME-ASYNC] 持久化待验证信息, certId={}, count={}", cert.getAcmeCertId(), infos.size());
    }

    /**
     * 标记证书为失败状态并记录错误信息。
     *
     * @param cert  证书实体
     * @param error 错误信息
     */
    private void updateFailed(MonitorAcmeCertificate cert, String error) {
        cert.setAcmeCertStatus(STATUS_FAILED);
        cert.setAcmeCertLastError(shortMsg(error));
        certificateMapper.updateById(cert);
    }

    /**
     * 成功签发后回填证书信息到实体。
     *
     * @param target 目标证书实体
     * @param src    证书申请结果
     */
    private void fillSuccess(MonitorAcmeCertificate target, AcmeCertificateResult src) {
        target.setAcmeCertStatus(STATUS_VALID);
        if (src.getNotBefore() != null) {
            target.setAcmeCertNotBefore(src.getNotBefore().format(DATE));
        }
        if (src.getNotAfter() != null) {
            target.setAcmeCertNotAfter(src.getNotAfter().format(DATE));
            // 计划续签日期：到期前 N 天
            LocalDateTime renewAt = src.getNotAfter().minusDays(Math.max(acmeProperties.getRenewDaysBefore(), 7));
            target.setAcmeCertRenewAt(renewAt.format(DATE));
        }
        target.setAcmeCertChainPem(src.getCertificatePem());
        target.setAcmeCertKeyPem(src.getPrivateKeyPem());
        target.setAcmeCertLastError(null);
    }

    /**
     * 构建域名列表（主域名 + SAN）。
     *
     * @param cert 证书实体
     * @return 域名列表
     */
    private List<String> buildDomains(MonitorAcmeCertificate cert) {
        List<String> domains = new ArrayList<>();
        domains.add(cert.getAcmeCertPrimaryDomain());
        if (StringUtils.isNotEmpty(cert.getAcmeCertSan())) {
            for (String s : cert.getAcmeCertSan().split(",")) {
                if (StringUtils.isNotEmpty(s)) {
                    domains.add(s.trim());
                }
            }
        }
        return domains;
    }

    /**
     * 向前端推送进度。未配置 Socket 时降级为日志输出。
     *
     * @param userId   用户ID
     * @param stage    阶段（apply/retry）
     * @param status   当前状态
     * @param message  提示信息
     * @param extra    附加数据
     */
    private void pushProgress(Integer userId, String stage, String status, String message, Map<String, Object> extra) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stage", stage);
        payload.put("status", status);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());
        if (extra != null) {
            payload.putAll(extra);
        }
        if (socketTemplate != null && userId != null) {
            socketTemplate.sendToUserObject(String.valueOf(userId), ACME_EVENT, payload);
        } else {
            log.info("[ACME-ASYNC] 进度推送(userId={}, stage={}, status={}): {}",
                    userId, stage, status, message);
        }
    }

    /**
     * 构造基础附加数据（证书ID与域名）。
     *
     * @param certId 证书ID
     * @param domain 域名，可为空
     * @return 附加数据映射
     */
    private Map<String, Object> baseMap(Integer certId, String domain) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("certId", certId);
        if (domain != null) {
            map.put("domain", domain);
        }
        return map;
    }

    /**
     * 构造带验证信息的附加数据。
     *
     * @param certId 证书ID
     * @param domain 域名
     * @param infos  验证信息列表
     * @return 附加数据映射
     */
    private Map<String, Object> validationMap(Integer certId, String domain, List<AcmeValidationInfo> infos) {
        Map<String, Object> map = baseMap(certId, domain);
        map.put("validationInfos", infos);
        return map;
    }

    /**
     * 提取简短异常信息。
     *
     * @param e 异常
     * @return 简短信息
     */
    private String shortMsg(Exception e) {
        String msg = e.getMessage();
        return shortMsg(msg);
    }

    /**
     * 限制错误信息长度，避免超出数据库字段。
     *
     * @param error 原始错误信息
     * @return 限制长度后的信息
     */
    private String shortMsg(String error) {
        if (error == null) {
            return "";
        }
        return error.length() > 512 ? error.substring(0, 512) : error;
    }
}
