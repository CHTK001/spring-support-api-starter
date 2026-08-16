package com.chua.starter.system.demo;

import com.chua.starter.job.support.annotation.Job;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 演示用任务配置。
 *
 * <p>仅在系统服务模块中演示 job-starter 的任务注解使用方式。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@EnableScheduling
@SpringBootConfiguration
class DemoJobConfiguration {

    /**
     * 演示任务, 打印测试信息。
     */
    @Job("test")
    public void execute() {
        System.out.println("测试");
    }
}
