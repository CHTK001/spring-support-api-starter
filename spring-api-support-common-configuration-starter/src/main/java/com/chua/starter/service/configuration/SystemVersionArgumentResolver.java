package com.chua.starter.service.configuration;

import com.chua.common.support.base.bean.BeanUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.configuration.resolver.VersionArgumentResolver;
import com.chua.starter.service.setting.VersionSetting;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 版本参数解析器
 * @author CH
 * @since 2024/8/26
 */
@Configuration
public class SystemVersionArgumentResolver implements VersionArgumentResolver {

    @Override
    public String version() {
        var setting = GlobalSettingFactory.getInstance().get("config", VersionSetting.class);
        if (setting != null) {
            // 使用 BeanUtils 获取字段值
            var map = BeanUtils.toMapAsm(setting);
            return map != null ? (String) map.get("version") : null;
        }
        return null;
    }
}
