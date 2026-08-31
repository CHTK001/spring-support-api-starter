package com.chua.starter.system.demo;

import com.chua.starter.job.support.annotation.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 演示用任务配置。
 *
 * <p>仅在系统服务模块中演示 job-starter 的任务注解使用方式。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@EnableScheduling
@SpringBootConfiguration
class DemoJobConfiguration {

    /**
     * 演示任务, 打印测试信息。
     */
    @Job("test")
    public void execute() {
        log.info("测试任务执行");
    }
}
