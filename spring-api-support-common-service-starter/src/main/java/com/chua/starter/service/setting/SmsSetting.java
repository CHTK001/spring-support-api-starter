package com.chua.starter.service.setting;

import com.chua.starter.common.support.function.Upgrade;
import com.chua.common.support.utils.StringUtils;
import lombok.Data;

@Data
public class SmsSetting implements Upgrade<SmsSetting> {
    private String appType;
    private String appId;
    private String appSecret;
    private String appSign;

    @Override
    public void upgrade(SmsSetting smsSetting) {
        this.appType = smsSetting.appType;
        this.appId = smsSetting.appId;
        this.appSecret = smsSetting.appSecret;
        this.appSign = smsSetting.appSign;
    }

    public boolean isValid() {
        return !StringUtils.isEmpty(appId) && !StringUtils.isEmpty(appSecret);
    }
}
