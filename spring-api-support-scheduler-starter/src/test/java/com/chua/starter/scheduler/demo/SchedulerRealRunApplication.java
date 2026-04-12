package com.chua.starter.scheduler.demo;

import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.system.properties.SystemSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties({SchedulerJobPlatformProperties.class, SystemSecurityProperties.class, AuthClientProperties.class})
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.scheduler"
})
public class SchedulerRealRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerRealRunApplication.class, args);
    }

    @Bean
    public static BeanDefinitionRegistryPostProcessor schedulerRealRunBeanFilter() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                removeBeanDefinition(registry, "userAgreementSettingInitializer");
                removeBeanDefinition(registry, "feedbackMenuInitializer");
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            }

            private void removeBeanDefinition(BeanDefinitionRegistry registry, String beanName) {
                if (registry.containsBeanDefinition(beanName)) {
                    registry.removeBeanDefinition(beanName);
                }
            }
        };
    }
}
