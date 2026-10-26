package com.chua.starter.monitor.configuration.apm;

import com.chua.starter.monitor.controller.apm.SystemMetricController;
import org.springframework.beans.factory.config.BeanDefinition;
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
 * {@code controller.apm} 下的控制器；而自动装配又会 {@code @Import} 同一个类。</p>
 *
 * <p>扫描注册的 Bean 名是短名（{@code systemMetricController}），这里补注册用的是
 * 全限定类名，两者名字不同，所以只按名字查重是不够的：同名检查会漏掉扫描到的定义，
 * 最终同一个类会出现两个 Bean，{@code @RequestMapping} 随之变成
 * 「Ambiguous handler methods」（HTTP 409 非法状态异常）。因此这里改成按<b>类名</b>
 * 遍历注册表：只要已经存在同类型的 Bean 定义就跳过。</p>
 *
 * <p>宿主想换成自己的实现，声明同类型 Bean 即可。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class SystemMetricControllerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String beanName = ClassUtils.getQualifiedName(SystemMetricController.class);
        if (registry.containsBeanDefinition(beanName) || hasDefinitionOfType(registry, beanName)) {
            return;
        }
        registry.registerBeanDefinition(beanName, new RootBeanDefinition(SystemMetricController.class));
    }

    /**
     * 判断注册表里是否已有该类型的 Bean 定义（不区分 Bean 名）。
     *
     * @param registry 注册表
     * @param className 目标类全限定名
     * @return 已存在返回 {@code true}
     */
    private boolean hasDefinitionOfType(BeanDefinitionRegistry registry, String className) {
        for (String registeredName : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(registeredName);
            if (className.equals(definition.getBeanClassName())) {
                return true;
            }
        }
        return false;
    }
}
