package com.chua.starter.panel.test;

import com.chua.task.support.task.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class TestJobHandler {

    private final AtomicLong counter = new AtomicLong(0);

    @Scheduled("0 0/1 * * * ?")
    public void execute() {
        long n = counter.incrementAndGet();
        log.info("[TestJobHandler] 执行 #{}", n);
    }
}
