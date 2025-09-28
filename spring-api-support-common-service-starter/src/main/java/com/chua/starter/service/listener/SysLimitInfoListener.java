package com.chua.starter.service.listener;

import com.chua.starter.service.SysLimitRecordService;
import com.chua.starter.system.entity.SysLimitInfo;
import com.chua.starter.system.entity.SysLimitRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 限流事件监听器
 * 
 * @author CH
 * @since 2025/9/28
 */
@Slf4j
@Component
public class SysLimitInfoListener implements ApplicationListener<SysLimitInfo> {
    
    @Autowired
    private SysLimitRecordService sysLimitRecordService;
    
    @Async
    @Override
    public void onApplicationEvent(SysLimitInfo event) {
        try {
            // 异步处理限流事件，保存到数据库
            SysLimitRecord sysLimitRecord = new SysLimitRecord();
            sysLimitRecord.setSysLimitConfigurationId(event.getSysLimitConfigurationId());
            sysLimitRecord.setSysLimitPath(event.getSysLimitPath());
            sysLimitRecord.setSysLimitName(event.getSysLimitName());
            sysLimitRecord.setSysLimitDimension(event.getSysLimitDimension());
            sysLimitRecord.setSysLimitKey(event.getSysLimitKey());
            sysLimitRecord.setSysLimitTime(event.getSysLimitTime() != null ? event.getSysLimitTime() : new Date());
            sysLimitRecord.setSysUserId(event.getSysUserId());
            sysLimitRecord.setSysUserName(event.getSysUserName());
            sysLimitRecord.setClientIp(event.getClientIp());
            sysLimitRecord.setRequestUrl(event.getRequestUrl());
            sysLimitRecord.setRequestMethod(event.getRequestMethod());
            sysLimitRecord.setUserAgent(event.getUserAgent());
            
            sysLimitRecordService.saveLimitRecord(sysLimitRecord);
        } catch (Exception e) {
            log.error("处理限流事件失败", e);
        }
    }
}