package com.chua.starter.system.configuration;

import com.chua.starter.common.support.serviceinfo.ServiceInfoRegistry;
import com.chua.starter.system.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
* 系统服务自动配置类
*
* <p>这是系统服务的核心配置类，负责初始化和配置系统服务的各个组件。</p>
*
* <h3>主要功能：</h3>
* <ul>
*   <li>自动配置系统服务相关Bean</li>
*   <li>初始化系统管理功能</li>
*   <li>配置Web MVC相关设置</li>
* </ul>
*
* <h3>支持的功能：</h3>
* <ul>
*   <li>用户管理 - 用户增删改查、状态管理、密码重置</li>
*   <li>角色管理 - 角色增删改查、权限分配</li>
*   <li>菜单管理 - 菜单树形结构、权限控制</li>
*   <li>部门管理 - 部门树形结构、层级管理</li>
* </ul>
*
* @since 2024/12/11
* @see SystemProperties 系统服务配置属性
* @author CH
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({SystemProperties.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EnableSystemServerConfiguration implements WebMvcConfigurer, CommandLineRunner {

    /**
    * 系统服务配置属性
     */
    private final SystemProperties systemProperties;

    /**
    * 应用启动后注册系统服务信息标志。
    *
    * @param args 启动参数
     */
    @Override
    public void run(String... args) throws Exception {
        ServiceInfoRegistry.getInstance().registerFlag(
                "system-server", "系统服务",
                systemProperties.isEnable(), "系统服务", null);
    }
}

