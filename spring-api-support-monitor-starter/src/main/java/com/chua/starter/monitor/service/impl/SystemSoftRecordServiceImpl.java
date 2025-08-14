package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRecord;
import com.chua.starter.monitor.mapper.SystemSoftRecordMapper;
import com.chua.starter.monitor.service.SystemSoftRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 软件安装记录服务实现
 */
@Slf4j
@Service
public class SystemSoftRecordServiceImpl extends ServiceImpl<SystemSoftRecordMapper, SystemSoftRecord> implements SystemSoftRecordService {

    @Override
    public ReturnResult<Boolean> install(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds) {
        // TODO: 实现安装逻辑（Docker/SSH）
        log.info("安装软件，占位: softId={}, versionId={}, servers={}", systemSoftId, systemSoftVersionId, serverIds);
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Boolean> uninstall(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds) {
        // TODO: 实现卸载逻辑（Docker/SSH）
        log.info("卸载软件，占位: softId={}, versionId={}, servers={}", systemSoftId, systemSoftVersionId, serverIds);
        return ReturnResult.ok(true);
    }
}

