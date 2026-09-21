package com.chua.starter.monitor.service.acme;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.network.protocol.ClientSetting;
import com.chua.common.support.network.ssl.AcmeCertificateResult;
import com.chua.common.support.network.ssl.AcmeConnectionResult;
import com.chua.common.support.network.ssl.AcmeProvider;
import com.chua.common.support.network.ssl.AcmeValidationInfo;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.ssh.support.client.SshClient;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.pojo.acme.AcmeApplyCertRequest;
import com.chua.starter.monitor.properties.AcmeProperties;
import com.chua.starter.monitor.service.acme.support.AcmeCertificateSupport;
import com.chua.starter.service.utils.ProtocolClientSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ACME 证书异步申请服务
 * 通过 @异步 在后台线程执行证书申请，并通过 WebSocket 推送结果到消息中心
 *
 * @author CH
 * @since 2025-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(name = "com.chua.socket.support.session.SocketSessionTemplate")
public class AcmeCertificateAsyncService {

    /**
     * 账户映射器
     */
    private final MonitorAcmeAccountMapper accountMapper;

    /**
     * 证书映射器
     */
    private final MonitorAcmeCertificateMapper certificateMapper;

    /**
     * 订单映射器
     */
    private final MonitorAcmeOrderMapper orderMapper;

    /**
     * ACME 配置属性
     */
    private final AcmeProperties acmeProperties;

