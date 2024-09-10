package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.lang.message.MessagePush;
import com.chua.common.support.lang.message.config.PushSetting;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IoUtils;
import com.chua.common.support.utils.StringUtils;
import lombok.Data;

/**
 * sms设置
 * @author CH
 * @since 2024/9/4
 */
@Data
public class SmsSetting implements Upgrade<SmsSetting> {

    private MessagePush messagePush;
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
    public void upgrade(SmsSetting smtpSetting) {
        this.appType = smtpSetting.getAppType();
        this.appId = smtpSetting.getAppId();
        this.appSecret = smtpSetting.getAppSecret();
        PushSetting pushSetting = PushSetting.builder()
                .accessKeyId(appId)
                .accessKeySecret(appSecret)
                .build();
        if(null != this.messagePush) {
            IoUtils.closeQuietly(messagePush);
        }
        ServiceProvider<MessagePush> serviceProvider = ServiceProvider.of(MessagePush.class);
        this.messagePush = serviceProvider.getNewExtension(appType, pushSetting);
        if(null == messagePush) {
            throw new RuntimeException(StringUtils.format("暂不支持{}, 请重新设置!", appType));
        }
    }

    /**
     * 是否有效
     * @return boolean
     */
    public boolean isValid() {
        return null != messagePush && !StringUtils.isEmpty(appId) && !StringUtils.isEmpty(appSecret);
    }
}
