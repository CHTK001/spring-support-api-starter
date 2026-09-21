package com.chua.starter.collect.configuration;

import com.chua.starter.scheduler.SchedulerApplication;
import com.chua.starter.scheduler.configuration.SchedulerJobConsoleWebMvcConfigurer;
import com.chua.starter.scheduler.controller.SchedulerJobConsoleAuthController;
import com.chua.starter.scheduler.controller.SchedulerJobConsoleController;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 调度平台并入采集宿主的装配。
 *
 * <p>采集与调度共用 job-starter 的 {@code sys_job / sys_job_log} 台账，这里只负责把调度平台的
 * {@code /v1/job-platform/**} 与 {@code /job-console/api/**} 接口面挂进本进程，不复制第二套调度账本。</p>
 *
 * <p>扫描时排掉两类东西：{@link SchedulerApplication} 自带 {@code com.chua.starter.**.mapper} 的全量
 * MapperScan 与一份重复的自动装配，跟着进来会静默改变采集侧的 Mapper 注册面（它的
 * {@code @EnableConfigurationProperties} 也一并被排除，所以调度属性由这里重新登记）；
 * {@link SchedulerJobConsoleController}、{@link SchedulerJobConsoleAuthController} 和
 * {@link SchedulerJobConsoleWebMvcConfigurer} 是旧控制台的页面壳与独立账号，合并后由采集控制台的
 * 一套登录和 Vue 作业台取代，只保留它与 {@code /job-console/api/**} 的数据接口。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SchedulerJobPlatformProperties.class)
@ComponentScan(
        basePackages = "com.chua.starter.scheduler",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SchedulerApplication.class,
                SchedulerJobConsoleController.class,
                SchedulerJobConsoleAuthController.class,
                SchedulerJobConsoleWebMvcConfigurer.class,
        })
)
@MapperScan("com.chua.starter.scheduler.mapper")
public class CollectSchedulerMergeConfiguration {
}
