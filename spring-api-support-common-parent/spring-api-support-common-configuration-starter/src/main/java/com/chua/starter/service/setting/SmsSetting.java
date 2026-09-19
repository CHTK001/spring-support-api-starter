package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.setting.GlobalSettingGroup;
import com.chua.common.support.utils.StringUtils;
import lombok.Data;

/**
 * sms设置
 *
 * @since 2024/9/4
 * @author CH
 */
@Data
@GlobalSettingGroup("sms")
public class SmsSetting implements Upgrade<SmsSetting> {

    /**
     * 短信厂家, 阿里云,百度云
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
    /** Upgrade */
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
