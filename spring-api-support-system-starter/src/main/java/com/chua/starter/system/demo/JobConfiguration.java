package com.chua.starter.system.demo;


import com.chua.starter.job.support.annotation.Job;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootConfiguration
class DemoJobConfiguration {


    @Job("test")
    public void execute() {
        System.out.println("测试");
    }
}
