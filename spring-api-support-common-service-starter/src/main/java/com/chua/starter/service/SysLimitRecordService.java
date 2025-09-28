package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.system.entity.SysLimitRecord;

/**
 * 限流记录服务接口
 * 
 * @author CH
 * @since 2025/9/28
 */
public interface SysLimitRecordService extends IService<SysLimitRecord> {
    
    /**
     * 保存限流记录
     * 
     * @param sysLimitRecord 限流记录
     * @return 是否保存成功
     */
    boolean saveLimitRecord(SysLimitRecord sysLimitRecord);
}