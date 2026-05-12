package com.chua.starter.system.test.handler;

import com.chua.starter.job.support.annotation.Job;
import com.chua.starter.job.support.thread.JobContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分布式调度测试用任务处理器
 */
@Slf4j
@Component
public class TestDemoJobHandler {

    private final AtomicLong executionCounter = new AtomicLong(0);

    @Job(value = "testDemoJobHandler", dispatchMode = "REMOTE", scheduleType = "cron", scheduleTime = "0/10 * * * * ?", autoStart = true)
    public void execute() {
        long n = executionCounter.incrementAndGet();
        String jobParam = JobContext.getJobContext().getJobParam();
        log.info("[TestDemoJobHandler] 远程任务执行 #{}, 参数={}", n, jobParam != null ? jobParam : "无");
    }

    public long getExecutionCount() {
        return executionCounter.get();
    }
}
