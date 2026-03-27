package com.chua.starter.monitor.engine.feature;

import com.chua.common.support.data.query.definition.query.TimeSeries;

/**
 * 时序特性。
 */
public interface SystemDataTimeSeriesFeature {

    TimeSeries timeSeries(String key);
}
