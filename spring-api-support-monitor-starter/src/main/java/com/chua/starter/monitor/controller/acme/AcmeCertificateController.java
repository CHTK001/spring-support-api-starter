package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.pojo.AcmeApplyCertRequest;
import com.chua.starter.monitor.pojo.AcmeCertificateDetailVo;
import com.chua.starter.monitor.service.AcmeCertificateService;
import com.chua.starter.monitor.service.MonitorAcmeAccountService;
import com.chua.starter.monitor.service.MonitorAcmeCertificateService;
import com.chua.starter.monitor.service.MonitorAcmeOrderService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ACME 证书管理接口
 * 提供证书申请、续签、吊销、下载等完整的证书生命周期管理功能
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-10-17
 * @since 2025-12-06 补充完整的 CRUD 和分页查询接口
 */
@Slf4j
@RestController
@RequestMapping("/v1/cert")
@Api(tags = "ACME证书管理")
@Tag(name = "ACME证书管理")
@RequiredArgsConstructor
public class AcmeCertificateController {

    private final AcmeCertificateService acmeCertificateService;
    private final MonitorAcmeAccountService accountService;
    private final MonitorAcmeCertificateService certificateService;
    private final MonitorAcmeOrderService orderService;

    // ======================== 账户管理 ========================

    /**
     * 分页查询 ACME 账户列表 ForCert
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 账户分页列表
     */
    @GetMapping("/account/page")
    @Operation(summary = "分页查询ACME账户列表 ForCert")
    public ReturnPageResult<MonitorAcmeAccount> pageAccountForCert(Query<MonitorAcmeAccount> page, MonitorAcmeAccount entity) {
        return PageResultUtils.ok(accountService.page(page.createPage(),
                Wrappers.<MonitorAcmeAccount>lambdaQuery()
                        .like(StringUtils.isNotEmpty(entity.getAcmeAccountEmail()), MonitorAcmeAccount::getAcmeAccountEmail, entity.getAcmeAccountEmail())
                        .eq(StringUtils.isNotEmpty(entity.getAcmeAccountStatus()), MonitorAcmeAccount::getAcmeAccountStatus, entity.getAcmeAccountStatus())
                        .orderByDesc(MonitorAcmeAccount::getUpdateTime)));
    }

    /**
     * 查询所有 ACME 账户列表 ForCert
     *
     * @return 账户列表
     */
    @GetMapping("/account/list")
    @Operation(summary = "查询所有ACME账户列表 ForCert")
    public ReturnResult<List<MonitorAcmeAccount>> listAccountForCert() {
        return ReturnResult.ok(accountService.list(
                Wrappers.<MonitorAcmeAccount>lambdaQuery()
                        .orderByDesc(MonitorAcmeAccount::getUpdateTime)));
    }

    /**
     * 保存/更新 ACME 账户 ForCert
     *
     * @param account 账户
     * @return 账户ID
     */
    @PostMapping("/account/save")
    @ApiOperation("保存/更新ACME账户 ForCert")
    public ReturnResult<Integer> saveAccountForCert(@RequestBody MonitorAcmeAccount account) {
        return acmeCertificateService.saveAccountForCert(account);
    }

    /**
     * 获取 ACME 账户详情 ForCert
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    @GetMapping("/account/{accountId}")
    @Operation(summary = "获取ACME账户详情 ForCert")
    public ReturnResult<MonitorAcmeAccount> getAccountForCert(@ApiParam("账户ID") @PathVariable Integer accountId) {
        return ReturnResult.ok(accountService.getById(accountId));
    }

    /**
     * 删除 ACME 账户 ForCert
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @DeleteMapping("/account/{accountId}")
    @Operation(summary = "删除ACME账户 ForCert")
    public ReturnResult<Boolean> deleteAccountForCert(@ApiParam("账户ID") @PathVariable Integer accountId) {
        log.info("[ACME] 删除账户: accountId={}", accountId);
        // 检查是否有关联的证书
        long certCount = certificateService.count(
                Wrappers.<MonitorAcmeCertificate>lambdaQuery()
                        .eq(MonitorAcmeCertificate::getAcmeAccountId, accountId));
        if (certCount > 0) {
            return ReturnResult.illegal("该账户下存在证书，无法删除");
        }
        return ReturnResult.ok(accountService.removeById(accountId));
    }

    // ======================== 证书管理 ========================

    /**
     * 分页查询证书列表 ForCert
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 证书分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询证书列表 ForCert")
    public ReturnPageResult<MonitorAcmeCertificate> pageCertForCert(Query<MonitorAcmeCertificate> page, MonitorAcmeCertificate entity) {
        return PageResultUtils.ok(certificateService.page(page.createPage(),
                Wrappers.<MonitorAcmeCertificate>lambdaQuery()
                        .eq(entity.getAcmeAccountId() != null, MonitorAcmeCertificate::getAcmeAccountId, entity.getAcmeAccountId())
                        .like(StringUtils.isNotEmpty(entity.getAcmeCertPrimaryDomain()), MonitorAcmeCertificate::getAcmeCertPrimaryDomain, entity.getAcmeCertPrimaryDomain())
                        .eq(StringUtils.isNotEmpty(entity.getAcmeCertStatus()), MonitorAcmeCertificate::getAcmeCertStatus, entity.getAcmeCertStatus())
                        .eq(StringUtils.isNotEmpty(entity.getAcmeCertChallengeType()), MonitorAcmeCertificate::getAcmeCertChallengeType, entity.getAcmeCertChallengeType())
                        .orderByDesc(MonitorAcmeCertificate::getUpdateTime)));
    }

    /**
     * 申请证书 ForCert
     *
     * @param request 请求参数
     * @return 证书ID
     */
    @PostMapping("/apply")
    @ApiOperation("申请证书 ForCert")
    public ReturnResult<Integer> applyForCert(@RequestBody AcmeApplyCertRequest request) {
        return acmeCertificateService.applyCertificateForCert(request);
    }

