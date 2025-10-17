package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.acme.support.client.AcmeClient;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.acme.AcmeCertificate;
import com.chua.common.support.lang.acme.AcmeCertificateStatus;
import com.chua.common.support.lang.acme.AcmeValidationResult;
import com.chua.common.support.lang.acme.AcmeValidationType;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.pojo.AcmeApplyCertRequest;
import com.chua.starter.monitor.properties.AcmeProperties;
import com.chua.starter.monitor.service.AcmeCertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ACME 证书服务实现
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcmeCertificateServiceImpl implements AcmeCertificateService {

    private final MonitorAcmeAccountMapper accountMapper;
    private final MonitorAcmeCertificateMapper certificateMapper;
    private final MonitorAcmeOrderMapper orderMapper;
    private final AcmeProperties acmeProperties;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 保存/更新 ACME 账户 ForCert
     *
     * @param account 账户
     * @return 结果（账户ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> saveAccountForCert(MonitorAcmeAccount account) {
        if (account == null || StringUtils.isNullOrEmpty(account.getAcmeAccountEmail())) {
            return ReturnResult.illegal("参数错误: acmeAccountEmail 不能为空");
        }
        if (StringUtils.isNullOrEmpty(account.getAcmeAccountServer())) {
            return ReturnResult.illegal("参数错误: acmeAccountServer 不能为空");
        }
        log.info("[ACME] 保存账户: email={}, server={}", account.getAcmeAccountEmail(), account.getAcmeAccountServer());
        if (account.getAcmeAccountId() == null) {
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
        return ReturnResult.ok(account.getAcmeAccountId());
    }

    /**
     * 申请证书 ForCert
     *
     * @param request 请求
     * @return 证书ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> applyCertificateForCert(AcmeApplyCertRequest request) {
        if (request == null || request.getAcmeAccountId() == null || StringUtils.isNullOrEmpty(request.getPrimaryDomain())) {
            return ReturnResult.illegal("参数错误: acmeAccountId/primaryDomain 不能为空");
        }
        // 查询账户信息
        MonitorAcmeAccount account = accountMapper.selectById(request.getAcmeAccountId());
        if (account == null) {
            return ReturnResult.illegal("ACME账户不存在: " + request.getAcmeAccountId());
        }

        List<String> sans = request.getSanDomains() == null ? List.of() : request.getSanDomains();
        String sanStr = sans.stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
        String challenge = StringUtils.defaultString(request.getChallengeType(), "HTTP-01");

        // 新建证书记录（pending）
        MonitorAcmeCertificate cert = new MonitorAcmeCertificate();
        cert.setAcmeAccountId(request.getAcmeAccountId());
        cert.setAcmeCertPrimaryDomain(request.getPrimaryDomain());
        cert.setAcmeCertSan(sanStr);
        cert.setAcmeCertChallengeType(challenge);
        cert.setAcmeCertStatus("pending");
        certificateMapper.insert(cert);
        int certId = cert.getAcmeCertId();
n        // 组装域名列表
        List<String> domains = new ArrayList<>();
        domains.add(request.getPrimaryDomain());
        if (!sans.isEmpty()) {
            domains.addAll(sans.stream().filter(StringUtils::isNotEmpty).toList());
        }
        AcmeValidationType validationType = mapValidationType(challenge);

        log.info("[ACME] 准备申请证书, certId={}, domain={}, san={}, type={}", certId, cert.getAcmeCertPrimaryDomain(), sanStr, validationType);

        // 初始化 ACME 客户端并连接
        AcmeClient client = new AcmeClient(account.getAcmeAccountServer(), account.getAcmeAccountEmail());
        if (!client.connect()) {
            cert.setAcmeCertStatus("failed");
            certificateMapper.updateById(cert);
            return ReturnResult.illegal("连接 ACME 服务器失败: " + account.getAcmeAccountServer());
        }

        // 拉取并保存挑战信息，便于外部完成验证（HTTP-01/DNS-01）
        List<Map<String, Object>> validations = new ArrayList<>();
        for (String domain : domains) {
            try {
                AcmeValidationResult vr = client.validateDomain(domain, validationType);
                Map<String, Object> item = new HashMap<>();
                item.put("domain", domain);
                item.put("type", validationType.getCode());
                item.put("token", vr.getToken());
                item.put("keyAuthorization", vr.getKeyAuthorization());
                item.put("httpPath", vr.getHttpChallengePath());
                item.put("httpContent", vr.getHttpChallengeContent());
                item.put("dnsName", vr.getDnsRecordName());
                item.put("dnsValue", vr.getDnsRecordValue());
                validations.add(item);
            } catch (Exception ex) {
                log.warn("[ACME] 获取域名验证信息失败: domain={}", domain, ex);
            }
        }
        // 写入订单记录（validating）
        MonitorAcmeOrder order = new MonitorAcmeOrder();
        order.setAcmeAccountId(account.getAcmeAccountId());
        order.setAcmeCertId(certId);
        order.setAcmeOrderStatus("validating");
        order.setAcmeAuthzUrls(Json.toJson(validations));
        orderMapper.insert(order);

        // 尝试直接申请（若外部已就绪会直接成功；否则会失败并转为等待人工验证）
        try {
            AcmeCertificate acmeCertificate = client.requestCertificate(domains, validationType);
            // 成功：落库证书与有效期
            fillCertificateSuccess(cert, acmeCertificate);
            certificateMapper.updateById(cert);

            order.setAcmeOrderStatus("valid");
            orderMapper.updateById(order);
            log.info("[ACME] 证书签发成功: certId={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());
            return ReturnResult.ok(certId, "证书已签发");
        } catch (Exception e) {
            // 失败：保持校验信息供外部配置，状态置为 validating
            log.warn("[ACME] 证书申请未完成，等待验证: certId={}, err={}", certId, e.getMessage());
            cert.setAcmeCertStatus("validating");
            cert.setAcmeCertLastError(shortMsg(e));
            certificateMapper.updateById(cert);
            return ReturnResult.ok(certId, "已生成验证信息，请按返回的HTTP/DNS要求完成验证后稍后重试");
        }
    }

    /**
     * 续签证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> renewCertificateForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }
        MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
        if (account == null) {
            return ReturnResult.illegal("ACME账户不存在: " + cert.getAcmeAccountId());
        }
        log.info("[ACME] 续签证书: id={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());

        // 准备客户端
        AcmeClient client = new AcmeClient(account.getAcmeAccountServer(), account.getAcmeAccountEmail());
        if (!client.connect()) {
            return ReturnResult.illegal("连接 ACME 服务器失败: " + account.getAcmeAccountServer());
        }

        // 构造待续签证书信息（按存量域名与验证类型）
        List<String> domains = buildDomains(cert);
        AcmeValidationType validationType = mapValidationType(cert.getAcmeCertChallengeType());
        AcmeCertificate pending = AcmeCertificate.builder()
                .primaryDomain(cert.getAcmeCertPrimaryDomain())
                .domains(domains)
                .validationType(validationType)
                .status(AcmeCertificateStatus.PENDING)
                .build();
        try {
            AcmeCertificate renewed = client.renewCertificate(pending);
            fillCertificateSuccess(cert, renewed);
            certificateMapper.updateById(cert);
            return ReturnResult.ok(true, "续签成功");
        } catch (Exception e) {
            log.error("[ACME] 续签失败: id={}", certId, e);
            cert.setAcmeCertLastError(shortMsg(e));
            certificateMapper.updateById(cert);
            return ReturnResult.illegal("续签失败: " + e.getMessage());
        }
    }

    /**
     * 吊销证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> revokeCertificateForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }
        MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
        if (account == null) {
            return ReturnResult.illegal("ACME账户不存在: " + cert.getAcmeAccountId());
        }
        log.warn("[ACME] 吊销证书: id={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());

        AcmeClient client = new AcmeClient(account.getAcmeAccountServer(), account.getAcmeAccountEmail());
        if (!client.connect()) {
            return ReturnResult.illegal("连接 ACME 服务器失败: " + account.getAcmeAccountServer());
        }
        try {
            AcmeCertificate current = AcmeCertificate.builder()
                    .primaryDomain(cert.getAcmeCertPrimaryDomain())
                    .domains(buildDomains(cert))
                    .validationType(mapValidationType(cert.getAcmeCertChallengeType()))
                    .status(AcmeCertificateStatus.VALID)
                    .build();
            client.revokeCertificate(current);
            cert.setAcmeCertStatus("revoked");
            certificateMapper.updateById(cert);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("[ACME] 吊销失败: id={}", certId, e);
            return ReturnResult.illegal("吊销失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书详情 ForCert
     *
     * @param certId 证书ID
     * @return 证书
     */
    @Override
    public ReturnResult<MonitorAcmeCertificate> getCertificateForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        return ReturnResult.ok(cert);
    }

    /**
     * 检查并续签到期前 N 天的证书（调度）
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndRenewSoonExpired() {
        int days = Math.max(acmeProperties.getRenewDaysBefore(), 7);
        String deadline = LocalDate.now().plusDays(days).atStartOfDay().format(DATE);
        List<MonitorAcmeCertificate> list = certificateMapper.selectList(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "valid")
                .le(MonitorAcmeCertificate::getAcmeCertNotAfter, deadline)
        );
        for (MonitorAcmeCertificate it : list) {
            try {
                log.info("[ACME] 到期前{}天自动续签: id={}, domain={}, notAfter={}", days, it.getAcmeCertId(), it.getAcmeCertPrimaryDomain(), it.getAcmeCertNotAfter());
                renewCertificateForCert(it.getAcmeCertId());
            } catch (Exception e) {
                log.error("[ACME] 自动续签失败, id={}", it.getAcmeCertId(), e);
            }
        }
    }

    // ======================== 内部工具方法 ========================

    /**
     * 映射挑战类型字符串到枚举
     */
    private AcmeValidationType mapValidationType(String challenge) {
        if (StringUtils.isEmpty(challenge)) {
            return AcmeValidationType.HTTP_01;
        }
        String c = challenge.trim().toUpperCase();
        if (c.startsWith("DNS")) {
            return AcmeValidationType.DNS_01;
        }
        if (c.startsWith("TLS")) {
            return AcmeValidationType.TLS_ALPN_01;
        }
        return AcmeValidationType.HTTP_01;
    }

    /**
     * 构建域名列表（主域名 + SAN）
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
     * 成功签发/续签后回填证书信息到实体
     */
    private void fillCertificateSuccess(MonitorAcmeCertificate target, AcmeCertificate src) {
        target.setAcmeCertStatus("valid");
        if (src.getValidFrom() != null) {
            target.setAcmeCertNotBefore(src.getValidFrom().format(DATE));
        }
        if (src.getValidTo() != null) {
            target.setAcmeCertNotAfter(src.getValidTo().format(DATE));
            // 计划续签日期：到期前 N 天
            LocalDateTime renewAt = src.getValidTo().minusDays(Math.max(acmeProperties.getRenewDaysBefore(), 7));
            target.setAcmeCertRenewAt(renewAt.format(DATE));
        }
        target.setAcmeCertChainPem(src.getCertificateContent());
        target.setAcmeCertKeyPem(src.getPrivateKey());
        target.setAcmeCertLastError(null);
    }

    /**
     * 提取简短异常信息
     */
    private String shortMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {return "";}
        return msg.length() > 512 ? msg.substring(0, 512) : msg;
    }
}
