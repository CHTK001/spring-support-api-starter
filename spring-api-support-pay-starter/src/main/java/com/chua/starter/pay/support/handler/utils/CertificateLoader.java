package com.chua.starter.pay.support.handler.utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @author CH
 * @since 2025/4/16 10:15
 */
public class CertificateLoader {
    /**
     * 从classpath加载微信支付平台证书
     */
    public static X509Certificate loadFromClasspath(String certPath) throws Exception {
        try (InputStream certStream = CertificateLoader.class
                .getResourceAsStream(certPath)) {
            if (certStream == null) {
                throw new IllegalStateException("证书文件未找到: " + certPath);
            }
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(certStream);
        }
    }

    /**
     * 从文件系统加载证书
     */
    public static X509Certificate loadFromFileSystem(String filePath) throws Exception {
        try (InputStream certStream = new FileInputStream(filePath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(certStream);
        }
    }

    /**
     * 从字符串内容加载证书
     */
    public static X509Certificate loadFromString(String certContent) throws Exception {
        try (InputStream certStream = new ByteArrayInputStream(certContent.getBytes())) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(certStream);
        }
    }
}
