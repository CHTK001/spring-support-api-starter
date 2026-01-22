package com.chua.starter.monitor.service.acme;

import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.acme.support.AcmeCertificateResult;
import com.chua.acme.support.AcmeConnectionResult;
import com.chua.acme.support.AcmeProvider;
import com.chua.acme.support.AcmeValidationInfo;
import com.chua.common.support.core.utils.ThreadUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.pojo.acme.AcmeApplyCertRequest;
import com.chua.starter.monitor.properties.AcmeProperties;
import com.chua.common.support.network.protocol.ClientSetting;
import com.chua.common.support.network.protocol.client.ProtocolClientFactory;
import com.chua.ssh.support.client.SshClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ACME 证书异步申请服务
 * 通过 @Async 在后台线程执行证书申请，并通过 WebSocket 推送结果到消息中心
 *
 * @author CH
 * @since 2025-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(name = "com.chua.socket.support.session.SocketSessionTemplate")
public class AcmeCertificateAsyncService {

    private final MonitorAcmeAccountMapper accountMapper;
    private final MonitorAcmeCertificateMapper certificateMapper;
    private final MonitorAcmeOrderMapper orderMapper;
    private final AcmeProperties acmeProperties;
    private final SocketSessionTemplate socketSessionTemplate;
    private final AcmeChallengeService challengeService;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步申请证书
     * 立即返回，实际申请在后台线程执行，完成后通过消息推送通知用户
     *
     * @param certId  预创建的证书ID
     * @param request 申请请求
     * @param userId  用户ID（用于消息推送）
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyCertificateAsync(Integer certId, AcmeApplyCertRequest request, Integer userId) {
        ThreadUtils.ofVirtual()
                .name("acme-async-" + certId)
                .start(() -> {
                    log.info("[ACME-ASYNC] 开始异步申请证书: certId={}, domain={}, userId={}",
                            certId, request.getPrimaryDomain(), userId);

                    MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
                    if (cert == null) {
                        pushMessage(userId, "证书申请失败", "证书记录不存在", "error", certId, request.getPrimaryDomain());
                        return;
                    }

                    // 查询账户信息
                    MonitorAcmeAccount account = accountMapper.selectById(request.getAcmeAccountId());
                    if (account == null) {
                        updateCertStatus(cert, "failed", "ACME账户不存在");
                        pushMessage(userId, "证书申请失败", "ACME账户不存在", "error", certId, request.getPrimaryDomain());
                        return;
                    }

                    // 推送开始消息
                    pushMessage(userId, "证书申请中", "正在连接 ACME 服务器...", "info", certId, request.getPrimaryDomain());

                    // 组装域名列表
                    List<String> sans = request.getSanDomains() == null ? List.of() : request.getSanDomains();
                    List<String> domains = new ArrayList<>();
                    domains.add(request.getPrimaryDomain());
                    domains.addAll(sans.stream().filter(StringUtils::isNotEmpty).toList());
                    String challenge = StringUtils.defaultString(request.getChallengeType(), "HTTP-01");

                    // 通过 SPI 获取 ACME 提供者
                    AcmeProvider provider = ServiceProvider.of(AcmeProvider.class).getNewExtension("acme4j");
                    if (provider == null) {
                        updateCertStatus(cert, "failed", "ACME 服务提供者不可用");
                        pushMessage(userId, "证书申请失败", "ACME 服务提供者不可用，请检查系统配置", "error", certId, request.getPrimaryDomain());
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
                            updateCertStatus(cert, "failed", connResult.getErrorMessage());
                            pushMessage(userId, "证书申请失败", "连接 ACME 服务器失败: " + connResult.getErrorMessage(), "error", certId, request.getPrimaryDomain());
                            return;
                        }

                        // 保存账户私钥（如果是新生成的）
                        if (account.getAcmeAccountPrivateKeyPem() == null && connResult.getAccountPrivateKeyPem() != null) {
                            account.setAcmeAccountPrivateKeyPem(connResult.getAccountPrivateKeyPem());
                            account.setAcmeAccountStatus("valid");
                            accountMapper.updateById(account);
                        }

                        // 推送验证中消息
                        pushMessage(userId, "证书申请中", "正在进行域名验证...", "info", certId, request.getPrimaryDomain());

                        // 获取验证信息
                        List<AcmeValidationInfo> validationInfos = provider.getValidationInfo(domains, challenge);
                        List<Map<String, Object>> validations = new ArrayList<>();
                        for (AcmeValidationInfo info : validationInfos) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("domain", info.getDomain());
                            item.put("type", info.getChallengeType());
                            item.put("token", info.getToken());
                            item.put("keyAuthorization", info.getKeyAuthorization());
                            item.put("httpPath", info.getHttpPath());
                            item.put("httpContent", info.getHttpContent());
                            item.put("dnsName", info.getDnsRecordName());
                            item.put("dnsValue", info.getDnsRecordValue());
                            validations.add(item);
                        }

                        // 写入订单记录
                        MonitorAcmeOrder order = new MonitorAcmeOrder();
                        order.setAcmeAccountId(account.getAcmeAccountId());
                        order.setAcmeCertId(certId);
                        order.setAcmeOrderStatus("validating");
                        order.setAcmeAuthzUrls(Json.toJson(validations));
                        orderMapper.insert(order);

                        // 根据部署模式处理
                        String deployMode = StringUtils.defaultString(cert.getAcmeCertDeployMode(), "manual");
                        
                        if ("local".equals(deployMode) && "HTTP-01".equals(challenge)) {
                            // 本机模式：通过内置 HTTP 端点响应验证请求
                            for (AcmeValidationInfo info : validationInfos) {
                                if ("HTTP-01".equals(info.getChallengeType()) && StringUtils.isNotEmpty(info.getToken())) {
                                    challengeService.put(info.getToken(), info.getKeyAuthorization());
                                }
                            }
                            log.info("[ACME-ASYNC] 本机模式，验证信息已存储到缓存");
                        } else if ("ssh".equals(deployMode) && "HTTP-01".equals(challenge)) {
                            // SSH远程模式：通过SSH创建验证文件
                            boolean deployed = deploySshChallengeFiles(request, validationInfos);
                            if (!deployed) {
                                cert.setAcmeCertStatus("validating");
                                cert.setAcmeCertLastError("SSH部署验证文件失败，请检查SSH连接信息");
                                certificateMapper.updateById(cert);
                                pushMessage(userId, "证书申请待验证",
                                        String.format("域名 %s SSH部署验证文件失败，请检查SSH连接信息后重新验证", request.getPrimaryDomain()),
                                        "warning", certId, request.getPrimaryDomain());
                                return;
                            }
                            log.info("[ACME-ASYNC] SSH模式，验证文件已部署到远程服务器: {}", request.getSshHost());
                        } else if ("manual".equals(deployMode)) {
                            cert.setAcmeCertStatus("validating");
                            cert.setAcmeCertLastError(null);
                            certificateMapper.updateById(cert);
                            log.info("[ACME-ASYNC] 手动模式，等待用户部署验证文件: certId={}", certId);
                            pushMessage(userId, "证书申请待验证",
                                    String.format("域名 %s 需要完成 %s 验证，请查看证书详情获取验证信息并手动部署",
                                            request.getPrimaryDomain(), challenge),
                                    "warning", certId, request.getPrimaryDomain());
                            return;
                        }

                        // 尝试申请证书
                        AcmeCertificateResult certResult = provider.requestCertificate(domains, challenge);

                        if (certResult.isSuccess()) {
                            // 成功签发
                            fillCertificateSuccess(cert, certResult);
                            certificateMapper.updateById(cert);
                            order.setAcmeOrderStatus("valid");
                            orderMapper.updateById(order);

                            log.info("[ACME-ASYNC] 证书签发成功: certId={}, domain={}", certId, request.getPrimaryDomain());
                            pushMessage(userId, "证书签发成功",
                                    String.format("域名 %s 的 SSL 证书已成功签发", request.getPrimaryDomain()),
                                    "success", certId, request.getPrimaryDomain());
                        } else {
                            // 需要手动验证
                            cert.setAcmeCertStatus("validating");
                            cert.setAcmeCertLastError(certResult.getErrorMessage());
                            certificateMapper.updateById(cert);

                            log.warn("[ACME-ASYNC] 证书申请需要验证: certId={}, err={}", certId, certResult.getErrorMessage());
                            pushMessage(userId, "证书申请待验证",
                                    String.format("域名 %s 需要完成 %s 验证，请查看证书详情获取验证信息",
                                            request.getPrimaryDomain(), challenge),
                                    "warning", certId, request.getPrimaryDomain());
                        }

                    } catch (Exception e) {
                        log.error("[ACME-ASYNC] 证书申请异常: certId={}", certId, e);
                        updateCertStatus(cert, "validating", shortMsg(e));
                        pushMessage(userId, "证书申请待验证",
                                String.format("域名 %s 需要手动验证: %s", request.getPrimaryDomain(), shortMsg(e)),
                                "warning", certId, request.getPrimaryDomain());
                    } finally {
                        provider.close();
                    }
                });

    }

    /**
     * 推送消息到用户的消息中心
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
     */
    private void updateCertStatus(MonitorAcmeCertificate cert, String status, String error) {
        cert.setAcmeCertStatus(status);
        cert.setAcmeCertLastError(error);
        certificateMapper.updateById(cert);
    }

    /**
     * 成功签发后回填证书信息
     */
    private void fillCertificateSuccess(MonitorAcmeCertificate target, AcmeCertificateResult src) {
        target.setAcmeCertStatus("valid");
        if (src.getNotBefore() != null) {
            target.setAcmeCertNotBefore(src.getNotBefore().format(DATE));
        }
        if (src.getNotAfter() != null) {
            target.setAcmeCertNotAfter(src.getNotAfter().format(DATE));
            LocalDateTime renewAt = src.getNotAfter().minusDays(Math.max(acmeProperties.getRenewDaysBefore(), 7));
            target.setAcmeCertRenewAt(renewAt.format(DATE));
        }
        target.setAcmeCertChainPem(src.getCertificatePem());
        target.setAcmeCertKeyPem(src.getPrivateKeyPem());
        target.setAcmeCertLastError(null);
    }

    /**
     * 提取简短异常信息
     */
    private String shortMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "";
        }
        return msg.length() > 512 ? msg.substring(0, 512) : msg;
    }

    /**
     * 本机模式下自动部署 HTTP-01 验证文件
     *
     * @param webrootPath     webroot 路径
     * @param validationInfos 验证信息列表
     * @return 是否部署成功
     */
    private boolean deployHttpChallengeFiles(String webrootPath, List<AcmeValidationInfo> validationInfos) {
        if (StringUtils.isEmpty(webrootPath)) {
            log.error("[ACME-ASYNC] webroot路径未配置");
            return false;
        }
        try {
            Path challengeDir = Paths.get(webrootPath, ".well-known", "acme-challenge");
            Files.createDirectories(challengeDir);
            for (AcmeValidationInfo info : validationInfos) {
                if ("HTTP-01".equals(info.getChallengeType()) && StringUtils.isNotEmpty(info.getToken())) {
                    Path tokenFile = challengeDir.resolve(info.getToken());
                    Files.writeString(tokenFile, info.getKeyAuthorization(), StandardCharsets.UTF_8);
                    log.info("[ACME-ASYNC] 已写入验证文件: {}", tokenFile);
                }
            }
            return true;
        } catch (IOException e) {
            log.error("[ACME-ASYNC] 部署验证文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * SSH远程模式下通过SSH部署 HTTP-01 验证文件
     *
     * @param request         申请请求（含 SSH 连接信息）
     * @param validationInfos 验证信息列表
     * @return 是否部署成功
     */
    private boolean deploySshChallengeFiles(AcmeApplyCertRequest request, List<AcmeValidationInfo> validationInfos) {
        if (StringUtils.isEmpty(request.getSshHost()) || StringUtils.isEmpty(request.getSshUsername())) {
            log.error("[ACME-ASYNC] SSH连接信息不完整");
            return false;
        }
        if (StringUtils.isEmpty(request.getWebrootPath())) {
            log.error("[ACME-ASYNC] webroot路径未配置");
            return false;
        }

        String clientId = "acme-ssh-" + System.currentTimeMillis();
        SshClient sshClient = null;
        try {
            // 创建 SSH 连接
            ClientSetting setting = ClientSetting.builder()
                    .host(request.getSshHost())
                    .port(request.getSshPort() != null ? request.getSshPort() : 22)
                    .username(request.getSshUsername())
                    .password(request.getSshPassword())
                    .clientType("ssh")
                    .connectTimeoutMillis(30000)
                    .build();
            sshClient = ProtocolClientFactory.create(clientId, SshClient.class, setting);
            log.info("[ACME-ASYNC] SSH连接成功: {}@{}", request.getSshUsername(), request.getSshHost());

            String webroot = request.getWebrootPath();
            // 创建验证目录
            String mkdirCmd = String.format("mkdir -p %s/.well-known/acme-challenge", webroot);
            sshClient.executeCommand(mkdirCmd);
            log.info("[ACME-ASYNC] 创建验证目录: {}", mkdirCmd);

            // 写入验证文件
            for (AcmeValidationInfo info : validationInfos) {
                if ("HTTP-01".equals(info.getChallengeType()) && StringUtils.isNotEmpty(info.getToken())) {
                    String writeCmd = String.format("echo '%s' > %s/.well-known/acme-challenge/%s",
                            info.getKeyAuthorization(), webroot, info.getToken());
                    sshClient.executeCommand(writeCmd);
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
                    ProtocolClientFactory.uncreated(clientId);
                } catch (Exception e) {
                    log.warn("[ACME-ASYNC] 关闭SSH连接失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 重新验证证书（用户手动部署完成后调用）
     *
     * @param certId 证书ID
     * @param userId 用户ID
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
                        updateCertStatus(cert, "failed", "ACME账户不存在");
                        pushMessage(userId, "重新验证失败", "ACME账户不存在", "error", certId, cert.getAcmeCertPrimaryDomain());
                        return;
                    }

                    pushMessage(userId, "正在重新验证", "正在连接 ACME 服务器...", "info", certId, cert.getAcmeCertPrimaryDomain());

                    // 组装域名列表
                    List<String> domains = new ArrayList<>();
                    domains.add(cert.getAcmeCertPrimaryDomain());
                    if (StringUtils.isNotEmpty(cert.getAcmeCertSan())) {
                        domains.addAll(Arrays.asList(cert.getAcmeCertSan().split(",")));
                    }
                    String challenge = cert.getAcmeCertChallengeType();

                    AcmeProvider provider = ServiceProvider.of(AcmeProvider.class).getNewExtension("acme4j");
                    if (provider == null) {
                        updateCertStatus(cert, "failed", "ACME 服务提供者不可用");
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
                            updateCertStatus(cert, "failed", connResult.getErrorMessage());
                            pushMessage(userId, "重新验证失败", "连接 ACME 服务器失败: " + connResult.getErrorMessage(), "error", certId, cert.getAcmeCertPrimaryDomain());
                            return;
                        }

                        pushMessage(userId, "正在重新验证", "正在进行域名验证...", "info", certId, cert.getAcmeCertPrimaryDomain());

                        AcmeCertificateResult certResult = provider.requestCertificate(domains, challenge);

                        if (certResult.isSuccess()) {
                            fillCertificateSuccess(cert, certResult);
                            certificateMapper.updateById(cert);

                            // 更新订单状态
                            MonitorAcmeOrder order = orderMapper.selectOne(
                                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MonitorAcmeOrder>()
                                            .eq(MonitorAcmeOrder::getAcmeCertId, certId)
                                            .orderByDesc(MonitorAcmeOrder::getUpdateTime)
                                            .last("LIMIT 1"));
                            if (order != null) {
                                order.setAcmeOrderStatus("valid");
                                orderMapper.updateById(order);
                            }

                            log.info("[ACME-ASYNC] 重新验证成功: certId={}", certId);
                            pushMessage(userId, "证书签发成功",
                                    String.format("域名 %s 的 SSL 证书已成功签发", cert.getAcmeCertPrimaryDomain()),
                                    "success", certId, cert.getAcmeCertPrimaryDomain());
                        } else {
                            cert.setAcmeCertStatus("validating");
                            cert.setAcmeCertLastError(certResult.getErrorMessage());
                            certificateMapper.updateById(cert);

                            log.warn("[ACME-ASYNC] 重新验证失败: certId={}, err={}", certId, certResult.getErrorMessage());
                            pushMessage(userId, "重新验证失败",
                                    String.format("域名验证失败: %s，请检查验证文件是否正确部署", certResult.getErrorMessage()),
                                    "error", certId, cert.getAcmeCertPrimaryDomain());
                        }
                    } catch (Exception e) {
                        log.error("[ACME-ASYNC] 重新验证异常: certId={}", certId, e);
                        updateCertStatus(cert, "validating", shortMsg(e));
                        pushMessage(userId, "重新验证失败",
                                String.format("验证异常: %s", shortMsg(e)),
                                "error", certId, cert.getAcmeCertPrimaryDomain());
                    } finally {
                        provider.close();
                    }
                });
    }
}
