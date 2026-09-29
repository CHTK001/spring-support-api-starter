package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MsOperationLog;
import com.chua.starter.monitor.mapper.MsOperationLogMapper;
import com.chua.starter.monitor.service.MsOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现。
 *
 * @since 2026-08-11
 * @author CH
 */
@Slf4j
@Service
public class MsOperationLogServiceImpl extends ServiceImpl<MsOperationLogMapper, MsOperationLog>
        implements MsOperationLogService {

    /**
     * 记录一条操作日志；失败时仅打日志，不影响主流程。
     */
    @Override
    public void log(String operator, String operation, String targetModule, String targetId, String description) {
        try {
            MsOperationLog record = new MsOperationLog();
            record.setOperator(operator);
            record.setOperation(operation);
            record.setTargetModule(targetModule);
            record.setTargetId(targetId);
            record.setDescription(description);
            record.setCreateTime(LocalDateTime.now());
            save(record);
        } catch (Exception e) {
            log.warn("记录操作日志失败: operator={}, operation={}, module={}, id={}", operator, operation, targetModule, targetId, e);
        }
    }
}
