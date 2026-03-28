package com.chua.starter.oshi.configuration;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.oshi.performance.SystemPerformanceController;
import com.chua.starter.oshi.performance.SystemPerformanceService;
import com.chua.starter.oshi.performance.SystemPerformanceServiceImpl;
import com.chua.starter.oshi.performance.SystemPerformanceSettingInitializer;
import com.chua.starter.oshi.utils.LocalSystemDataCollector;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * OSHI 相关自动装配。
 *
 * @author CH
 * @since 2026/3/28
 */
@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SysSettingMapper.class, LocalSystemDataCollector.class, SysSettingService.class})
@ConditionalOnMissingClass("com.chua.starter.monitor.configuration.PerformanceMonitorAutoConfiguration")
public class OshiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LocalSystemDataCollector localSystemDataCollector() {
        return new LocalSystemDataCollector();
    }

    @Bean
    @ConditionalOnBean(SysSettingService.class)
    @ConditionalOnMissingBean
    public SystemPerformanceService systemPerformanceService(
            SysSettingService sysSettingService,
            LocalSystemDataCollector localSystemDataCollector,
            ObjectProvider<SocketSessionTemplate> socketSessionTemplateProvider) {
        return new SystemPerformanceServiceImpl(sysSettingService, localSystemDataCollector, socketSessionTemplateProvider);
    }

    @Bean
    @ConditionalOnBean(SystemPerformanceService.class)
    @ConditionalOnMissingBean
    public SystemPerformanceController systemPerformanceController(SystemPerformanceService systemPerformanceService) {
        return new SystemPerformanceController(systemPerformanceService);
    }

    @Bean
    @ConditionalOnBean({SysSettingMapper.class, SysSettingGroupMapper.class})
    @ConditionalOnMissingBean
    public SystemPerformanceSettingInitializer systemPerformanceSettingInitializer(
            SysSettingMapper sysSettingMapper,
            SysSettingGroupMapper sysSettingGroupMapper) {
        return new SystemPerformanceSettingInitializer(sysSettingMapper, sysSettingGroupMapper);
    }
}
