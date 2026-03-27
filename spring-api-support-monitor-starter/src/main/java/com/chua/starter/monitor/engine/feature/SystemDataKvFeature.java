package com.chua.starter.monitor.engine.feature;

import com.chua.common.support.data.query.definition.query.Kv;

/**
 * KV 特性。
 */
public interface SystemDataKvFeature {

    Kv kv(String key);
}
