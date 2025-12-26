package com.chua.starter.service.setting;

import com.chua.advanced.support.lang.mail.MailConfiguration;
import com.chua.advanced.support.lang.mail.MailSender;
import com.chua.common.support.function.Upgrade;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

/**
 * smtp设置
 * @author CH
 * @since 2024/9/4
 */
@Data
@Slf4j
public class SmtpSetting implements Upgrade<SmtpSetting> {
    /**
     * 发送人
     */
    private String from;
    /**
     * 主机
     */
    private String host = "smtp.qq.com";
    /**
     * 授权码
     */
    private String password;

    /**
     * smtp端口
     */
    private int port = 25;

    /**
     * 邮箱发送器
     */
    private volatile MailSender mailSender;

    /**
     * 是否加载
     */
    private volatile boolean isLoaded;

    private static final String SPRING_HTML_MAIL_SENDER_CLASS = "com.chua.starter.email.support.SpringHtmlMailSender";

    @Override
    public void upgrade(SmtpSetting smtpSetting) {
        this.host = smtpSetting.getHost();
        this.password = smtpSetting.getPassword();
        this.port = smtpSetting.getPort();
        this.isLoaded = true;
        MailConfiguration mailConfiguration = new MailConfiguration();
        mailConfiguration.setSmtpHost(smtpSetting.getHost());
        mailConfiguration.setPassword(smtpSetting.getPassword());
        mailConfiguration.setSmtpPort(smtpSetting.getPort());
        mailConfiguration.setUsername(smtpSetting.getFrom());
        try {
            // 使用反射加载，避免硬编码依赖
            Class<?> mailSenderClass = Class.forName(SPRING_HTML_MAIL_SENDER_CLASS);
            Constructor<?> constructor = mailSenderClass.getConstructor(MailConfiguration.class);
            MailSender sender = (MailSender) constructor.newInstance(mailConfiguration);
            SpringBeanUtils.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(sender);
            setMailSender(sender);
        } catch (ClassNotFoundException e) {
            log.debug("Email module not available: {}", SPRING_HTML_MAIL_SENDER_CLASS);
        } catch (Exception e) {
            log.warn("Failed to initialize mail sender: {}", e.getMessage());
        }
    }
}
