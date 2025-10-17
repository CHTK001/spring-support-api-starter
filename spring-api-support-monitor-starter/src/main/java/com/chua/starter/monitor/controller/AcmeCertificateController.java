package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.pojo.AcmeApplyCertRequest;
import com.chua.starter.monitor.service.AcmeCertificateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ACME 证书接口
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@RestController
@RequestMapping("/v1/cert")
@Api(tags = "ACME证书管理")
@Tag(name = "ACME证书管理")
@RequiredArgsConstructor
public class AcmeCertificateController {

    private final AcmeCertificateService acmeCertificateService;

    /**
     * 保存/更新ACME账户 ForCert
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
     * @return 证书详情
     */
    @GetMapping("/{certId}")
    @ApiOperation("获取证书详情 ForCert")
    public ReturnResult<MonitorAcmeCertificate> getForCert(@ApiParam("证书ID") @PathVariable Integer certId) {
        return acmeCertificateService.getCertificateForCert(certId);
    }
}
