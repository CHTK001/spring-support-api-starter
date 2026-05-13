package com.chua.starter.job.test;

import com.chua.starter.job.support.annotation.Job;
import com.chua.starter.job.support.log.DefaultJobLog;
import com.chua.starter.job.support.thread.JobContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication(scanBasePackages = "com.chua.starter.job.support")
public class ExecutorTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecutorTestApplication.class, args);
    }

    @Component
    public static class DemoJobHandler {

        @Job("demoJob")
        public void execute() {
            String param = JobContext.getJobParam();
            DefaultJobLog.log("demoJob 远程执行, param=" + param);
            JobContext.getJobContext().setSuccess("demoJob success: " + param);
        }
    }
}
