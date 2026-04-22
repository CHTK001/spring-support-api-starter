package com.chua.starter.monitor.spi.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.starter.monitor.entity.MonitorSysGenServerLoginRecord;
import com.chua.starter.monitor.mapper.MonitorSysGenServerLoginRecordMapper;
import com.chua.starter.monitor.spi.ServerLoginRecordProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 基于数据库的服务器登录记录提供者
 */
@Spi("database")
@SpiDescribe("数据库登录记录提供者")
@RequiredArgsConstructor
@Slf4j
@Service
public class DatabaseServerLoginRecordProvider implements ServerLoginRecordProvider {

    private final MonitorSysGenServerLoginRecordMapper loginRecordMapper;

    @Override
    public String provider() {
        return "database";
    }

    @Override
    public List<MonitorSysGenServerLoginRecord> getTopNLoginRecords(Integer serverId, int topN) {
        if (loginRecordMapper == null) {
            return Collections.emptyList();
        }
        try {
            return loginRecordMapper.getTopNLoginRecords(serverId, topN);
        } catch (Exception e) {
            log.error("获取登录记录失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean recordLogin(MonitorSysGenServerLoginRecord record) {
        if (loginRecordMapper == null || record == null) {
            return false;
        }
        try {
            loginRecordMapper.insert(record);
            return true;
        } catch (Exception e) {
            log.error("保存登录记录失败", e);
            return false;
        }
    }
}
