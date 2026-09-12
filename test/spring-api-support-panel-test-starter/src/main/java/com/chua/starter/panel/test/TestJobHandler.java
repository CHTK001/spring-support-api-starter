package com.chua.starter.panel.test;

import com.chua.task.support.task.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
* 面板测试定时任务处理器，用于验证任务调度框架基本功能。
*
* <p>每秒自增计数器并输出日志，仅用于测试目的。
*
* @author CH
* @since 4.0.0.42
 */
@Slf4j
@Component
public class TestJobHandler {

    private final AtomicLong counter = new AtomicLong(0); // 数量

    @Scheduled("0 0/1 * * * ?")
    public void execute() {
        long n = counter.incrementAndGet();
        log.info("[TestJobHandler] 执行 #{}", n);
    }
}