    /**
     * 消息推送会话模板
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 本机 HTTP-01 验证缓存服务
     */
    private final AcmeChallengeService challengeService;

    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步申请证书
     * 立即返回，实际申请在虚拟线程中执行，完成后通过消息推送通知用户
     *
     * @param certId  预创建的证书标识
     * @param request 申请请求
     * @param userId  用户标识（用于消息推送）
     */
    public void applyCertificateAsync(Integer certId, AcmeApplyCertRequest request, Integer userId) {
        ThreadUtils.ofVirtual()
                .name("acme-async-" + certId)
                .start(() -> {
                    log.info("[ACME-ASYNC] 开始异步申请证书: certId={}, domain={}, userId={}",
                            certId, request.primaryDomain(), userId);

                    MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
                    if (cert == null) {
                        pushMessage(userId, "证书申请失败", "证书记录不存在", "error", certId, request.primaryDomain());
                        return;
                    }

                    // 查询账户信息
                    MonitorAcmeAccount account = accountMapper.selectById(request.acmeAccountId());
                    if (account == null) {
                        updateCertStatus(cert, AcmeCertificateSupport.STATUS_FAILED, "ACME账户不存在");
                        pushMessage(userId, "证书申请失败", "ACME账户不存在", "error", certId, request.primaryDomain());
                        return;
                    }

                    // 推送开始消息
                    pushMessage(userId, "证书申请中", "正在连接 ACME 服务器...", "info", certId, request.primaryDomain());

                    // 组装域名列表（主域名 + SAN），与持久化的证书记录保持同一来源
                    List<String> domains = AcmeCertificateSupport.buildDomains(cert);
                    String challenge = StringUtils.defaultString(request.challengeType(), AcmeCertificateSupport.CHALLENGE_HTTP_01);

                    // 通过 SPI 获取 ACME 提供者（按账户服务商类型路由）
                    AcmeProvider provider = ServiceProvider.of(AcmeProvider.class)
                            .getNewExtension(AcmeCertificateSupport.providerName(account.getAcmeAccountProviderType()));
                    if (provider == null) {
                        updateCertStatus(cert, AcmeCertificateSupport.STATUS_FAILED, "ACME 服务提供者不可用");
                        pushMessage(userId, "证书申请失败", "ACME 服务提供者不可用，请检查系统配置", "error", certId, request.primaryDomain());
                        return;
                    }

                    try {
                        // 连接 ACME 服务器
                        AcmeConnectionResult connResult = provider.connect(
                                account.getAcmeAccountServer(),
                                account.getAcmeAccountEmail(),
                                account.getAcmeAccountPrivateKeyPem(),
                                account.getAcmeAccountEabKid(),
                                account.getAcmeAccountEabHmacKey()
                        );

                        if (!connResult.isSuccess()) {
                            updateCertStatus(cert, AcmeCertificateSupport.STATUS_FAILED, connResult.getError());
                            pushMessage(userId, "证书申请失败", "连接 ACME 服务器失败: " + connResult.getError(), "error", certId, request.primaryDomain());
                            return;
                        }

                        // 已保存私钥为空或为历史占位值时，持久化本次生成的真实私钥，避免重复注册账户
                        if (AcmeCertificateSupport.needPersistAccountKey(account.getAcmeAccountPrivateKeyPem())
                                && connResult.getPrivateKeyPem() != null) {
                            account.setAcmeAccountPrivateKeyPem(connResult.getPrivateKeyPem());
                            account.setAcmeAccountStatus("valid");
                            accountMapper.updateById(account);
                        }

                        // 推送验证中消息
                        pushMessage(userId, "证书申请中", "正在进行域名验证...", "info", certId, request.primaryDomain());

                        // 获取验证信息（同一订单地址随验证信息返回，供跨请求恢复）
                        List<AcmeValidationInfo> validationInfos = provider.getValidationInfo(domains, challenge);

                        // 写入订单记录（订单地址 + 验证信息）
                        MonitorAcmeOrder order = new MonitorAcmeOrder();
                        order.setAcmeAccountId(account.getAcmeAccountId());
                        order.setAcmeCertId(certId);
                        order.setAcmeOrderStatus(AcmeCertificateSupport.STATUS_VALIDATING);
                        order.setAcmeOrderUrl(AcmeCertificateSupport.resolveOrderUrl(validationInfos));
                        order.setAcmeAuthzUrls(AcmeCertificateSupport.toValidationJson(validationInfos));
                        orderMapper.insert(order);

                        // 根据部署模式处理
                        String deployMode = StringUtils.defaultString(cert.getAcmeCertDeployMode(), AcmeCertificateSupport.DEPLOY_MODE_MANUAL);

                        if (AcmeCertificateSupport.DEPLOY_MODE_LOCAL.equals(deployMode)
                                && AcmeCertificateSupport.CHALLENGE_HTTP_01.equals(challenge)) {
                            // 本机模式：通过内置 HTTP 端点响应验证请求
                            for (AcmeValidationInfo info : validationInfos) {
                                if (AcmeCertificateSupport.CHALLENGE_HTTP_01.equals(info.getChallengeType())
                                        && StringUtils.isNotEmpty(info.getToken())) {
                                    challengeService.put(info.getToken(), info.getHttpContent());
                                }
                            }
                            // 配置了 webroot 时同步写静态文件（尽力而为，失败不影响内置端点响应）
                            AcmeCertificateSupport.writeHttpChallengeFiles(request.webrootPath(), validationInfos);
                            log.info("[ACME-ASYNC] 本机模式，验证信息已存储到缓存");
                        } else if (AcmeCertificateSupport.DEPLOY_MODE_SSH.equals(deployMode)
                                && AcmeCertificateSupport.CHALLENGE_HTTP_01.equals(challenge)) {
                            // SSH远程模式：通过SSH创建验证文件
                            boolean deployed = deploySshChallengeFiles(request, validationInfos);
                            if (!deployed) {
                                cert.setAcmeCertStatus(AcmeCertificateSupport.STATUS_VALIDATING);
                                cert.setAcmeCertLastError("SSH部署验证文件失败，请检查SSH连接信息");
                                certificateMapper.updateById(cert);
                                pushMessage(userId, "证书申请待验证",
                                        String.format("域名 %s SSH部署验证文件失败，请检查SSH连接信息后重新验证", request.primaryDomain()),
                                        "warning", certId, request.primaryDomain());
                                return;
                            }
                            log.info("[ACME-ASYNC] SSH模式，验证文件已部署到远程服务器: {}", request.sshHost());
                        } else if (AcmeCertificateSupport.DEPLOY_MODE_MANUAL.equals(deployMode)) {
                            cert.setAcmeCertStatus(AcmeCertificateSupport.STATUS_VALIDATING);
                            cert.setAcmeCertLastError(null);
                            certificateMapper.updateById(cert);
                            log.info("[ACME-ASYNC] 手动模式，等待用户部署验证文件: certId={}", certId);
                            pushMessage(userId, "证书申请待验证",
                                    String.format("域名 %s 需要完成 %s 验证，请查看证书详情获取验证信息并手动部署",
                                            request.primaryDomain(), challenge),
                                    "warning", certId, request.primaryDomain());
                            return;
                        }

                        // 尝试申请证书
                        AcmeCertificateResult certResult = provider.requestCertificate(domains, challenge);

                        if (certResult.isSuccess()) {
                            // 成功签发
                            AcmeCertificateSupport.fillCertificateSuccess(cert, certResult, acmeProperties.getRenewDaysBefore());
                            certificateMapper.updateById(cert);
                            order.setAcmeOrderStatus(AcmeCertificateSupport.ORDER_STATUS_VALID);
                            orderMapper.updateById(order);

                            log.info("[ACME-ASYNC] 证书签发成功: certId={}, domain={}", certId, request.primaryDomain());
                            pushMessage(userId, "证书签发成功",
                                    String.format("域名 %s 的 SSL 证书已成功签发", request.primaryDomain()),
                                    "success", certId, request.primaryDomain());
                        } else {
                            // 需要手动验证
                            cert.setAcmeCertStatus(AcmeCertificateSupport.STATUS_VALIDATING);
                            cert.setAcmeCertLastError(AcmeCertificateSupport.shortMsg(certResult.getError()));
                            certificateMapper.updateById(cert);

                            log.warn("[ACME-ASYNC] 证书申请需要验证: certId={}, err={}", certId, certResult.getError());
                            pushMessage(userId, "证书申请待验证",
                                    String.format("域名 %s 需要完成 %s 验证，请查看证书详情获取验证信息",
                                            request.primaryDomain(), challenge),
                                    "warning", certId, request.primaryDomain());
                        }

                    } catch (Exception e) {
                        log.error("[ACME-ASYNC] 证书申请异常: certId={}", certId, e);
                        updateCertStatus(cert, AcmeCertificateSupport.STATUS_VALIDATING, AcmeCertificateSupport.shortMsg(e));
                        pushMessage(userId, "证书申请待验证",
                                String.format("域名 %s 需要手动验证: %s", request.primaryDomain(), AcmeCertificateSupport.shortMsg(e)),
                                "warning", certId, request.primaryDomain());
                    } finally {
                        provider.close();
                    }
                });

    }

