package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRecord;

/**
 * 软件安装记录服务
 */
public interface SystemSoftRecordService extends IService<SystemSoftRecord> {

    /** 安装（多台服务器） */
    ReturnResult<Boolean> install(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds);

    /** 卸载（多台服务器） */
    ReturnResult<Boolean> uninstall(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds);
}

