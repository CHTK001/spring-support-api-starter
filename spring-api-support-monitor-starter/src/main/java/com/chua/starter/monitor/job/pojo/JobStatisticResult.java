package com.chua.starter.monitor.job.pojo;


import lombok.Data;

@Data
public class JobStatisticResult {

    private Long[] successCount;

    private Long[] failureCount;
}
