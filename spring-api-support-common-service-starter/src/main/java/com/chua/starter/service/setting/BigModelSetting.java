package com.chua.starter.service.setting;

import com.chua.starter.common.support.function.Upgrade;
import lombok.Data;

@Data
public class BigModelSetting implements Upgrade<BigModelSetting> {
    private String appType;
    private String appId;
    private String appSecret;
    private String appKey;
    private String host;
    private String baseUrl;

    @Override
    public void upgrade(BigModelSetting bigModelSetting) {
        if (null != bigModelSetting) {
            this.appType = bigModelSetting.appType;
            this.appId = bigModelSetting.appId;
            this.appSecret = bigModelSetting.appSecret;
            this.appKey = bigModelSetting.appKey;
            this.host = bigModelSetting.host;
            this.baseUrl = bigModelSetting.baseUrl;
        }
    }
}
