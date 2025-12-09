package com.chua.starter.monitor.acme;

import com.chua.common.support.annotations.Spi;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 acme4j 的 ACME 提供者实现
 * 使用 acme4j 库实现 ACME 协议
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Spi("acme4j")
public class Acme4jProvider implements AcmeProvider {

    /**
     * ACME 会话
     */
    private Session session;

    /**
     * ACME 账户
     */
    private Account account;

    /**
     * 账户密钥对
     */
    private KeyPair accountKeyPair;

    /**
     * 域名密钥对
     */
    private KeyPair domainKeyPair;

    /**
     * 当前订单
     */
    private Order currentOrder;

    /**
     * 当前授权列表
     */
    private List<Authorization> currentAuthorizations;

    @Override
    public AcmeConnectionResult connect(String serverUrl, String email, String privateKeyPem) {
        try {
            log.info("[ACME4J] 连接到 ACME 服务器: {}, email: {}", serverUrl, email);
            
            // 创建会话
            session = new Session(serverUrl);
            
            // 加载或生成账户密钥对
            if (privateKeyPem != null && !privateKeyPem.isBlank()) {
                try (StringReader reader = new StringReader(privateKeyPem)) {
                    accountKeyPair = KeyPairUtils.readKeyPair(reader);
                    log.debug("[ACME4J] 使用已有账户密钥");
                }
            } else {
                accountKeyPair = KeyPairUtils.createKeyPair(2048);
                log.debug("[ACME4J] 生成新的账户密钥");
            }
            
            // 查找或创建账户
            AccountBuilder accountBuilder = new AccountBuilder()
                    .addEmail(email)
                    .agreeToTermsOfService()
                    .useKeyPair(accountKeyPair);
            
            account = accountBuilder.create(session);
            log.info("[ACME4J] 账户创建/登录成功: {}", account.getLocation());
            
            // 导出账户私钥
            String exportedKeyPem = exportKeyPair(accountKeyPair);
            
            return AcmeConnectionResult.success(exportedKeyPem, account.getLocation().toString());
        } catch (Exception e) {
            log.error("[ACME4J] 连接失败: {}", e.getMessage(), e);
            return AcmeConnectionResult.fail("连接 ACME 服务器失败: " + e.getMessage());
        }
    }

    @Override
    public List<AcmeValidationInfo> getValidationInfo(List<String> domains, String challengeType) {
        List<AcmeValidationInfo> validations = new ArrayList<>();
        
        try {
            if (account == null) {
                log.error("[ACME4J] 账户未初始化，请先调用 connect 方法");
                return validations;
            }
            
            log.info("[ACME4J] 获取域名验证信息: domains={}, challengeType={}", domains, challengeType);
            
            // 创建订单
            currentOrder = account.newOrder().domains(domains).create();
            currentAuthorizations = new ArrayList<>();
            
            // 遍历所有授权
            for (Authorization auth : currentOrder.getAuthorizations()) {
                currentAuthorizations.add(auth);
                String domain = auth.getIdentifier().getDomain();
                
                // 根据挑战类型获取对应的挑战
                Challenge challenge = findChallenge(auth, challengeType);
                if (challenge == null) {
                    log.warn("[ACME4J] 域名 {} 没有找到 {} 类型的挑战", domain, challengeType);
                    continue;
                }
                
                AcmeValidationInfo.AcmeValidationInfoBuilder builder = AcmeValidationInfo.builder()
                        .domain(domain)
                        .challengeType(challengeType)
                        .status(auth.getStatus().name());
                
                if (challenge instanceof Http01Challenge http01) {
                    builder.token(http01.getToken())
                            .keyAuthorization(http01.getAuthorization())
                            .httpPath("/.well-known/acme-challenge/" + http01.getToken())
                            .httpContent(http01.getAuthorization());
                } else if (challenge instanceof Dns01Challenge dns01) {
                    builder.token(dns01.getDigest())
                            .keyAuthorization(dns01.getDigest())
                            .dnsRecordName("_acme-challenge." + domain)
                            .dnsRecordValue(dns01.getDigest());
                }
                
                validations.add(builder.build());
            }
            
            log.info("[ACME4J] 获取到 {} 个域名的验证信息", validations.size());
        } catch (Exception e) {
            log.error("[ACME4J] 获取验证信息失败: {}", e.getMessage(), e);
        }
        
        return validations;
    }

