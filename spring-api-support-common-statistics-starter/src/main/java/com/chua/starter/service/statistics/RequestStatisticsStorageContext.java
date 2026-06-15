package com.chua.starter.service.statistics;

import java.util.Map;

public record RequestStatisticsStorageContext(
        String storageType,
        Map<String, Object> options
) {
    public RequestStatisticsStorageContext {
        options = options == null ? Map.of() : Map.copyOf(options);
    }
}
