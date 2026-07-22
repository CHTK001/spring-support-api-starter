package com.chua.starter.service.setting;

import com.chua.starter.common.support.function.Upgrade;
import lombok.Data;

@Data
public class SmtpSetting implements Upgrade<SmtpSetting> {
    private String from;
    private String host = "smtp.qq.com";
    private String password;
    private int port = 25;
    private volatile boolean isLoaded;

    @Override
    public void upgrade(SmtpSetting smtpSetting) {
        this.host = smtpSetting.host;
        this.password = smtpSetting.password;
        this.port = smtpSetting.port;
        this.isLoaded = true;
    }
}
