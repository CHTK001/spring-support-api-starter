package com.chua.starter.system.demo;


import com.chua.starter.common.support.annotations.Job;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootConfiguration
public class JobConfiguration {


    @Job("test")
    public void execute() {
        System.out.println("测试");
    }
}
