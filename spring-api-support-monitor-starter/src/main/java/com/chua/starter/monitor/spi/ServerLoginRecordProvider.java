package com.chua.starter.monitor.spi;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.starter.monitor.entity.MonitorSysGenServerLoginRecord;

import java.util.List;

/**
 * 服务器登录记录 SPI 接口
 * 使用 @Spi("实现编码") 提供不同数据源的登录记录实现
 */
@SpiDescribe("服务器登录记录SPI")
public interface ServerLoginRecordProvider {

    /**
     * 获取提供者编码
     */
    String provider();

    /**
     * 获取最近 N 条登录记录
     *
     * @param serverId 服务器ID（null表示所有服务器）
     * @param topN     获取记录数
     * @return 登录记录列表
     */
    List<MonitorSysGenServerLoginRecord> getTopNLoginRecords(Integer serverId, int topN);

    /**
     * 记录登录事件
     *
     * @param record 登录记录
     * @return 是否记录成功
     */
    default boolean recordLogin(MonitorSysGenServerLoginRecord record) {
        return true;
    }
}