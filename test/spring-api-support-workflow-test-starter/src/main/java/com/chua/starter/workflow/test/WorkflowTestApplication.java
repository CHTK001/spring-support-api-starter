package com.chua.starter.workflow.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Workflow 联调测试后端入口。
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.chua.starter.workflow.test",
        "com.chua.starter.workflow",
})
public class WorkflowTestApplication {

    public static void main(String[] args) {
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "workflow-test");
        }
        SpringApplication.run(WorkflowTestApplication.class, args);
    }
}
