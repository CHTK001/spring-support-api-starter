package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.SysLimitRecordService;
import com.chua.starter.service.mapper.SysLimitRecordMapper;
import com.chua.starter.system.entity.SysLimitRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 限流记录服务实现类
 * 
 * @author CH
 * @since 2025/9/28
 */
@Slf4j
@Service
public class SysLimitRecordServiceImpl extends ServiceImpl<SysLimitRecordMapper, SysLimitRecord> implements SysLimitRecordService {

    @Override
    public boolean saveLimitRecord(SysLimitRecord sysLimitRecord) {
        return this.save(sysLimitRecord);
    }
}