package com.chua.starter.oauth.sso.configuration;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.oauth.sso.properties.TenantDataSourceProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.List;

/**
 * 租户数据库
 *
 * @author CH
 */
@Configuration
@ConditionalOnProperty(prefix = TenantDataSourceProperties.PRE,
        name = "enable",
        havingValue = "true")
public class TenantConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {


    private TenantDataSourceProperties tenantDataSourceProperties;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<DataSourceProperties> properties = tenantDataSourceProperties.getProperties();
        for (DataSourceProperties dataSourceProperties : properties) {
            String name = dataSourceProperties.getName();
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            DataSource dataSource = DataSourceBuilder.create()
                    .type(dataSourceProperties.getType())
                    .driverClassName(dataSourceProperties.getDriverClassName())
                    .url(dataSourceProperties.getUrl())
                    .username(dataSourceProperties.getUsername())
                    .password(dataSourceProperties.getPassword())
                    .build();

            registry.registerBeanDefinition(name,
                    BeanDefinitionBuilder.rootBeanDefinition(DataSource.class, () -> dataSource).getBeanDefinition()
            );

            DataSourceContextSupport.addDatasource(name, dataSource);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        tenantDataSourceProperties = Binder.get(environment)
                .bindOrCreate(TenantDataSourceProperties.PRE, TenantDataSourceProperties.class);
    }
}
