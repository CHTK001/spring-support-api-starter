package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.HttpEngine;
import com.chua.starter.monitor.entity.SystemDataSetting;

/**
 * HTTP 专用引擎。
 */
public class HttpSystemDataEngine extends AbstractSystemDataEngine {

    private HttpEngine httpEngine;

    public HttpSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public <T> T unwrap(Class<T> targetType) {
        HttpEngine engine = ensureHttpEngine();
        if (targetType != null && targetType.isInstance(engine)) {
            return targetType.cast(engine);
        }
        return super.unwrap(targetType);
    }

    @Override
    public void close() throws Exception {
        if (httpEngine != null) {
            httpEngine.close();
        }
        super.close();
    }

    private synchronized HttpEngine ensureHttpEngine() {
        if (httpEngine == null) {
            httpEngine = new HttpEngine();
        }
        return httpEngine;
    }
}
