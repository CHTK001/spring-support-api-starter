package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.pojo.AcmeApplyCertRequest;
import com.chua.starter.monitor.pojo.AcmeCertificateDetailVo;

/**
 * ACME 证书服务接口
 * 提供证书生命周期管理功能
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-10-17
 * @since 2025-12-06 补充完整接口
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
     * 获取证书详情 ForCert（含账户和订单信息）
     *
     * @param certId 证书ID
     * @return 证书详情VO
     */
    ReturnResult<AcmeCertificateDetailVo> getCertificateDetailForCert(Integer certId);

    /**
     * 获取证书统计信息 ForCert
     *
     * @return 统计信息
     */
    ReturnResult<AcmeCertificateDetailVo.CertStats> getCertStatsForCert();

    /**
     * 检查并续签即将到期的证书（定时任务调用）
     */
    void checkAndRenewSoonExpired();
}
