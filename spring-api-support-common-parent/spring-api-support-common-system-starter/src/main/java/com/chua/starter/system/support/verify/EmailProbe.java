package com.chua.starter.system.support.verify;

import com.chua.starter.service.setting.SmtpSetting;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.Folder;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 邮箱服务器连通性探测
 *
 * <p>系统配置里的 SMTP/IMAP 只有真正握手一次才算生效，否则管理员面对的是"填了但没人用"的假配置。
 * 本类分别用 SMTP(SMTPS) 与 IMAPS 完成连接+认证，并把失败原因原样带回，供配置页直接展示。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
public final class EmailProbe {

    /**
     * 连接超时(毫秒)
     */
    private static final String CONNECT_TIMEOUT = "5000";

    /**
     * 读取超时(毫秒)
     */
    private static final String READ_TIMEOUT = "8000";

    /**
     * 探测结果
     *
     * @param success 是否握手并认证成功
     * @param message 成功为服务端标识，失败为异常原因
     * @param cost    耗时(毫秒)
     */
    public record ProbeResult(boolean success, String message, long cost) {
    }

    private EmailProbe() {
    }

    /**
     * 探测发信通道：连接 SMTP 并提交认证
     *
     * @param setting 邮箱配置
     * @return 探测结果，配置缺失时直接返回未配置
     */
    public static ProbeResult probeSmtp(SmtpSetting setting) {
        if (null == setting || !setting.isValid()) {
            return new ProbeResult(false, "SMTP 未配置（主机/账号/授权码需齐备）", 0);
        }
        long start = System.currentTimeMillis();
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", setting.getHost());
        props.setProperty("mail.smtp.port", String.valueOf(setting.getPort()));
        props.setProperty("mail.smtp.auth", String.valueOf(setting.isAuth()));
        props.setProperty("mail.smtp.connectiontimeout", CONNECT_TIMEOUT);
        props.setProperty("mail.smtp.timeout", READ_TIMEOUT);
        if (setting.isSslEnable()) {
            props.setProperty("mail.smtp.ssl.enable", "true");
        } else if (setting.isStarttls()) {
            props.setProperty("mail.smtp.starttls.enable", "true");
        }
        try {
            Transport transport = Session.getInstance(props).getTransport(setting.isSslEnable() ? "smtps" : "smtp");
            transport.connect(setting.getHost(), setting.getPort(), setting.resolveUsername(), setting.getPassword());
            transport.close();
            return new ProbeResult(true, "SMTP " + setting.getHost() + ":" + setting.getPort() + " 认证通过",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.warn("SMTP 探测失败: {}:{}, 原因: {}", setting.getHost(), setting.getPort(), e.getMessage());
            return new ProbeResult(false, "SMTP " + setting.getHost() + ":" + setting.getPort() + " 失败: " + e.getMessage(),
                    System.currentTimeMillis() - start);
        }
    }

    /**
     * 探测收信通道：连接 IMAPS、打开收件箱并读取邮件总数
     *
     * @param setting 邮箱配置
     * @return 探测结果，配置缺失时直接返回未配置
     */
    public static ProbeResult probeImap(SmtpSetting setting) {
        if (null == setting || !setting.isImapValid()) {
            return new ProbeResult(false, "IMAP 未配置（主机需填写，账号与发信共用）", 0);
        }
        long start = System.currentTimeMillis();
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", setting.getImapHost());
        props.setProperty("mail.imaps.port", String.valueOf(setting.getImapPort()));
        props.setProperty("mail.imaps.ssl.enable", "true");
        props.setProperty("mail.imaps.connectiontimeout", CONNECT_TIMEOUT);
        props.setProperty("mail.imaps.timeout", READ_TIMEOUT);
        Store store = null;
        try {
            Session session = Session.getInstance(props);
            store = session.getStore("imaps");
            store.connect(setting.getImapHost(), setting.getImapPort(),
                    setting.resolveUsername(), setting.getPassword());
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            int total = inbox.getMessageCount();
            inbox.close(false);
            return new ProbeResult(true, "IMAP " + setting.getImapHost() + ":" + setting.getImapPort()
                    + " 认证通过，收件箱邮件数 " + total, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.warn("IMAP 探测失败: {}:{}, 原因: {}", setting.getImapHost(), setting.getImapPort(), e.getMessage());
            return new ProbeResult(false, "IMAP " + setting.getImapHost() + ":" + setting.getImapPort()
                    + " 失败: " + e.getMessage(), System.currentTimeMillis() - start);
        } finally {
            if (null != store) {
                try {
                    store.close();
                } catch (Exception e) {
                    log.debug("[email] 关闭存储失败，已忽略: {}", e.getMessage());
                }
            }
        }
    }
}
