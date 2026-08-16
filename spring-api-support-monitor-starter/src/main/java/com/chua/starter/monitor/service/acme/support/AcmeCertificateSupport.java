package com.chua.starter.monitor.service.acme.support;

import com.chua.acme.support.AcmeCertificateResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ACME 证书公共支持类，集中提供域名列表构建、错误信息截断与签发结果回填等公共逻辑，
 * 供 {@code AcmeCertificateServiceImpl} 与 {@code AcmeCertificateAsyncService} 复用，
 * 避免重复实现。
 *
 * @author CH
 * @since 4.0.0.42
 */
public final class AcmeCertificateSupport {

    /**
     * 证书状态：已签发
     */
    private static final String STATUS_VALID = "valid";

    /**
     * 自动续签窗口下限天数，防止配置值过小导致高频无效续签
     */
    private static final int MIN_RENEW_DAYS = 7;

    /**
     * 错误信息最大长度，避免超出数据库字段长度
     */
    private static final int MAX_ERROR_LENGTH = 512;

    /**
     * 时间格式化器，与证书表时间字段格式保持一致
     */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 私有构造方法，禁止实例化
     */
    private AcmeCertificateSupport() {
    }

    /**
     * 构建域名列表（主域名 + SAN）。
     *
     * @param cert 证书实体
     * @return 域名列表
     */
    public static List<String> buildDomains(MonitorAcmeCertificate cert) {
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
     * 提取简短异常信息，限制长度避免超出数据库字段。
     *
     * @param e 异常
     * @return 简短信息
     */
    public static String shortMsg(Throwable e) {
        String msg = e.getMessage();
        return shortMsg(msg);
    }

    /**
     * 限制错误信息长度，避免超出数据库字段。
     *
     * @param error 原始错误信息
     * @return 限制长度后的信息
     */
    public static String shortMsg(String error) {
        if (error == null) {
            return "";
        }
        return error.length() > MAX_ERROR_LENGTH ? error.substring(0, MAX_ERROR_LENGTH) : error;
    }

    /**
     * 成功签发后回填证书信息到实体。
     *
     * @param target         目标证书实体
     * @param src            证书申请结果
     * @param renewDaysBefore 续签提前天数（配置值）
     */
    public static void fillCertificateSuccess(MonitorAcmeCertificate target, AcmeCertificateResult src, int renewDaysBefore) {
        target.setAcmeCertStatus(STATUS_VALID);
        if (src.getNotBefore() != null) {
            target.setAcmeCertNotBefore(src.getNotBefore().format(DATE));
        }
        if (src.getNotAfter() != null) {
            target.setAcmeCertNotAfter(src.getNotAfter().format(DATE));
            // 计划续签日期：到期前 N 天（下限 MIN_RENEW_DAYS 天）
            LocalDateTime renewAt = src.getNotAfter().minusDays(Math.max(renewDaysBefore, MIN_RENEW_DAYS));
            target.setAcmeCertRenewAt(renewAt.format(DATE));
        }
        target.setAcmeCertChainPem(src.getCertificatePem());
        target.setAcmeCertKeyPem(src.getPrivateKeyPem());
        target.setAcmeCertLastError(null);
    }
}