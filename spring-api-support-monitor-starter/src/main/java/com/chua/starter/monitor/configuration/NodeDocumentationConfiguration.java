package com.chua.starter.monitor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 节点文档配置
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Configuration
public class NodeDocumentationConfiguration {

    /**
     * 配置RestTemplate用于节点文档API调用
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒连接超时
        factory.setReadTimeout(30000);    // 30秒读取超时
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        log.info("节点文档RestTemplate配置完成 - 连接超时: {}ms, 读取超时: {}ms",
                10000, 30000);
        
        return restTemplate;
    }
}
