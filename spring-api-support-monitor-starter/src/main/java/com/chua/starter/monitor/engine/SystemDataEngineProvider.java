package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.starter.monitor.entity.SystemDataSetting;
import org.springframework.core.Ordered;

/**
 * 数据源引擎提供者。
 */
public interface SystemDataEngineProvider extends Ordered {

    /**
     * 当前提供者是否支持该数据源配置。
     *
     * @param setting 数据源配置
     * @return 是否支持
     */
    boolean supports(SystemDataSetting setting);

    /**
     * 创建对应的数据源引擎。
     *
     * @param setting 数据源配置
     * @param options 数据源选项
     * @return 数据源引擎
     */
    SystemDataEngine create(SystemDataSetting setting, DataSourceOptions options);

    @Override
    default int getOrder() {
        return 0;
    }
}
