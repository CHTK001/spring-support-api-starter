package com.chua.starter.service.configuration;

import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.configuration.resolver.VersionArgumentResolver;
import com.chua.starter.service.setting.VersionSetting;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 版本参数解析器
 * @author CH
 * @since 2024/8/26
 */
@Configuration
public class SystemVersionArgumentResolver implements VersionArgumentResolver {

    @Override
    public String version() {
        return GlobalSettingFactory.getInstance().get("config", VersionSetting.class).getVersion();
    }
}
