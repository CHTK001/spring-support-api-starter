package com.chua.starter.monitor.job.pojo;


import lombok.Data;

import java.time.LocalDate;

@Data
public class JobStatistic {

    private Long cnt;

    private String jobLogTriggerCode;

    private LocalDate jobLogTriggerDate;
}
