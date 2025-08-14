package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;

/**
 * 软件信息服务
 */
public interface SystemSoftService extends IService<SystemSoft> {

    /** 同步（从docker拉取软件信息） */
    ReturnResult<Boolean> syncFromDocker();
}

