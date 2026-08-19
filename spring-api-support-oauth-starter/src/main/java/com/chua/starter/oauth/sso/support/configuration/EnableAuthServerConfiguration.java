package com.chua.starter.oauth.sso.support.configuration;

import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.common.support.serviceinfo.ServiceInfoRegistry;
import com.chua.starter.common.support.utils.BeanDefinitionRegistryUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.gitee.GiteeBinderProvider;
import com.chua.starter.oauth.sso.support.gitee.GiteeLoginProvider;
import com.chua.starter.oauth.sso.support.processor.ResponseAdviceMethodProcessor;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.CasProperties;
import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import com.chua.starter.oauth.sso.support.properties.ThirdPartyBinderProperties;
import com.chua.starter.oauth.sso.support.properties.ThirdPartyLoginProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * OAuth认证服务器自动配置类
 *
 * <p>这是OAuth认证服务器的核心配置类，负责初始化和配置认证服务器的各个组件。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>自动注册认证相关的Bean定义</li>
 *   <li>配置OAuth协议处理器</li>
 *   <li>初始化第三方登录提供者（如Gitee）</li>
 *   <li>设置用户信息解析器</li>
 *   <li>配置响应处理器和消息转换器</li>
 * </ul>
 *
 * <h3>支持的协议：</h3>
 * <ul>
 *   <li>HTTP协议 - 标准HTTP认证</li>
 *   <li>Custom协议 - 自定义认证协议</li>
 *   <li>JWT协议 - JSON Web Token认证</li>
 *   <li>JWT-RSA协议 - 基于RSA签名的JWT认证</li>
 * </ul>
 *
 * <h3>集成的第三方登录：</h3>
 * <ul>
 *   <li>Gitee登录和账号绑定</li>
 *   <li>支持扩展其他第三方登录提供者</li>
 * </ul>
 *
 * <p>该配置类实现了多个Spring接口，确保在应用启动的不同阶段正确初始化各个组件。</p>
 *
 * @author CH
 * @since 2022/7/23 8:51
 * @see AuthServerProperties 认证服务器配置属性
 * @see LoginCheck 登录检查服务
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({
        AuthServerProperties.class,
        ThirdPartyBinderProperties.class,
        CasProperties.class,
        ThirdPartyLoginProperties.class,
        OauthSecurityProperties.class
})
public class EnableAuthServerConfiguration implements BeanDefinitionRegistryPostProcessor,
        EnvironmentPostProcessor,
        CommandLineRunner,
        ApplicationContextAware,
        DisposableBean, WebMvcConfigurer {

    AuthServerProperties authServerProperties;
    private List<HttpMessageConverter<?>> messageConverters;

    /**
     * 登录检查服务Bean
     * <p>核心认证服务，负责处理登录验证逻辑，通过 Spring 容器获取 UserInfoService 实现</p>
     *
     * @param applicationContext Spring 应用上下文
     * @return 登录检查服务实例
     */
    @Bean
    public LoginCheck loginCheck(ApplicationContext applicationContext) {
        return new LoginCheck(authServerProperties, applicationContext);
    }

    /**
     * 注册 OAuth 认证服务器模块服务信息。
     *
     * @return 服务信息贡献器
     */
    @Bean
    public com.chua.starter.common.support.serviceinfo.ServiceInfoContributor authServerServiceInfoContributor() {
        return registry -> registry.registerFlag("oauth-server", "认证授权", true, "OAuth 认证服务器模块");
    }


    @Override
    /** Post处理BeanDefinitionRegistry */
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinitionRegistryUtils.registerBeanDefinition(registry, GiteeLoginProvider.class);
        BeanDefinitionRegistryUtils.registerBeanDefinition(registry, GiteeBinderProvider.class);
    }

    @Override
    /** Post处理BeanFactory */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    /** Post处理Environment */
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        properties.setProperty("spring.freemarker.templateLoaderPath", "classpath:/templates/");
        properties.setProperty("spring.freemarker.suffix", ".ftl");
        properties.setProperty("spring.freemarker.charset", "UTF-8");
        properties.setProperty("spring.freemarker.request-context-attribute", "request");
        properties.setProperty("spring.freemarker.number_format", "0.##########");
        properties.setProperty("spring.freemarker.cache", "false");
        properties.setProperty("spring.resources.static-locations", "classpath:/static/");
        properties.setProperty("spring.mvc.static-path-pattern", "/static/**");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("oauth", properties);
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addLast(propertiesPropertySource);
    }

    @Override
    /** 销毁 */
    public void destroy() throws Exception {
    }


    @Override
    /** 设置ApplicationContext */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (null != authServerProperties) {
            return;
        }
        this.messageConverters = new LinkedList<>();
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());

        authServerProperties = Binder.get(applicationContext.getEnvironment()).bindOrCreate(AuthServerProperties.PRE, AuthServerProperties.class);
    }

    @Override
    /** 添加ReturnValueHandlers */
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(0, new ResponseAdviceMethodProcessor(messageConverters));
    }

    @Override
    /** 运行 */
    public void run(String... args) throws Exception {
        ServiceInfoRegistry registry = ServiceInfoRegistry.getInstance();
        registry.registerFlag("auth-server", "鉴权服务",
                authServerProperties.isEnable(),
                "鉴权服务器 [加密: " + authServerProperties.getEncryption() + "]",
                null);
        registry.registerFlag("auth-server-aksk-check", "鉴权服务",
                authServerProperties.isOpenCheckAkSk(), "AK/SK 密钥校验", null);
    }
}
