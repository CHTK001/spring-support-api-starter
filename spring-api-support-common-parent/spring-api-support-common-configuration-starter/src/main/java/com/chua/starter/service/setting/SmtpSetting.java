package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
import com.chua.common.support.utils.StringUtils;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * smtp设置
 * @since 2024/9/4
 * @author CH
 */
@Data
@GlobalSettingGroup("smtp")
@ConditionalOnClass(name = {"org.springframework.mail.javamail.JavaMailSenderImpl"})
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
     * 登录名，多数服务商与发件人地址一致
     */
    private String username;
    /**
     * 授权码
     */
    private String password;

    /**
     * smtp端口
     */
    private int port = 465;

    /**
     * 是否走登录认证
     */
    private boolean auth = true;

    /**
     * 是否启用 STARTTLS（587 等明文端口升级为 TLS）
     */
    private boolean starttls = false;

    /**
     * 是否使用隐式 TLS（SMTPS，通常 465）
     */
    private boolean sslEnable = true;

    /**
     * imap主机(IMAPS)，为空表示服务器未开通收信通道
     */
    private String imapHost;

    /**
     * imap端口，IMAPS 默认 993
     */
    private int imapPort = 993;

    /**
     * 是否加载
     */
    private volatile boolean isLoaded;

    @Override
    /**
     * Upgrade
    */
    public void upgrade(SmtpSetting smtpSetting) {
        this.from = smtpSetting.from;
        this.host = smtpSetting.host;
        this.username = smtpSetting.username;
        this.password = smtpSetting.password;
        this.port = smtpSetting.port;
        this.auth = smtpSetting.auth;
        this.starttls = smtpSetting.starttls;
        this.sslEnable = smtpSetting.sslEnable;
        this.imapHost = smtpSetting.imapHost;
        this.imapPort = smtpSetting.imapPort;
        this.isLoaded = true;
    }

    /**
     * 登录名：未显式配置时回退到发件人地址
     *
     * @return 发信登录名
     */
    public String resolveUsername() {
        return StringUtils.isNotBlank(username) ? username : from;
    }

    /**
     * 服务器是否配置了可用的发信通道，决定邮箱验证码能否下发
     *
     * @return true 表示 host/账号/授权码齐备
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(host)
                && StringUtils.isNotBlank(resolveUsername())
                && StringUtils.isNotBlank(password)
                && port > 0;
    }

    /**
     * 服务器是否配置了可用的收信(IMAPS)通道，收信账号与发信共用
     *
     * @return true 表示 host/账号/密码齐备
     */
    public boolean isImapValid() {
        return StringUtils.isNotBlank(imapHost)
                && StringUtils.isNotBlank(resolveUsername())
                && StringUtils.isNotBlank(password)
                && imapPort > 0;
    }
}
