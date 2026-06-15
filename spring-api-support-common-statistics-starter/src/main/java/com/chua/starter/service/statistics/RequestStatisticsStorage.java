package com.chua.starter.service.statistics;

import com.chua.common.support.core.annotation.SpiDescribe;

import java.util.List;

@SpiDescribe("请求统计记录存储SPI")
public interface RequestStatisticsStorage {

    String type();

    void storeBatch(List<RequestStatisticsRecord> records, RequestStatisticsStorageContext context);
}
