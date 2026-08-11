package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
import com.chua.common.support.utils.StringUtils;
import lombok.Data;

/**
 * sms设置
 *
 * @author CH
 * @since 2024/9/4
 */
@Data
@GlobalSettingGroup("sms")
public class SmsSetting implements Upgrade<SmsSetting> {

    /**
     * 短信厂家, ALIYUN,BAIDU
     */
    private String appType;

    /**
     * 密钥
     */
    private String appId;

    /**
     * 密钥
     */
    private String appSecret;

    /**
     * 签名
     */
    private String appSign;

    @Override
    public void upgrade(SmsSetting smsSetting) {
        this.appType = smsSetting.appType;
        this.appId = smsSetting.appId;
        this.appSecret = smsSetting.appSecret;
        this.appSign = smsSetting.appSign;
    }

    /**
     * 是否有效
     *
     * @return boolean
     */
    public boolean isValid() {
        return !StringUtils.isEmpty(appId) && !StringUtils.isEmpty(appSecret);
    }
}
