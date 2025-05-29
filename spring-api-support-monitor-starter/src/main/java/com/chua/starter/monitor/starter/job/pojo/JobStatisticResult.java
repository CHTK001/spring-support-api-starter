package com.chua.starter.monitor.starter.job.pojo;


import lombok.Data;

@Data
public class JobStatisticResult {

    private Long[] successCount;

    private Long[] failureCount;
}
