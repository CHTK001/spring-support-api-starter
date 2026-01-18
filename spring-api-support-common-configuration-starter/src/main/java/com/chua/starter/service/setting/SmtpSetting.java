package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
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
     * 是否加载
     */
    private volatile boolean isLoaded;

    @Override
    public void upgrade(SmtpSetting smtpSetting) {
        this.host = smtpSetting.host;
        this.password = smtpSetting.password;
        this.port = smtpSetting.port;
        this.isLoaded = true;
        // TODO: 邮件发送器初始化需要 MailConfiguration 和 MailSender 类，待 utils-support-extension-starter 模块提供
    }
}
