package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.common.support.core.utils.ServiceProvider;
import com.chua.common.support.core.utils.IoUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.deeplearning.support.ml.bigmodel.BigModelClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 大模型设置类，用于存储和处理与大模型应用相关的配置信息
 *
 * @author CH
 * @since 2024/9/10
 */
@Data
@Slf4j
public class BigModelSetting implements Upgrade<BigModelSetting> {

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


    private BigModelClient bigModelClient;

    public BigModelClient getBigModelClient() {
        if(null == bigModelClient) {
            throw new RuntimeException(StringUtils.format("暂不支持{}, 请重新设置!", appType));
        }
        return bigModelClient;
    }

    @Override
    public void upgrade(BigModelSetting bigModelSetting) {
         if (null != bigModelSetting) {
             this.appType = bigModelSetting.appType;
             this.appId = bigModelSetting.appId;
             this.appSecret = bigModelSetting.appSecret;
             this.appKey = bigModelSetting.appKey;
             if(null != this.bigModelClient) {
                 IoUtils.closeQuietly(bigModelClient);
             }
             this.bigModelClient = ServiceProvider.of(BigModelClient.class).getNewExtension(bigModelSetting.appType, com.chua.deeplearning.support.ml.bigmodel.BigModelSetting.builder()
                     .appId(appId)
                     .appKey(appKey)
                     .appSecret(appSecret)
                     .build());
             if(null == bigModelClient) {
                 // 使用 System.out.println 代替 log，避免 Lombok 处理问题
                 System.out.println(StringUtils.format("暂不支持{}, 请重新设置!", appType));
             }
         }
    }
}

