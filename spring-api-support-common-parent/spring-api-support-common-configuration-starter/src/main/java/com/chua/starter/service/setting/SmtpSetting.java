package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
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
    /** Upgrade */
    public void upgrade(SmtpSetting smtpSetting) {
        this.host = smtpSetting.host;
        this.password = smtpSetting.password;
        this.port = smtpSetting.port;
        this.isLoaded = true;
    }
}
