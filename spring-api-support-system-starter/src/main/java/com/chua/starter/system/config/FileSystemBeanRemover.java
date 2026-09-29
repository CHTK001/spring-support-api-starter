package com.chua.starter.system.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 移除文件系统模块的循环依赖 bean
 */
@Component
public class FileSystemBeanRemover implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof DefaultListableBeanFactory registry) {
            String[] beansToRemove = {
                "sysFileSystemController",
                "sysFileSystemServiceImpl",
                "sysFileSystemSettingServiceImpl",
                "sysFileSystemGroupServiceImpl",
                "sysFileSystemPartServiceImpl",
                "sysFileSystemCleanTask",
                "fileSystemCleanJobHandler",
                "sysFileSystemMergeTaskRegistration"
            };
            for (String beanName : beansToRemove) {
                if (registry.containsBeanDefinition(beanName)) {
                    registry.removeBeanDefinition(beanName);
                }
            }
        }
    }
}
