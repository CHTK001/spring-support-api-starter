package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord;
import com.chua.starter.monitor.mapper.MonitorSysGenScriptUploadRecordMapper;
import com.chua.starter.monitor.service.script.MonitorSysGenScriptUploadRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 脚本上传记录 Service 实现
 */
@Slf4j
@Service
public class MonitorSysGenScriptUploadRecordServiceImpl extends ServiceImpl<MonitorSysGenScriptUploadRecordMapper, MonitorSysGenScriptUploadRecord>
        implements MonitorSysGenScriptUploadRecordService {
}

