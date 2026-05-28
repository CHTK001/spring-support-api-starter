package com.chua.starter.workflow.configuration;

import com.chua.starter.workflow.engine.AssignmentStrategy;
import com.chua.starter.workflow.engine.AssignmentStrategyFactory;
import com.chua.starter.workflow.engine.strategies.*;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.List;

/**
 * 工作流模块自动配置
 *
 * @author CH
 * @since 2026/5/28
 */
@Slf4j
@AutoConfiguration
@ComponentScan(basePackages = "com.chua.starter.workflow")
@MapperScan(basePackages = "com.chua.starter.workflow.mapper")
public class WorkflowAutoConfiguration {

    public WorkflowAutoConfiguration() {
        log.info("轻量级审批工作流模块已启用");
    }

    @Bean
    public AssignmentStrategyFactory assignmentStrategyFactory() {
        List<AssignmentStrategy> strategies = Arrays.asList(
                new ApplicantAssignmentStrategy(),
                new UserAssignmentStrategy(),
                new DeptAssignmentStrategy(),
                new DeptHeadAssignmentStrategy(),
                new RoleAssignmentStrategy()
        );
        return new AssignmentStrategyFactory(strategies);
    }
}
