package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.network.ssl.AcmeCertificateResult;
import com.chua.common.support.network.ssl.AcmeConnectionResult;
import com.chua.common.support.network.ssl.AcmeProvider;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.pojo.acme.AcmeApplyCertRequest;
import com.chua.starter.monitor.pojo.acme.AcmeCertificateDetailVo;
import com.chua.starter.monitor.pojo.acme.CertStats;
import com.chua.starter.monitor.properties.AcmeProperties;
import com.chua.starter.monitor.service.acme.AcmeCertificateAsyncService;
import com.chua.starter.monitor.service.acme.support.AcmeCertificateSupport;
import com.chua.starter.monitor.service.acme.AcmeCertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ACME 证书服务实现，提供账户保存、证书申请（异步）、续签、吊销、详情、统计、
 * 到期自动续签与重新验证等完整的证书生命周期管理能力。
 *
   * @版本 1.0.0
 * @since 2025-10-17
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcmeCertificateServiceImpl implements AcmeCertificateService {

    /**
     * 挑战类型：HTTP-01
     */
    private static final String CHALLENGE_HTTP_01 = "HTTP-01";

    /**
     * 部署模式：手动
     */
    private static final String DEPLOY_MODE_MANUAL = "manual";

    /**
     * 证书状态：已提交待签发
     */
    private static final String STATUS_PENDING = "pending";

    /**
     * 证书状态：待验证
     */
    private static final String STATUS_VALIDATING = "validating";

    /**
     * 证书状态：已签发
     */
    private static final String STATUS_VALID = "valid";

    /**
     * 证书状态：已吊销
     */
    private static final String STATUS_REVOKED = "revoked";

    /**
     * 证书状态：签发失败
     */
    private static final String STATUS_FAILED = "failed";

    /**
     * 证书状态：已过期
     */
    private static final String STATUS_EXPIRED = "expired";

    /**
     * 自动续签窗口下限天数，防止配置值过小导致高频无效续签
     */
    private static final int MIN_RENEW_DAYS = 7;

    /**
     * 即将到期判定窗口天数
     */
    private static final int SOON_EXPIRING_DAYS = 30;

    /**
     * 时间格式化器，与证书表时间字段格式保持一致
     */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
      * ACME 账户 映射器，用于账户查询与持久化
     */
    private final MonitorAcmeAccountMapper accountMapper;

    /**
      * ACME 证书 映射器，用于证书查询与持久化
     */
    private final MonitorAcmeCertificateMapper certificateMapper;

    /**
      * ACME 订单 映射器，用于订单查询与持久化
     */
    private final MonitorAcmeOrderMapper orderMapper;

    /**
     * ACME 配置，读取续签提前天数等参数
     */
    private final AcmeProperties acmeProperties;

    /**
     * 认证服务，用于获取当前登录用户信息以便消息推送
     */
    private final AuthService authService;

    /**
     * 异步证书服务，负责后台执行证书申请与重新验证
     */
    private final AcmeCertificateAsyncService asyncService;

    /**
      * 保存/更新 ACME 账户 forcert
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
        // 服务商类型为空时默认 ACME 协议
        if (StringUtils.isNullOrEmpty(account.getAcmeAccountProviderType())) {
            account.setAcmeAccountProviderType(AcmeCertificateSupport.PROVIDER_TYPE_ACME);
        }
        log.info("[ACME] 保存账户: email={}, server={}, providerType={}", account.getAcmeAccountEmail(), account.getAcmeAccountServer(), account.getAcmeAccountProviderType());
        if (account.getAcmeAccountId() == null) {
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
        return ReturnResult.ok(account.getAcmeAccountId());
    }

    /**
      * 申请证书 forcert（异步执行，立即返回）
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
        String sanStr = String.join(",", sans.stream().filter(StringUtils::isNotEmpty).toList());
        String challenge = StringUtils.defaultString(request.getChallengeType(), CHALLENGE_HTTP_01);

        // 新建证书记录（pending）
        String deployMode = StringUtils.defaultString(request.getDeployMode(), DEPLOY_MODE_MANUAL);
        MonitorAcmeCertificate cert = new MonitorAcmeCertificate();
        cert.setAcmeAccountId(request.getAcmeAccountId());
        cert.setAcmeCertPrimaryDomain(request.getPrimaryDomain());
        cert.setAcmeCertSan(sanStr);
        cert.setAcmeCertChallengeType(challenge);
        cert.setAcmeCertStatus(STATUS_PENDING);
        cert.setAcmeCertDeployMode(deployMode);
        cert.setAcmeCertWebrootPath(request.getWebrootPath());
        certificateMapper.insert(cert);
        int certId = cert.getAcmeCertId();

        log.info("[ACME] 提交证书申请, certId={}, domain={}, san={}, challenge={}", certId, cert.getAcmeCertPrimaryDomain(), sanStr, challenge);

 // 获取当前用户标识用于消息推送
        Integer userId = null;
        try {
            CurrentUser currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getUserId() != null) {
                userId = Integer.parseInt(currentUser.getUserId());
            }
        } catch (Exception e) {
            log.warn("[ACME] 获取当前用户失败，消息推送可能受影响: {}", e.getMessage());
        }

        // 异步执行证书申请
        asyncService.applyCertificateAsync(certId, request, userId);

        return ReturnResult.ok(certId, "证书申请已提交，请在消息中心查看进度");
    }

    /**
      * 续签证书 forcert
     *
     * @param certId 证书标识
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

        // 通过 SPI 获取 ACME 提供者（按账户服务商类型路由）
        AcmeProvider provider = ServiceProvider.of(AcmeProvider.class)
                .getNewExtension(AcmeCertificateSupport.providerName(account.getAcmeAccountProviderType()));
        if (provider == null) {
            return ReturnResult.illegal("ACME 服务提供者不可用");
        }

 // 连接 ACME 服务器（支持 EAB，zerossl 等 CA 需要）
        AcmeConnectionResult connResult = provider.connect(
                account.getAcmeAccountServer(),
                account.getAcmeAccountEmail(),
                account.getAcmeAccountPrivateKeyPem(),
                account.getAcmeAccountEabKid(),
                account.getAcmeAccountEabHmacKey()
        );
        if (!connResult.isSuccess()) {
            provider.close();
            return ReturnResult.illegal("连接 ACME 服务器失败: " + connResult.getError());
        }

        // 续签证书
        List<String> domains = AcmeCertificateSupport.buildDomains(cert);
        String challenge = cert.getAcmeCertChallengeType();
        try {
            AcmeCertificateResult certResult = provider.renewCertificate(domains, challenge);
            if (certResult.isSuccess()) {
                fillCertificateSuccess(cert, certResult);
                certificateMapper.updateById(cert);
                log.info("[ACME] 续签成功: id={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());
                return ReturnResult.ok(true, "续签成功");
            } else {
                cert.setAcmeCertLastError(certResult.getError());
                certificateMapper.updateById(cert);
                return ReturnResult.illegal("续签失败: " + certResult.getError());
            }
        } catch (Exception e) {
            log.error("[ACME] 续签失败: id={}", certId, e);
            cert.setAcmeCertLastError(AcmeCertificateSupport.shortMsg(e));
            certificateMapper.updateById(cert);
            return ReturnResult.illegal("续签失败: " + e.getMessage());
        } finally {
            provider.close();
        }
    }

    /**
      * 吊销证书 forcert
     *
     * @param certId 证书标识
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

 // CAS 服务商需 证书id+instanceid 才能吊销，当前业务层无该信息，直接提示
        if (AcmeCertificateSupport.PROVIDER_TYPE_CAS.equalsIgnoreCase(account.getAcmeAccountProviderType())) {
            return ReturnResult.illegal("阿里云 CAS 吊销需证书实例ID，暂不支持在线吊销，请前往阿里云控制台操作");
        }

        // 通过 SPI 获取 ACME 提供者（按账户服务商类型路由）
        AcmeProvider provider = ServiceProvider.of(AcmeProvider.class)
                .getNewExtension(AcmeCertificateSupport.providerName(account.getAcmeAccountProviderType()));
        if (provider == null) {
            return ReturnResult.illegal("ACME 服务提供者不可用");
        }

        // 连接 ACME 服务器
        AcmeConnectionResult connResult = provider.connect(
                account.getAcmeAccountServer(),
                account.getAcmeAccountEmail(),
                account.getAcmeAccountPrivateKeyPem()
        );
        if (!connResult.isSuccess()) {
            provider.close();
            return ReturnResult.illegal("连接 ACME 服务器失败: " + connResult.getError());
        }

        try {
            boolean revoked = provider.revokeCertificate(cert.getAcmeCertChainPem());
            if (revoked) {
                cert.setAcmeCertStatus(STATUS_REVOKED);
                certificateMapper.updateById(cert);
                log.info("[ACME] 证书吊销成功: id={}", certId);
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.illegal("吊销失败");
            }
        } catch (Exception e) {
            log.error("[ACME] 吊销失败: id={}", certId, e);
            return ReturnResult.illegal("吊销失败: " + e.getMessage());
        } finally {
            provider.close();
        }
    }

    /**
      * 获取证书详情 forcert（含账户和订单信息）
     *
     * @param certId 证书标识
     * @return 证书详情VO
     */
    @Override
    public ReturnResult<AcmeCertificateDetailVo> getCertificateDetailForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }

        AcmeCertificateDetailVo vo = new AcmeCertificateDetailVo();
        vo.setCertificate(cert);

        // 查询关联账户
        if (cert.getAcmeAccountId() != null) {
            vo.setAccount(accountMapper.selectById(cert.getAcmeAccountId()));
        }

        // 查询关联订单
        List<MonitorAcmeOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<MonitorAcmeOrder>()
                .eq(MonitorAcmeOrder::getAcmeCertId, certId)
                .orderByDesc(MonitorAcmeOrder::getUpdateTime));
        vo.setOrders(orders);

 // 生成cURL自检命令（仅对待验证状态且HTTP-01挑战类型）
        if (STATUS_VALIDATING.equals(cert.getAcmeCertStatus()) && CHALLENGE_HTTP_01.equals(cert.getAcmeCertChallengeType())) {
            List<String> curlCommands = new ArrayList<>();
            if (!orders.isEmpty()) {
                MonitorAcmeOrder latestOrder = orders.get(0);
                String authzUrlsJson = latestOrder.getAcmeAuthzUrls();
                if (StringUtils.isNotEmpty(authzUrlsJson)) {
                    try {
                        List<Map<String, Object>> validations = Json.fromJson(authzUrlsJson, List.class);
                        if (validations != null) {
                            for (Map<String, Object> v : validations) {
                                String domain = (String) v.get("domain");
                                String httpPath = (String) v.get("httpPath");
                                if (StringUtils.isNotEmpty(domain) && StringUtils.isNotEmpty(httpPath)) {
 // 生成cURL命令，使用-I检查响应头，使用-s静默模式
                                    String curlCmd = String.format("curl -s -o /dev/null -w '%%{http_code}' http://%s%s", domain, httpPath);
                                    curlCommands.add(curlCmd);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("[ACME] 解析验证信息JSON失败: certId={}, error={}", certId, e.getMessage());
                    }
                }
            }
            vo.setCurlCommands(curlCommands);
        }

        // 计算到期天数
        if (StringUtils.isNotEmpty(cert.getAcmeCertNotAfter())) {
            try {
                LocalDateTime notAfter = LocalDateTime.parse(cert.getAcmeCertNotAfter(), DATE);
                long days = ChronoUnit.DAYS.between(LocalDateTime.now(), notAfter);
                vo.setDaysUntilExpiry(days);
                vo.setSoonExpiring(days <= SOON_EXPIRING_DAYS && days >= 0);
            } catch (Exception e) {
                log.warn("[ACME] 解析证书到期时间失败: certId={}, notAfter={}", certId, cert.getAcmeCertNotAfter());
            }
        }

        // 设置当前步骤信息（用于前端展示申请流程）
        String status = cert.getAcmeCertStatus();
        if (STATUS_PENDING.equals(status)) {
            vo.setCurrentStep(1);
            vo.setStepDescription("第1步: 提交申请 - 正在连接 ACME 服务器...");
        } else if (STATUS_VALIDATING.equals(status)) {
            vo.setCurrentStep(2);
            vo.setStepDescription("第2步: 等待验证 - 请部署验证文件后点击重新验证");
        } else if (STATUS_VALID.equals(status)) {
            vo.setCurrentStep(3);
            vo.setStepDescription("第3步: 签发完成 - 证书已成功签发");
        } else if (STATUS_FAILED.equals(status)) {
            vo.setCurrentStep(3);
            vo.setStepDescription("第3步: 签发失败 - " + StringUtils.defaultString(cert.getAcmeCertLastError(), "未知错误"));
        } else if (STATUS_REVOKED.equals(status)) {
            vo.setCurrentStep(3);
            vo.setStepDescription("第3步: 证书已吐销");
        } else if (STATUS_EXPIRED.equals(status)) {
            vo.setCurrentStep(3);
            vo.setStepDescription("第3步: 证书已过期");
        }

        return ReturnResult.ok(vo);
    }

    /**
      * 获取证书统计信息 forcert
     *
     * @return 统计信息
     */
    @Override
    public ReturnResult<CertStats> getCertStatsForCert() {
        CertStats stats = new CertStats();

        // 账户总数
        stats.setAccountCount(accountMapper.selectCount(null));

        // 证书总数
        stats.setCertCount(certificateMapper.selectCount(null));

        // 按状态统计
        stats.setValidCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_VALID)));
        stats.setPendingCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .in(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_PENDING, STATUS_VALIDATING)));
        stats.setRevokedCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_REVOKED)));
        stats.setFailedCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_FAILED)));

        // 统计即将到期和已过期的证书
        String now = LocalDateTime.now().format(DATE);
        String soon = LocalDateTime.now().plusDays(SOON_EXPIRING_DAYS).format(DATE);

        stats.setExpiringCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_VALID)
                .ge(MonitorAcmeCertificate::getAcmeCertNotAfter, now)
                .le(MonitorAcmeCertificate::getAcmeCertNotAfter, soon)));

        stats.setExpiredCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_VALID)
                .lt(MonitorAcmeCertificate::getAcmeCertNotAfter, now)));

        return ReturnResult.ok(stats);
    }

    /**
     * 检查并续签到期前 N 天的证书（调度）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndRenewSoonExpired() {
        int days = Math.max(acmeProperties.getRenewDaysBefore(), MIN_RENEW_DAYS);
        String deadline = LocalDate.now().plusDays(days).atStartOfDay().format(DATE);
        List<MonitorAcmeCertificate> list = certificateMapper.selectList(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, STATUS_VALID)
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

    /**
      * 重新验证证书 forcert（用户手动部署完成后调用）
     *
     * @param certId 证书标识
     * @return 是否提交成功
     */
    @Override
    public ReturnResult<Boolean> retryValidationForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }
        if (!STATUS_VALIDATING.equals(cert.getAcmeCertStatus())) {
            return ReturnResult.illegal("证书状态不是待验证，无法重新验证");
        }

 // 获取当前用户标识用于消息推送
        Integer userId = null;
        try {
            CurrentUser currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getUserId() != null) {
                userId = Integer.parseInt(currentUser.getUserId());
            }
        } catch (Exception e) {
            log.warn("[ACME] 获取当前用户失败: {}", e.getMessage());
        }

        log.info("[ACME] 提交重新验证: certId={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());
        asyncService.retryValidationAsync(certId, userId);
        return ReturnResult.ok(true, "重新验证已提交，请在消息中心查看进度");
    }

    /**
     * 成功签发/续签后回填证书信息到实体
     *
     * @param target 目标证书实体
     * @param src    证书申请结果
     */
    private void fillCertificateSuccess(MonitorAcmeCertificate target, AcmeCertificateResult src) {
        AcmeCertificateSupport.fillCertificateSuccess(target, src, acmeProperties.getRenewDaysBefore());
    }
}
