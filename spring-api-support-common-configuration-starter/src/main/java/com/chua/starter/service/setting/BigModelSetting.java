package com.chua.starter.service.setting;

import com.chua.common.support.function.Upgrade;
import com.chua.deeplearning.support.ai.bigmodel.BigModelClient;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.IoUtils;
import com.chua.common.support.core.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 大模型设置类，用于存储和处理与大模型应用相关的配置信息
 *
 * @since 2024/9/10
 * @author CH
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

    /**
     * API Host 地址（如 https://api.agnes-ai.com）
     */
    private String host;

    /**
     * API Base URL（如 https://api.agnes-ai.com/v1/chat/completions）
     */
    private String baseUrl;


    /** BIG模型客户端 */
    private BigModelClient bigModelClient;

    /** 获取BigModelClient */
    public BigModelClient getBigModelClient() {
        if(null == bigModelClient) {
            throw new RuntimeException(StringUtils.format("暂不支持{}, 请重新设置!", appType));
        }
        return bigModelClient;
    }

    @Override
    /** Upgrade */
    public void upgrade(BigModelSetting bigModelSetting) {
         if (null != bigModelSetting) {
             this.appType = bigModelSetting.appType;
             this.appId = bigModelSetting.appId;
             this.appSecret = bigModelSetting.appSecret;
             this.appKey = bigModelSetting.appKey;
             this.host = bigModelSetting.host;
             this.baseUrl = bigModelSetting.baseUrl;
             if(null != this.bigModelClient) {
                 IoUtils.closeQuietly(bigModelClient);
             }
             com.chua.common.support.ai.bigmodel.BigModelSetting bms = com.chua.common.support.ai.bigmodel.BigModelSetting.builder()
                     .appId(appId)
                     .appKey(appKey)
                     .appSecret(appSecret)
                     .host(host)
                     .baseUrl(baseUrl)
                     .provider(appType)
                     .build();
             // 优先使用 openai（嵌入式兼容），其次用配置的 appType，最后回退默认
             ServiceProvider<BigModelClient> provider = ServiceProvider.of(BigModelClient.class);
             this.bigModelClient = provider.getFirstAvailable(new Object[]{bms}, "openai", bigModelSetting.appType);
              if(null == bigModelClient) {
                  log.warn(StringUtils.format("暂不支持{}, 请重新设置!", appType));
              }
         }
    }
}
