package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.lang.mail.MailConfiguration;
import com.chua.common.support.lang.mail.MailSender;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.email.support.SpringHtmlMailSender;
import lombok.Data;

/**
 * smtp设置
 * @author CH
 * @since 2024/9/4
 */
@Data
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
            SpringHtmlMailSender springHtmlMailSender = new SpringHtmlMailSender(mailConfiguration);
            SpringBeanUtils.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(springHtmlMailSender);
            setMailSender(springHtmlMailSender);
        } catch (Exception ignored) {
        }
    }
}
