package com.chua.starter.system;

import com.chua.report.client.starter.job.log.DefaultJobLog;
import com.chua.report.client.starter.job.log.JobLog;
import com.chua.starter.common.support.annotations.Job;
import org.springframework.boot.SpringBootConfiguration;

/**
 * @author CH
 * @since 2024/9/15
 */
@SpringBootConfiguration
public class TestDemo {


    @Job("test")
    public void test() {
        DefaultJobLog.log("测试");
    }
}