    /**
     * 推送消息到用户的消息中心
     * @param userId 用户id
     * @param title title
     * @param content 内容
     * @param level 级别
     * @param certId certId
     * @param domain domain
     */
    private void pushMessage(Integer userId, String title, String content, String level, Integer certId, String domain) {
        try {
            Map<String, Object> data = new HashMap<>(12);
            data.put("messageId", System.currentTimeMillis());
            data.put("title", title);
            data.put("content", content);
            data.put("type", "acme_cert_apply");
            data.put("level", level);
            data.put("bizType", "acme_certificate");
            data.put("bizId", String.valueOf(certId));
            data.put("url", "/acme");
            data.put("sendTime", LocalDateTime.now().format(DATE));
            data.put("dataId", userId != null ? String.valueOf(userId) : "0");
            data.put("certId", certId);
            data.put("domain", domain);

            String topic = SocketTopics.Service.Message.PUSH + "@" + (userId != null ? userId : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.debug("[ACME-ASYNC] 消息推送成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("[ACME-ASYNC] 消息推送失败: userId={}, title={}", userId, title, e);
        }
    }

    /**
     * 更新证书状态
     * @param cert cert
     * @param status 状态
     * @param error 错误
     */
    private void updateCertStatus(MonitorAcmeCertificate cert, String status, String error) {
        cert.setAcmeCertStatus(status);
        cert.setAcmeCertLastError(error);
        certificateMapper.updateById(cert);
    }

    /**
     * SSH远程模式下通过SSH部署 HTTP-01 验证文件
     *
     * @param request         申请请求（含 SSH 连接信息）
     * @param validationInfos 验证信息列表
     * @return 是否部署成功
     */
    private boolean deploySshChallengeFiles(AcmeApplyCertRequest request, List<AcmeValidationInfo> validationInfos) {
        if (StringUtils.isEmpty(request.sshHost()) || StringUtils.isEmpty(request.sshUsername())) {
            log.error("[ACME-ASYNC] SSH连接信息不完整");
            return false;
        }
        if (StringUtils.isEmpty(request.webrootPath())) {
            log.error("[ACME-ASYNC] webroot路径未配置");
            return false;
        }

        String clientId = "acme-ssh-" + System.currentTimeMillis();
        SshClient sshClient = null;
        try {
            // 创建 SSH 连接
            ClientSetting setting = ClientSetting.builder()
                    .host(request.sshHost())
                    .port(request.sshPort() != null ? request.sshPort() : 22)
                    .username(request.sshUsername())
                    .password(request.sshPassword())
                    .connectTimeout(30000)
                    .build();
            sshClient = ProtocolClientSupport.create(clientId, SshClient.class, setting);
            log.info("[ACME-ASYNC] SSH连接成功: {}@{}", request.sshUsername(), request.sshHost());

            String webroot = request.webrootPath();
            // 创建验证目录
            String mkdirCmd = String.format("mkdir -p %s/.well-known/acme-challenge", webroot);
            sshClient.exec().command(mkdirCmd).execute();
            log.info("[ACME-ASYNC] 创建验证目录: {}", mkdirCmd);

            // 写入验证文件
            for (AcmeValidationInfo info : validationInfos) {
                if (AcmeCertificateSupport.CHALLENGE_HTTP_01.equals(info.getChallengeType())
                        && StringUtils.isNotEmpty(info.getToken())) {
                    String writeCmd = String.format("echo '%s' > %s/.well-known/acme-challenge/%s",
                            info.getHttpContent(), webroot, info.getToken());
                    sshClient.exec().command(writeCmd).execute();
                    log.info("[ACME-ASYNC] SSH写入验证文件: {}", info.getToken());
                }
            }
            return true;
        } catch (Exception e) {
            log.error("[ACME-ASYNC] SSH部署验证文件失败: {}", e.getMessage(), e);
            return false;
        } finally {
            if (sshClient != null) {
                try {
                    ProtocolClientSupport.destroy(clientId, SshClient.class);
                } catch (Exception e) {
                    log.warn("[ACME-ASYNC] 关闭SSH连接失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 重新验证证书（用户手动部署完成后调用）
     *
     * @param certId 证书标识
     * @param userId 用户标识
     */
    public void retryValidationAsync(Integer certId, Integer userId) {
        ThreadUtils.ofVirtual()
                .name("acme-retry-" + certId)
                .start(() -> {
                    log.info("[ACME-ASYNC] 开始重新验证证书: certId={}, userId={}", certId, userId);

                    MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
                    if (cert == null) {
                        pushMessage(userId, "重新验证失败", "证书记录不存在", "error", certId, null);
                        return;
                    }

                    MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
                    if (account == null) {
                        updateCertStatus(cert, AcmeCertificateSupport.STATUS_FAILED, "ACME账户不存在");
                        pushMessage(userId, "重新验证失败", "ACME账户不存在", "error", certId, cert.getAcmeCertPrimaryDomain());
                        return;
                    }

                    pushMessage(userId, "正在重新验证", "正在连接 ACME 服务器...", "info", certId, cert.getAcmeCertPrimaryDomain());

                    // 查询最近一笔订单，存在订单地址时跨请求恢复该订单，避免重新下单导致已部署验证失效
                    MonitorAcmeOrder latestOrder = orderMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MonitorAcmeOrder>()
                                    .eq(MonitorAcmeOrder::getAcmeCertId, certId)
                                    .orderByDesc(MonitorAcmeOrder::getUpdateTime)
                                    .last("LIMIT 1"));

                    // 组装域名列表（主域名 + SAN）
                    List<String> domains = AcmeCertificateSupport.buildDomains(cert);
                    String challenge = cert.getAcmeCertChallengeType();

                    // 通过 SPI 获取 ACME 提供者（按账户服务商类型路由）
                    AcmeProvider provider = ServiceProvider.of(AcmeProvider.class)
                            .getNewExtension(AcmeCertificateSupport.providerName(account.getAcmeAccountProviderType()));
                    if (provider == null) {
                        updateCertStatus(cert, AcmeCertificateSupport.STATUS_FAILED, "ACME 服务提供者不可用");
                        pushMessage(userId, "重新验证失败", "ACME 服务提供者不可用", "error", certId, cert.getAcmeCertPrimaryDomain());
                        return;
                    }

                    try {
                        AcmeConnectionResult connResult = provider.connect(
                                account.getAcmeAccountServer(),
                                account.getAcmeAccountEmail(),
                                account.getAcmeAccountPrivateKeyPem(),
                                account.getAcmeAccountEabKid(),
                                account.getAcmeAccountEabHmacKey()
                        );

                        if (!connResult.isSuccess()) {
                            updateCertStatus(cert, AcmeCertificateSupport.STATUS_FAILED, connResult.getError());
                            pushMessage(userId, "重新验证失败", "连接 ACME 服务器失败: " + connResult.getError(), "error", certId, cert.getAcmeCertPrimaryDomain());
                            return;
                        }

                        pushMessage(userId, "正在重新验证", "正在进行域名验证...", "info", certId, cert.getAcmeCertPrimaryDomain());

                        // 优先恢复历史订单；历史数据无订单地址时回退为重新下单
                        AcmeCertificateResult certResult;
                        if (latestOrder != null && StringUtils.isNotEmpty(latestOrder.getAcmeOrderUrl())) {
                            certResult = provider.resumeCertificate(latestOrder.getAcmeOrderUrl(), domains, challenge);
                        } else {
                            log.warn("[ACME-ASYNC] 未找到历史订单地址，回退为重新下单: certId={}", certId);
                            certResult = provider.requestCertificate(domains, challenge);
                        }

                        if (certResult.isSuccess()) {
                            AcmeCertificateSupport.fillCertificateSuccess(cert, certResult, acmeProperties.getRenewDaysBefore());
                            certificateMapper.updateById(cert);

                            // 更新订单状态
                            if (latestOrder != null) {
                                latestOrder.setAcmeOrderStatus(AcmeCertificateSupport.ORDER_STATUS_VALID);
                                orderMapper.updateById(latestOrder);
                            }

                            log.info("[ACME-ASYNC] 重新验证成功: certId={}", certId);
                            pushMessage(userId, "证书签发成功",
                                    String.format("域名 %s 的 SSL 证书已成功签发", cert.getAcmeCertPrimaryDomain()),
                                    "success", certId, cert.getAcmeCertPrimaryDomain());
                        } else {
                            cert.setAcmeCertStatus(AcmeCertificateSupport.STATUS_VALIDATING);
                            cert.setAcmeCertLastError(AcmeCertificateSupport.shortMsg(certResult.getError()));
                            certificateMapper.updateById(cert);

                            log.warn("[ACME-ASYNC] 重新验证失败: certId={}, err={}", certId, certResult.getError());
                            pushMessage(userId, "重新验证失败",
                                    String.format("域名验证失败: %s，请检查验证文件是否正确部署", certResult.getError()),
                                    "error", certId, cert.getAcmeCertPrimaryDomain());
                        }
                    } catch (Exception e) {
                        log.error("[ACME-ASYNC] 重新验证异常: certId={}", certId, e);
                        updateCertStatus(cert, AcmeCertificateSupport.STATUS_VALIDATING, AcmeCertificateSupport.shortMsg(e));
                        pushMessage(userId, "重新验证失败",
                                String.format("验证异常: %s", AcmeCertificateSupport.shortMsg(e)),
                                "error", certId, cert.getAcmeCertPrimaryDomain());
                    } finally {
                        provider.close();
                    }
                });
    }
}
