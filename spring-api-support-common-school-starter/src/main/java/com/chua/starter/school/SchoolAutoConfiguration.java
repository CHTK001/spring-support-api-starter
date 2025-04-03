package com.chua.starter.school;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 学校模块自动配置
 *
 * @author CH
 */
@Configuration
@ComponentScan("com.chua.starter.school")
@MapperScan("com.chua.starter.school.mapper")
public class SchoolAutoConfiguration {
} 