package com.chua.starter.monitor.service;

import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;

/**
 * IP地址服务
 *
 * @author CH
 * @since 2024/7/31
 */
public interface ReportIptablesService {


    /**
     * 转换报表地址
     * @param ip ip
     * @return 地址
     */
    ReturnResult<GeoCity> transferReportAddress(String ip);
}
