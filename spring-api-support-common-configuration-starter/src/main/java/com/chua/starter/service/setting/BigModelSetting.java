package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 大模型设置类，用于存储和处理与大模型应用相关的配置信息
 * 注意: BigModel 功能暂时禁用，因为 utils-support-deeplearning-starter 不可用
 *
 * @author CH
 * @since 2024/9/10
 */
@Data
public class BigModelSetting implements Upgrade<BigModelSetting> {

    private static final Logger log = LoggerFactory.getLogger(BigModelSetting.class);

    /**
     * 应用类型，标识大模型应用的类别
     */
    private String appType;

    /**
     * 应用ID，唯一标识一个大模型应用
     */
    private String appId;

    /**
     * 应用密钥，用于应用的安全验证
     */
    private String appSecret;

    /**
     * 应用公钥，与应用密钥配合使用，增强应用的安全性
     */
    private String appKey;

    @Override
    public void upgrade(BigModelSetting bigModelSetting) {
         if (null != bigModelSetting) {
             this.appType = bigModelSetting.appType;
             this.appId = bigModelSetting.appId;
             this.appSecret = bigModelSetting.appSecret;
             this.appKey = bigModelSetting.appKey;
             log.warn("BigModel 功能已禁用，appType: {}", appType);
         }
    }
}

