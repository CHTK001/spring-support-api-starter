package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.pojo.AcmeApplyCertRequest;

/**
 * ACME 证书服务
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
public interface AcmeCertificateService {

    /**
     * 保存/更新 ACME 账户 ForCert
     *
     * @param account 账户
     * @return 结果（账户ID）
     */
    ReturnResult<Integer> saveAccountForCert(MonitorAcmeAccount account);

    /**
     * 申请证书 ForCert
     *
     * @param request 请求
     * @return 证书ID
     */
    ReturnResult<Integer> applyCertificateForCert(AcmeApplyCertRequest request);

    /**
     * 续签证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    ReturnResult<Boolean> renewCertificateForCert(Integer certId);

    /**
     * 吊销证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    ReturnResult<Boolean> revokeCertificateForCert(Integer certId);

    /**
     * 获取证书详情 ForCert
     *
     * @param certId 证书ID
     * @return 证书
     */
    ReturnResult<MonitorAcmeCertificate> getCertificateForCert(Integer certId);
}
