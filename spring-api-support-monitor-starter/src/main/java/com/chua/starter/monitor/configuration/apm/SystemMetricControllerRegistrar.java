package com.chua.starter.monitor.configuration.apm;

import com.chua.starter.monitor.controller.apm.SystemMetricController;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * 按需注册 {@link SystemMetricController}，避开与宿主组件扫描的重复定义。
 *
 * <p>监控服务自身（{@code MonitorServerApplication}）以
 * {@code scanBasePackages = "com.chua.starter.monitor"} 启动，会扫到
 * {@code controller.apm} 下的控制器；而自动装配又会 {@code @Import} 同一个类。
 * Spring 以「全限定类名」作为 Bean 名称，两条路径命中的名字完全相同，直接
 * {@code @Import} 会抛 {@code ConflictingBeanDefinitionException}。</p>
 *
 * <p>所以这里先查注册表：该 Bean 名已存在（被扫描到了）就跳过，只在宿主
 * 扫不到时补注册。宿主想换成自己的实现，声明同类型 Bean 即可。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class SystemMetricControllerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String beanName = ClassUtils.getQualifiedName(SystemMetricController.class);
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        registry.registerBeanDefinition(beanName, new RootBeanDefinition(SystemMetricController.class));
    }
}
