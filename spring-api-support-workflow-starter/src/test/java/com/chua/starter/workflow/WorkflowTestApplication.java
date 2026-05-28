package com.chua.starter.workflow;

import com.chua.starter.workflow.configuration.WorkflowAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 工作流集成测试应用入口
 */
@SpringBootApplication
@MapperScan(basePackages = "com.chua.starter.workflow.mapper")
@ComponentScan(basePackages = "com.chua.starter.workflow")
public class WorkflowTestApplication {
}