    @Override
    public AcmeCertificateResult requestCertificate(List<String> domains, String challengeType) {
        try {
            if (account == null) {
                return AcmeCertificateResult.fail("账户未初始化，请先调用 connect 方法");
            }
            
            log.info("[ACME4J] 申请证书: domains={}, challengeType={}", domains, challengeType);
            
            // 如果没有当前订单，创建新订单
            if (currentOrder == null) {
                currentOrder = account.newOrder().domains(domains).create();
                currentAuthorizations = new ArrayList<>();
                for (Authorization auth : currentOrder.getAuthorizations()) {
                    currentAuthorizations.add(auth);
                }
            }
            
            // 触发所有挑战验证
            for (Authorization auth : currentAuthorizations) {
                if (auth.getStatus() == Status.VALID) {
                    continue;
                }
                
                Challenge challenge = findChallenge(auth, challengeType);
                if (challenge == null) {
                    return AcmeCertificateResult.fail("域名 " + auth.getIdentifier().getDomain() + " 没有找到对应的挑战");
                }
                
                // 触发挑战
                challenge.trigger();
                
                // 等待挑战完成
                int attempts = 0;
                while (challenge.getStatus() != Status.VALID && attempts < 30) {
                    if (challenge.getStatus() == Status.INVALID) {
                        return AcmeCertificateResult.fail("域名验证失败: " + auth.getIdentifier().getDomain());
                    }
                    Thread.sleep(3000);
                    challenge.update();
                    attempts++;
                }
                
                if (challenge.getStatus() != Status.VALID) {
                    return AcmeCertificateResult.fail("域名验证超时: " + auth.getIdentifier().getDomain());
                }
            }
            
            // 生成域名密钥对
            domainKeyPair = KeyPairUtils.createKeyPair(2048);
            
            // 生成 CSR
            CSRBuilder csrBuilder = new CSRBuilder();
            csrBuilder.addDomains(domains);
            csrBuilder.sign(domainKeyPair);
            byte[] csr = csrBuilder.getEncoded();
            
            // 完成订单
            currentOrder.execute(csr);
            
            // 等待订单完成
            int attempts = 0;
            while (currentOrder.getStatus() != Status.VALID && attempts < 30) {
                if (currentOrder.getStatus() == Status.INVALID) {
                    return AcmeCertificateResult.fail("订单失败");
                }
                Thread.sleep(3000);
                currentOrder.update();
                attempts++;
            }
            
            if (currentOrder.getStatus() != Status.VALID) {
                return AcmeCertificateResult.fail("订单超时");
            }
            
            // 获取证书
            Certificate certificate = currentOrder.getCertificate();
            if (certificate == null) {
                return AcmeCertificateResult.fail("获取证书失败");
            }
            
            // 导出证书和私钥
            String certPem = exportCertificateChain(certificate);
            String keyPem = exportKeyPair(domainKeyPair);
            
            // 获取有效期
            X509Certificate x509 = certificate.getCertificate();
            LocalDateTime notBefore = LocalDateTime.ofInstant(
                    x509.getNotBefore().toInstant(), ZoneId.systemDefault());
            LocalDateTime notAfter = LocalDateTime.ofInstant(
                    x509.getNotAfter().toInstant(), ZoneId.systemDefault());
            
            log.info("[ACME4J] 证书申请成功: domains={}, notAfter={}", domains, notAfter);
            
            // 清理当前订单
            currentOrder = null;
            currentAuthorizations = null;
            
            return AcmeCertificateResult.success(certPem, keyPem, notBefore, notAfter);
        } catch (Exception e) {
            log.error("[ACME4J] 证书申请失败: {}", e.getMessage(), e);
            return AcmeCertificateResult.fail("证书申请失败: " + e.getMessage());
        }
    }

    @Override
    public AcmeCertificateResult renewCertificate(List<String> domains, String challengeType) {
        // 续签与申请流程相同
        log.info("[ACME4J] 续签证书: domains={}", domains);
        return requestCertificate(domains, challengeType);
    }

    @Override
    public boolean revokeCertificate(String certificatePem) {
        try {
            if (session == null) {
                log.error("[ACME4J] 会话未初始化");
                return false;
            }
            
            log.warn("[ACME4J] 吊销证书");
            
            // 解析证书
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(certificatePem.getBytes()));
            
            // 吊销证书
            Certificate.revoke(session, accountKeyPair, cert, null);
            
            log.info("[ACME4J] 证书吊销成功");
            return true;
        } catch (Exception e) {
            log.error("[ACME4J] 证书吊销失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getAccountPrivateKeyPem() {
        if (accountKeyPair == null) {
            return null;
        }
        return exportKeyPair(accountKeyPair);
    }

    @Override
    public void close() {
        session = null;
        account = null;
        accountKeyPair = null;
        domainKeyPair = null;
        currentOrder = null;
        currentAuthorizations = null;
        log.debug("[ACME4J] 连接已关闭");
    }

    /**
     * 根据挑战类型查找对应的挑战
     *
     * @param auth          授权
     * @param challengeType 挑战类型
     * @return 挑战对象
     */
    private Challenge findChallenge(Authorization auth, String challengeType) {
        if ("DNS-01".equalsIgnoreCase(challengeType) || "DNS".equalsIgnoreCase(challengeType)) {
            return auth.findChallenge(Dns01Challenge.class).orElse(null);
        }
        // 默认使用 HTTP-01
        return auth.findChallenge(Http01Challenge.class).orElse(null);
    }

    /**
     * 导出密钥对为 PEM 格式
     *
     * @param keyPair 密钥对
     * @return PEM 字符串
     */
    private String exportKeyPair(KeyPair keyPair) {
        try (StringWriter writer = new StringWriter()) {
            KeyPairUtils.writeKeyPair(keyPair, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("[ACME4J] 导出密钥失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 导出证书链为 PEM 格式
     *
     * @param certificate 证书
     * @return PEM 字符串
     */
    private String exportCertificateChain(Certificate certificate) {
        try (StringWriter writer = new StringWriter()) {
            certificate.writeCertificate(writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("[ACME4J] 导出证书失败: {}", e.getMessage());
            return null;
        }
    }
}
