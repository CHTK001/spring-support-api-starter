package com.chua.starter.payment.test;

import com.chua.starter.ai.support.entity.SysAiGlobalSetting;
import com.chua.starter.ai.support.model.SysAiGlobalSettingSaveRequest;
import com.chua.starter.ai.support.service.SysAiGlobalSettingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 支付测试模块 AI 相关配置。
 * 提供空实现的 {@link SysAiGlobalSettingService} bean，避免 RAG 模块因扫描不到
 * AI starter 的 {@code SysAiGlobalSettingServiceImpl}（缺少 @Service 注解）而启动失败。
 * 测试模块不启用 AI 功能，空实现已满足需求。
 */
@Configuration
public class PaymentTestAiConfiguration {

    @Bean
    @ConditionalOnMissingBean(SysAiGlobalSettingService.class)
    public SysAiGlobalSettingService sysAiGlobalSettingService() {
        return new SysAiGlobalSettingService() {
            @Override
            public List<SysAiGlobalSetting> list(String group) {
                return Collections.emptyList();
            }

            @Override
            public boolean replace(SysAiGlobalSettingSaveRequest request) {
                return true;
            }

            @Override
            public Optional<String> getValue(String group, String field) {
                return Optional.empty();
            }

            @Override
            public boolean getBoolean(String group, String field, boolean defaultValue) {
                return defaultValue;
            }
        };
    }
}