    /**
     * 续签证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @PostMapping("/{certId}/renew")
    @ApiOperation("续签证书 ForCert")
    public ReturnResult<Boolean> renewForCert(@ApiParam("证书ID") @PathVariable Integer certId) {
        return acmeCertificateService.renewCertificateForCert(certId);
    }

    /**
     * 吊销证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @PostMapping("/{certId}/revoke")
    @ApiOperation("吊销证书 ForCert")
    public ReturnResult<Boolean> revokeForCert(@ApiParam("证书ID") @PathVariable Integer certId) {
        return acmeCertificateService.revokeCertificateForCert(certId);
    }

    /**
     * 获取证书详情 ForCert
     *
     * @param certId 证书ID
     * @return 证书详情（含账户信息和订单信息）
     */
    @GetMapping("/{certId}")
    @ApiOperation("获取证书详情 ForCert")
    public ReturnResult<AcmeCertificateDetailVo> getForCert(@ApiParam("证书ID") @PathVariable Integer certId) {
        return acmeCertificateService.getCertificateDetailForCert(certId);
    }

    /**
     * 删除证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @DeleteMapping("/{certId}")
    @Operation(summary = "删除证书 ForCert")
    public ReturnResult<Boolean> deleteCertForCert(@ApiParam("证书ID") @PathVariable Integer certId) {
        log.info("[ACME] 删除证书: certId={}", certId);
        // 同时删除关联订单
        orderService.remove(Wrappers.<MonitorAcmeOrder>lambdaQuery()
                .eq(MonitorAcmeOrder::getAcmeCertId, certId));
        return ReturnResult.ok(certificateService.removeById(certId));
    }

    /**
     * 下载证书文件 ForCert
     *
     * @param certId   证书ID
     * @param fileType 文件类型（cert/key/chain）
     * @param response HTTP响应
     */
    @GetMapping("/{certId}/download")
    @Operation(summary = "下载证书文件 ForCert")
    public void downloadCertForCert(
            @ApiParam("证书ID") @PathVariable Integer certId,
            @ApiParam("文件类型: cert/key/chain") @RequestParam(defaultValue = "cert") String fileType,
            HttpServletResponse response) {
        MonitorAcmeCertificate cert = certificateService.getById(certId);
        if (cert == null) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "证书不存在");
            } catch (IOException e) {
                log.error("[ACME] 下载证书响应错误: certId={}", certId, e);
            }
            return;
        }

        String content;
        String fileName;
        String domain = cert.getAcmeCertPrimaryDomain().replace("*", "wildcard");

        switch (fileType.toLowerCase()) {
            case "key" -> {
                content = cert.getAcmeCertKeyPem();
                fileName = domain + ".key";
            }
            case "chain" -> {
                content = cert.getAcmeCertChainPem();
                fileName = domain + "_chain.crt";
            }
            default -> {
                content = cert.getAcmeCertChainPem();
                fileName = domain + ".crt";
            }
        }

        if (StringUtils.isEmpty(content)) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "证书内容为空");
            } catch (IOException e) {
                log.error("[ACME] 下载证书响应错误: certId={}", certId, e);
            }
            return;
        }

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            log.info("[ACME] 下载证书成功: certId={}, fileType={}", certId, fileType);
        } catch (IOException e) {
            log.error("[ACME] 下载证书失败: certId={}", certId, e);
        }
    }

    /**
     * 手动触发续签检查 ForCert
     *
     * @return 续签结果
     */
    @PostMapping("/renew-check")
    @Operation(summary = "手动触发续签检查 ForCert")
    public ReturnResult<String> triggerRenewCheckForCert() {
        log.info("[ACME] 手动触发续签检查");
        acmeCertificateService.checkAndRenewSoonExpired();
        return ReturnResult.ok("续签检查已触发");
    }

    // ======================== 订单管理 ========================

    /**
     * 查询证书关联的订单列表 ForCert
     *
     * @param certId 证书ID
     * @return 订单列表
     */
    @GetMapping("/{certId}/orders")
    @Operation(summary = "查询证书关联的订单列表 ForCert")
    public ReturnResult<List<MonitorAcmeOrder>> listOrdersForCert(@ApiParam("证书ID") @PathVariable Integer certId) {
        return ReturnResult.ok(orderService.list(
                Wrappers.<MonitorAcmeOrder>lambdaQuery()
                        .eq(MonitorAcmeOrder::getAcmeCertId, certId)
                        .orderByDesc(MonitorAcmeOrder::getUpdateTime)));
    }

    /**
     * 获取证书统计信息 ForCert
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取证书统计信息 ForCert")
    public ReturnResult<AcmeCertificateDetailVo.CertStats> statsForCert() {
        return acmeCertificateService.getCertStatsForCert();
    }
}
